### 应用场景
- 应用解耦
- 流量削峰
- 大数据处理
- 异构系统

### 消息的发送方式

1. 同步消息

阻塞，等待服务器响应

2. 异步消息，回调处理消息发送结果

不会阻塞，等待broker的确认
采用事件监听接受broker返回的确认

3. 单向消息 oneway message

单向消息发送，不等待服务器响应
有可能丢消息，网络不确定

### 批量消息发送(≤1MB)

1. 具有同一topic，消息配置
2. 不支持延时消息
3. 建议一个批量消息不要超过1MB
4. 如果不确定是否超过限制，可以手动计算大小分批发送

### 消息消费过滤

- TAG
- SQL：需要开启配置 _enablePropertyFilter = true_

### 消息的消费模式
#### 集群模式（default）MessageModel.CLUSTERING
集群模式下，MQ认为任意一条消息只需要被集群内任意一个消费者处理即可

1. 每条消息只需要被处理一次，broker只会把消息发送给消费集群中的一个消费者
2. 在消息重投时，不能保证路由到同一台机器上
3. 消费状态由broker维护

#### 广播模式 MessageModel.BROADCASTING
广播模式下，MQ会将每条消息推送给集群内所有注册过的消费者，确保每一条消息至少被每台机器消费一次

1. 消费进度由消费者维护
2. 确保每个消费者消费一次
3. 消费失败的消息不会重投

### 事务
#### 分布式消息事务
2pc（two-phase commit protocol 两阶段提交）RocketMQ

1. _尝试提交（数据状态：不可用）_
2. _二次确认（数据状态：可用）_

3pc（three-phase commit protocol 三阶段提交）

1. _can commit_
2. _pre commit_
3. _do commit_

tcc（try confirm cancel 补偿事务）

1. _try阶段：对业务系统做检测及资源预留，锁定数据_
2. _confirm阶段：确认执行业务操作_
3. _cancel阶段：取消执行业务操作_

#### RocketMQ消息事务
Half Message：预处理消息，当broker收到此类消息后，存储到RMQ_SYS_TRANS_TOPIC的消息队列中
检查事务状态：broker会启动一个定时任务，消费RMQ_SYS_TRANS_TOPIC队列中的消息，每次执行任务会向消息发送者确认事务提交状态（提交/回滚/未知），如果是未知，等待下一次回查
超时：如果超过回查次数，默认回滚消息
![Half Message](/pic/RocketMQ%20Half%20Message.jpeg)

### 消息重投机制
_ConsumeConcurrentlyStatus.RECONSUME_LATER 消费者稍后再处理消息_
_消费超时_

#### 消息重复消费（幂等性）？
影响消息正常发送和消费的重要原因是网络的不确定性

引起重复消费的原因
1. ACK，正常情况consumer在真正消费完消息后应该发送ack，通知broker该消息已正常消费，从队列中剔除，当ack因为网络原因无法发送到broker，broker会认为此条消息没有被消费，此后会开启消息的重投机制把消息再次投递给consumer
2. group，在集群模式下，消息在broker中保证相同的group的consumer消费一次，但是针对不同的group的consumer会推送多次

解决方案
1. 数据库表，在处理消息前，使用消息主键在表中带有约束的字段 insert
2. Map，单机时可采用 ConcurrentHashMap # putIfAbsent (K key, V value)
3. Redis，使用主键或set操作

### 如何保证消息顺序消费
MessageQueue 保证顺序数据结构（FIFO）
Producer
- 同一topic、同一queue、同一线程发送消息

Consumer
- 一个线程消费一个queue的消息
- 多个queue只能保证单个queue里的顺序
```java
// 生产者
DefaultMQProducer producer = new DefaultMQProducer(group);
producer.setNamesrvAddr(namesrv);

producer.start();

Message message = new Message(topic, "order message".getBytes());
/*
 * MessageQueue 保证顺序数据结构（FIFO）
 * 同一topic、同一queue、同一线程
 */
producer.send(message, new MessageQueueSelector() {
    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        // 手动选择queue
        return mqs.get((Integer) arg);
    }
}, 0, 2000);


// 消费者
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test-order-customer");
consumer.setNamesrvAddr(namesrv);
consumer.subscribe(topic, "*");
consumer.setConsumeThreadMin(1);
consumer.setConsumeThreadMax(1);
/*
 * MessageListenerOrderly 顺序消费，对一个queue开启一个线程，多个queue开启多个线程
 */
consumer.registerMessageListener((MessageListenerOrderly) (list, consumeOrderlyContext) -> {
    for (MessageExt messageExt : list) {
        String message = new String(messageExt.getBody());
        log.info("message content is {}", message);
    }
    return ConsumeOrderlyStatus.SUCCESS;
});
consumer.start();
```
![Queue Selector](/pic/RocketMQ%20Queue%20Selector.jpeg)

### 深入学习
![RocketMQ Pull 模式](/pic/RocketMQ%20pull%20模式.jpeg)

### 偏移量offset
- offset 每个broker中的queue在收到消息时会记录offset，初始值为0，每记录一条消息递增1
- minOffset 最小值
- maxOffset 最大值
- consumerOffset 消费者消费进度
- diffTotal 消费积压/未被消费的消息数量

### 长轮询机制
三种连接方式
- 短轮询：客户端不断发送请求到服务端，每次都需要重新连接
- 长轮询：客户端发送请求到服务端，服务端有数据返回，没有数据挂起等待数据返回（空报文）
- 长连接：连接一旦建立，永不断开（push方式）

consumer -> broker 在RocketMQ中采用长轮询建立连接

- consumer的处理能力，broker未知
- 直接推送消息broker端压力太大，需要维护consumer的状态
- 采用长连接有可能consumer不能及时处理推送过来的数据
- pull主动权在consumer手中

