# 二、Agent 核心范式

本文是学习路线的阶段二（第 2–3 周）：手写一个不依赖任何框架的最小 Agent Loop（Java 实现），掌握经典 Agent 范式与上下文工程。这是全程最关键的一步——先懂原理，再用框架。


## 一、Agent 的定义与最小闭环

**Agent = LLM + 工具 + 循环**。与一次性问答的区别在于：模型可以**多轮决定**做什么，直到任务完成或达到终止条件。

最小闭环：

```
while (未完成 && 未超预算) {
    1. 组装上下文（系统提示 + 历史 + 本轮输入 + 可用工具）
    2. 调用 LLM
    3. 若返回 tool_calls → 执行工具 → 结果追加为 tool 消息 → 继续
    4. 若返回纯文本 → 视为最终答案，结束
}
```

四个必须处理的终止/保护条件：
1. **最大迭代次数**（防止死循环烧钱）；
2. **总 token / 总耗时预算**；
3. **工具连续失败次数**（如 3 次失败即降级退出）；
4. **人工审批闸点**（高危操作前挂起，见第四节）。


## 二、Java 手写最小 Agent Loop

下面是可运行思路的骨架（省略具体厂商 SDK，核心在循环结构）：

```java
public class AgentLoop {
    private final LlmClient llm;            // 封装模型调用
    private final ToolRegistry tools;       // 工具注册表
    private final int maxIterations = 10;   // 防死循环

    public String run(String userInput) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(loadPrompt("agent-system.txt")));
        messages.add(new UserMessage(userInput));

        for (int i = 0; i < maxIterations; i++) {
            LlmResponse resp = llm.chat(messages, tools.definitions());

            if (resp.toolCalls().isEmpty()) {
                return resp.content();          // 最终答案
            }
            messages.add(resp.assistantMessage()); // 记录模型的工具调用意图
            for (ToolCall call : resp.toolCalls()) {
                ToolResult result = safeInvoke(call, tools); // 统一异常与超时
                messages.add(result.toMessage());            // 结果回填
            }
        }
        throw new BudgetExceededException("达到最大迭代次数");
    }

    private ToolResult safeInvoke(ToolCall call, ToolRegistry tools) {
        try {
            return tools.invoke(call.name(), call.arguments());
        } catch (Exception e) {
            return ToolResult.error(call.id(), "工具执行失败: " + e.getMessage());
        }
    }
}
```

**工具注册表**（描述质量决定准确率）：

```java
public record ToolDefinition(String name, String description, JsonSchema schema) {}

ToolRegistry tools = new ToolRegistry()
    .register("query_alert",
              "查询最近 N 分钟的告警列表。当需要了解系统当前异常时调用。",
              schemaOf("minutes", "int", "时间范围，单位分钟"))
    .register("get_topology",
              "查询告警设备的上下游拓扑关系。用于根因分析时追查影响面。",
              schemaOf("deviceId", "string", "设备唯一标识"));
```

写完后自测三类用例：
1. **单工具直达**：一句话触发一次调用即完成；
2. **多步链式**：先查告警 → 再查拓扑 → 综合分析（观察循环如何多次往返）;
3. **异常路径**：工具抛异常/返回错误串，确认模型能自我纠正或优雅失败。


## 三、经典 Agent 范式

| 范式                      | 结构                                    | 适用场景             | 关键点                                                         |
|---------------------------|-----------------------------------------|----------------------|----------------------------------------------------------------|
| **ReAct**                 | Thought → Action → Observation 循环     | 通用工具调用         | 最基础范式；现代实现中"Thought"常被压缩进工具选择本身          |
| **Plan-and-Execute**      | 先生成完整计划，再逐步执行，可重规划    | 多步骤复杂任务       | 计划与执行解耦；执行中发现计划错误要触发重规划                 |
| **Reflexion**             | 执行 → 自我评估 → 改进 → 重试           | 代码生成、多轮优化   | 需要可度量的反馈信号（测试/评分）                              |
| **Supervisor / 多智能体** | 主控 Agent 分派任务给子 Agent，汇总结果 | 任务可拆解、专业分工 | 子 Agent 上下文隔离是价值所在（各看各的窗口）                  |
| **Human-in-the-Loop**     | 关键节点挂起，人工确认后继续            | 高危/合规场景        | 企业级刚需：审批即"工具执行前的闸门"                           |
| **Workflow vs Agent**     | 固定编排（DAG）对比模型自主决策         | 静态流程用 workflow  | Anthropic 建议：**能用 workflow 就别上 agent**，只为确定性买单 |

选型原则：**用最少的自主性完成任务**。自主性越高，评估、成本、安全的复杂度越高。


## 四、Human-in-the-Loop 实现要点

以"变更操作 Agent"为例的审批流程：

