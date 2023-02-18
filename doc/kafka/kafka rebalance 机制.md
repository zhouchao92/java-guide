consumer group 消费者与 partition 重新匹配的过程

前提：consumer没有指定partition消费

三种策略：
- range（partitions/consumers）
- 轮询
- sticky 保持原分区状态的基础上进行分配

rebalance 时机：
- consumer group 成员个数发生变化
- consumer 消费超时
- group 订阅的 topic 个数发生变化
- group 订阅的 topic 的分区数发生变化


coordinator（协调者）：通常是 partition 的 leader 节点所在的 broker，负责监控 group 中 consumer 的存活，consumer 维持到 coordinator 的心跳，判断 consumer 的消费超时
- coordinator 通过心跳返回通知 consumer 进行 rebalance
- consumer 请求 coordinator 加入组，coordinator 选举产生 consumer leader
- leader consumer 从 coordinator 获取所有的 consumer，发送 syncGroup 给到 coordinator
- coordinator 通过心跳机制将 syncGroup 下发到 consumer
- 完成 rebalance

leader consumer 监听 topic 的变化，通知 coordinator 发送 rebalance

如果 consumer 消费超时，触发 rebalance，重新分配后，该消息会被其他消费者消费，若原 consumer 提交 offset 会导致错误
===> 乐观锁，coordinator 在 rebalance 时标记 generation 给到 consumer，每次重新 rebalance 后 +1，在 consumer 提交时对比 generation，不一致时拒绝提交