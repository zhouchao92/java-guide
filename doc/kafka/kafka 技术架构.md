consumer group
topic
partition：一个 topic 以多个 partition 分布在 多个 broker 上，每个 partition 都是有序的队列。一个 topic 的每个 partition 都有若干个副本 replica（一个 leader + 多个 follower）。生产者发送数据的对象以及消费者消费数据的对象都是 leader，follower 负责实时从 leader 同步数据，leader 发生故障时，某个 follower 可以成为 leader
【主从==>主备】


zookeeper 集群管理 leader 节点