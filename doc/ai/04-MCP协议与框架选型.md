# 四、MCP 协议与框架选型

本文是学习路线的阶段四（第 5–6 周）：掌握 MCP/A2A 协议，建立 Spring AI 主线能力，并用 LangGraph 对照理解另一种编排范式。核心思想：**协议让工具可复用，框架让工程可维护**。


## 一、MCP（Model Context Protocol）

### 1.1 定位

MCP 是模型与外部系统之间的**开放标准协议**（由 Anthropic 发起，现为跨厂商通用标准）。解决的问题：过去每个模型厂商 × 每个工具各写一套集成（M×N），MCP 把它变成 M+N——工具方实现一次 MCP Server，任何支持 MCP 的客户端都能用。

```
MCP Client（宿主应用：IDE/Agent 框架/桌面应用）
        ↕  MCP 协议（stdio / HTTP 传输）
MCP Server（封装一个系统：数据库/文件系统/Jira/监控平台…）
        ↕
后端真实系统
```

### 1.2 三大原语

| 原语          | 方向                     | 作用                                               | 类比     |
|---------------|--------------------------|----------------------------------------------------|----------|
| **Tools**     | 模型调用 → Server 执行   | 有副作用的操作/查询                                | 函数调用 |
| **Resources** | 客户端读取 → Server 提供 | 上下文数据源（文件、记录），由应用而非模型决定加载 | REST GET |
| **Prompts**   | 用户触发 → Server 提供   | 预置的提示词模板/工作流入口                        | 快捷指令 |

补充：**Tools 是给模型的，Resources 是给应用的**——这个边界是 MCP 设计的精髓，避免模型乱拉数据。

### 1.3 协议交互流程

```
1. initialize  握手（协议版本、能力协商）
2. initialized 确认
3. tools/list  客户端拉取工具清单（name/description/schema）
4. tools/call  模型发起调用 → Server 执行 → 返回结果（文本/结构化）
5. 会话期间可双向通知（资源变更、进度上报）
```

传输方式：**stdio**（本地子进程，个人工具）与 **Streamable HTTP**（远程服务，企业部署；取代早期 SSE）。

### 1.4 安全与权限边界（企业级重点）

- **最小权限**：Server 只暴露必要工具，一个 MCP Server 只管一个系统；
- **鉴权**：HTTP 传输走 OAuth 2.1 / Bearer Token，Server 侧校验调用方身份；
- **工具即攻击面**：模型可能被诱导调用危险工具 → 高危工具必须配确认闸（见 02 册 HITL）；
- **提示注入链**：工具返回的内容是**不可信输入**，回填上下文前要视作数据而非指令；
- **审计**：谁（哪个会话/租户）调了什么工具、传了什么参数、结果如何，全量落日志。

### 1.5 自写 MCP Server 实战步骤（本阶段产出）

1. 用官方 SDK（Java SDK 或 Python FastMCP）封装一个内部系统，例如"告警查询"或"变更单查询"；
2. 定义 2~4 个 tools + 1~2 个 resources，**写好 description**（模型靠它选工具）；
3. 用 MCP Inspector（官方调试器）联调；
4. 接入宿主（Spring AI MCP Client / OpenCode 等）实测；
5. 补：鉴权、限流、审计日志。


## 二、A2A（Agent-to-Agent Protocol）

- 解决 **Agent 之间**的互操作（MCP 解决 Agent 与工具之间），由 Google 发起、多厂商参与，与 MCP 互补而非竞争。
- 核心概念：**Agent Card**（`/.well-known/agent.json`，声明能力/端点/认证方式，类似"Agent 的 API 文档"）、**Task**（生命周期：submitted → working → input-required → completed/failed）、消息流（支持流式更新与人工介入节点）。
- 学习建议：**了解概念与任务生命周期即可**，当前企业落地仍以单 Agent + MCP 为主；多组织、跨厂商 Agent 协作需求出现时再深入。


## 三、Spring AI 主线（Java 企业级）

### 3.1 核心抽象

| 抽象                     | 作用                                     | 对应理解                          |
|--------------------------|------------------------------------------|-----------------------------------|
| `ChatClient`             | 流式/同步调用的门面，链式 API            | 手写 AgentLoop 的第 1 步          |
| `ChatModel`              | 底层模型适配（OpenAI/Anthropic/Ollama…） | 厂商无关层                        |
| `Advisor`                | 调用拦截链：日志、记忆、RAG 注入、护栏   | 类比 Spring 的 Filter/Interceptor |
| `@Tool` / `ToolCallback` | 把 Java 方法暴露给模型                   | 手写 AgentLoop 的工具注册表       |
| `VectorStore`            | 向量库统一抽象（pgvector/Redis/ES…）     | 03 册检索管道的落点               |
| `ChatMemory`             | 对话记忆持久化                           | 上下文工程的会话层                |
| MCP Client/Server        | 协议接入                                 | 本册第一节                        |
| Observation / OTel       | 可观测埋点                               | 05 册                             |

