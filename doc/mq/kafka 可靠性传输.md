producer ---> broker:
参数配置 request.required.acks
1 默认，只确认 leader，leader 宕机会丢数据
0 不确认
-1 所有 follower 都确认，可靠性最高，极端情况下可能存在 ISR 中只存在一个 leader 存活，相当于 acks = 1 的情况

producer 发送 send 有对应的回调函数，onFailure() 重试

min.insync.replicas ISR 集合中最小副本个数，当且仅当 request.required.acks = -1 时生效，默认值为 1，如果 ISR 副本数少于配置的数量时，客户端会返回异常


consumer 消费者消费消息时，手动 ack