```
模型决定调用 execute_change()
  → 应用拦截：标记该工具为"需审批"
  → 挂起当前 Agent 状态（messages 落库，支持恢复）
  → 通知审批人（工单/IM），附：变更内容、模型理由、风险等级
  → 审批通过 → 恢复循环，真实执行工具，结果回填
  → 审批拒绝 → 回填"用户拒绝执行：原因X"，让模型改写方案
```

工程要点：
- **状态必须落库**（对话、工具调用、审批状态），进程重启后可恢复；
- **挂起是常态而非异常**，设计上按"天级等待"考虑，不占用内存会话；
- **拒绝也是信息**：把拒绝原因回填给模型，让它调整而非简单终止。


## 五、上下文工程（Context Engineering）

比提示词更关键的能力：管理那扇"会关上的窗"。

### 5.1 上下文的组成与预算

```
系统提示 + 工具定义（静态，占比应尽量小）
+ 工具结果（增长最快：一个大 JSON 就能吃掉几千 token）
+ 对话历史（线性增长）
+ 检索文档（见 03 册）
+ 预留输出
```

### 5.2 核心技术

| 技术                | 做法                                                  | 说明                                     |
|---------------------|-------------------------------------------------------|------------------------------------------|
| **Compaction 压缩** | 历史过长时，把旧轮次摘要成一段"前情提要"              | 保留结论与未决事项，丢弃过程细节         |
| **工具结果裁剪**    | 大结果截断/分页/只保留关键字段，或存文件给模型按需读  | "工具返回 10 万行"是最常见的上下文爆炸源 |
| **记忆分层**        | 短期=当前会话；长期=跨会话持久化（向量库/结构化存储） | 见 06 册进阶                             |
| **渐进披露**        | 工具定义先给摘要，模型按需查详细 schema               | 控制静态开销                             |
| **丢弃策略**        | 优先丢：重复内容、低相关旧轮次、原始日志              | 摘要成本高于结构化字段                   |

### 5.3 判断标准

上下文工程做得好 = 模型在第 N 轮仍能**拿到做正确决策所需的最少信息**。自检问题：
- 模型能看到所有工具的完整原始结果吗？（应该只看到摘要/关键字段）
- 摘要压缩后，未决任务和约束条件还在吗？
- 每轮 token 用量可追踪吗？（接 05 册可观测性）


## 六、学习资源（阶段二）

### 必读（按顺序）

1. **Anthropic 官方文章《Building Effective Agents》**——https://www.anthropic.com/research/building-effective-agents （企业级范式选型第一手资料，重点读 workflow vs agent、何时用多智能体）。
2. **ReAct 论文（2022）**——Yao et al., *"ReAct: Synergizing Reasoning and Acting in Language Models"*，读引言+方法即可，理解 Thought/Action/Observation 循环的起源。
3. **LangGraph 官方概念文档《Concepts》**——https://langchain-ai.github.io/langgraph/concepts/ （即使主线是 Java，也借它理解 State/Node/Edge/Checkpoint 等编排概念，是事实上的行业词汇表）。
4. **Anthropic 工程博客《How we built our multi-agent research system》**——多智能体协作的真实生产实践（Supervisor 模式的最好案例）。

### 选读

- **Reflexion 论文（2023）**——Shinn et al., *"Reflexion: Language Agents with Verbal Reinforcement Learning"*。
- **Plan-and-Solve / Plan-and-Execute 类论文**——搜索 "Plan-and-Solve prompting" 了解计划式范式。
- **《Generative Agents》（斯坦福小镇）**——Park et al.，理解记忆流（memory stream）设计，对应长期记忆。
- **Andrej Karpathy 关于 Agent 上下文管理的分享**——搜索 "Karpathy LLM context engineering"（含其 X/博客短文），理解 compaction 直觉。

### 国内资源（中文友好）

- **Spring AI Alibaba**（阿里）——https://java2ai.com/ ，Java 生态的国内 Agent 框架（ReactAgent/多智能体/Graph 编排），中文文档，可与 Spring AI 主线对照；
- **书生大模型实战营**（github.com/InternLM/Tutorial）——Lagent 智能体关卡，中文文档 + B站视频；
- **机器之心 / InfoQ 中文站**——检索 "Agent 架构"、"多智能体 生产实践"，看国内团队的落地案例。

### 动手实践（本周产出）

1. **用纯 Java 完成第二节的 AgentLoop**（可用任意模型 API，不引框架），跑通三类自测用例；
2. 把自己正在用的 Coding Agent（如 OpenCode/Claude Code）的行为当观察对象：它的工具列表、循环行为、上下文压缩迹象，逆向理解工程设计；
3. **用 Spring AI 重写同一 Agent**：对照 `ChatClient` + `@Tool` + `ToolCallback` 与自己手写版本的每一步，写出差异笔记（这就是"原理→框架"的桥）。
