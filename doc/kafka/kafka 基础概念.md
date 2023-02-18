## 安装
1. 部署 zookeeper 服务器
2. 本机安装 jdk 环境
3. download 安装包文件上传至机器，解压缩
4. 修改配置文件 /config/server.properties【broker，listeners，log.dir，zookeeper.connect配置项】
5. 启动，/bin/kafka-server-start.sh -daemon /config/server.properties 
6. 校验是否启动成功（进入zookeeper内部查看是否有kafka的节点）， /brokers/ids/0

## 消息日志文件(/tmp/kafka-logs)
- 0000000.log 消息日志文件
- _consumer_offsets-49 kafka 内部自己创建了_consumer_offsets 主题包含了50个分区，用于存放消费者消费某个主题的偏移量。（确认分区-> hash(consumerGroupId) % partitions）
- 文件保存的消息默认存储7天

## 副本
ISR：可以同步和已经同步的节点的集合，如果 ISR 集合中的节点性能较差会被踢出。 

## 集群消费
- kafka 只在 partition 的范围内保证消息消费的局部顺序性，不能在同一个 topic 的多个 partition 中保证总的消费顺序。
- 一个 consumer group 中的消费者数量不要超过 partition 数量
- consumer 宕机会触发 rebalance 机制

## 缓存区
- buffer.memory 缓存区大小，32M
- batch.size 批处理大小，16KB
- linger.ms 等待时间，10ms

## 长轮询 poll 消息
默认情况下，消费者一次 poll 会拉取500条消息，长轮询时间为1000ms
- 如果一次轮询 poll 到500条消息，结束轮询消费消息
- 如果一次轮询没有 poll 到500条消息，轮询时间没有超过1000ms，继续长轮询poll，直至拉取到500条消息或达到1000ms
- 如果多次轮询未poll到500条消息，且时间达到1000ms，结束轮询消费消息

两次长轮询poll消息的间隔（即消费消息时间）如果超过30s（默认值，max.poll.interval.ms），集群会认定该消费者消费能力较差，会把该消费者踢出ISR中，并触发rebalance。（性能开销）===》可以通过调整poll的消息数量来规避这个问题。（max.poll.records）

## 健康检查
消费者每隔1s（heartbeat.interval.ms）向集群发送心跳，如果超过10s（session.timeout.ms）没有收到心跳，消费者会被消费组踢出，触发rebalance


## controller
kafka 集群中 broker 启动时会向 zookeeper 申请临时序号节点，序号最先的节点将作为集群的 controller，负责整个集群中的所有分区和副本的状态：
- 当某个分区的leader节点宕机，controller会负责为该分区选举新的leader
- 当某个分区的ISR发生变化时，由controller通知所有broker更新其数据信息
- 当使用kafka-topic.sh脚本为某个topic增加分区数量时，由controller负责让新分区被其他节点感知