### 3.2 与手写 AgentLoop 的映射（学完 02 册后对照）

```
手写版本                     Spring AI
─────────────────────────────────────────────
messages 列表组装      →     ChatClient + ChatMemory
工具注册与描述         →     @Tool 方法 + ToolCallbackProvider
循环控制/终止条件      →     Agent 模块 / 自写循环（框架不替你决定策略）
结果校验与重试         →     OutputConverter + 重试模板
拦截扩展（日志/护栏）  →     Advisor 链
```

**重要认知**：Spring AI 提供的是**构件**，不是完整 Agent 运行时——循环策略、终止条件、审批闸、评估这些企业级决策仍由你设计（这正是 02、05 册的价值）。

### 3.3 典型组合用法

- **RAG**：`ChatClient` + `Advisor`（如 QuestionAnswerAdvisor）+ `VectorStore`；
- **工具调用**：`@Tool` 方法注入 → 模型多轮调用；
- **MCP**：`McpClient` 连远程 Server，工具自动进入工具清单；
- **结构化输出**：`BeanOutputConverter`（01 册实验 1）。


## 四、LangGraph 对照（Python 生态）

Java 主线之外，理解行业事实标准的编排范式：

| 概念       | 含义                                    | 价值                              |
|------------|-----------------------------------------|-----------------------------------|
| State      | 全局状态对象（可定义 reducer 合并规则） | 状态是显式、可序列化的            |
| Node       | 执行单元（LLM 调用/工具/纯函数）        | 节点只读写 State                  |
| Edge       | 状态流转；**条件边**按 State 决定下一步 | 循环/分支显式建模                 |
| Checkpoint | 状态快照持久化                          | 断点续跑、时间旅行、HITL 中断恢复 |

对照结论：
- LangGraph 把 Agent 建成**状态机**，Java 生态对应思路是 Spring Statemachine / 自写编排 + 状态落库；
- **Checkpoint + interrupt 是 HITL 的标准实现**，你在 02 册手写时会发现同样的需求；
- 学习定位：借 LangGraph 文档学"编排词汇表"，不必转栈。


## 五、框架选型建议

| 场景                             | 建议                                               |
|----------------------------------|----------------------------------------------------|
| 企业 Java 后端、已有 Spring 体系 | **Spring AI 主线**，循环与策略自写                 |
| 研究性质、快速原型、Python 团队  | LangGraph                                          |
| 极简场景（单轮工具）             | 不用框架，手写（02 册能力）                        |
| 多智能体复杂编排                 | Spring AI + 自写 supervisor，或引入 LangGraph 思路 |

警惕：框架换代快、API 不稳定——**把能力沉淀在自己的抽象层**（Advisor/自定义接口），模型与框架都可替换。


## 六、学习资源（阶段四）

### 必读（按顺序）

1. **MCP 官方文档（Model Context Protocol）**——https://modelcontextprotocol.io/ ：先读《Intro》与《Get Started》跑通 demo，再读《Concepts》（Tools/Resources/Prompts）、《Specs》传输与安全章节。
2. **MCP 官方规范仓库**——https://github.com/modelcontextprotocol/modelcontextprotocol （schema 定义、版本演进、安全最佳实践 `SECURITY.md`）。
3. **Spring AI 官方文档**——https://docs.spring.io/spring-ai/reference/ ：依次读 ChatClient、Tools、RAG/Advisors、MCP 章节（本栈主线，边读边写代码）。
4. **LangGraph 官方《Concepts》**——https://langchain-ai.github.io/langgraph/concepts/ （State/Node/Edge/Checkpoint + HITL interrupt 页面）。

### 选读

- **Spring AI 官方示例仓库**——https://github.com/spring-projects/spring-ai （chatbot、tool calling、RAG、MCP 示例，直接抄结构）。
- **MCP Inspector**——https://github.com/modelcontextprotocol/inspector （官方协议调试器，写 Server 必备）。
- **A2A 协议官网**——https://a2a-protocol.org/ （Agent Card 与 Task 生命周期）。
- **论文《A Survey on the Optimization of Large Language Model-based Agents》/ Agent 编排综述**——了解 planner/executor 模式谱系。
- **Anthropic 文章《Introducing the Model Context Protocol》**——MCP 设计动机的原始阐述。

### 动手实践（第 5–6 周产出）

1. **第 5 周**：跑通 MCP Inspector + 一个官方示例 Server；用 Spring AI `@Tool` 重写 02 册的 AgentLoop 工具层；读 LangGraph 概念文档并画出"条件边 + checkpoint"的状态机图。
2. **第 6 周**：**自研 MCP Server**（告警查询或变更单查询，见 08 分册项目一/二），补鉴权与审计日志；用 MCP Client 接入 Spring AI 应用，形成"模型 → MCP → 内部系统"的完整链路。
