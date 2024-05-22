## HW 与 LEO
```plaintext
----------------------------
|        LEO               | 待写入数据
----------------------------
|                          | 不能被消费
|        HW                | 
----------------------------
| /    /     /     /     / | consumer 可见 
|_/_firstUnstableOffset _/_| 第一条未提交的数据
|    LastStableOffset      | 最后一条已提交的数据
|                          |
|     LogStartOffset       | 起始位置
|--------------------------|
```
HW：Hig hWater Mark，LEO：Log End Offset


## isolution.level = ?
- read_uncommitted 只能消费到 LastStableOffset
- read_committed 可以消费到 HW 的上一条

## 集群中的HW与LEO
一个 partition 对应的 ISR 中最小的 LEO 作为分区的 HW，consumer 最多只能消费到 HW 所在的位置。

新消息写入时，leader会等待ISR中所有节点同步HW才会更新HW，消费者才能消费到此消息。

## 副本同步？
leader 收到消息后更新本地的 LEO，leader 还会维护 follower 的 LEO 即 remote LEO，follower 发出 fetch 同步数据（携带自身的 LEO）、leader 会更新 remote LEO，更新分区 HW，然后将响应数据给 follower，follower 更新自身 HW（取响应中的 HW 和自身 LEO 中的较小值），LEO + 1

ISR：如果一个 follower 落后 leader 不超过某个时间阀值，则放在 ISR 中，否则放在 OSR 中

同步副本时，follower 获取 leader 的 LEO 和 LogStartOffset，与本地对比、如果本地的 LogStartOffset 超出了 leader 的值，则超出这个值的数据删除，再进行同步，如果本地的小于 leader 的值，则直接同步
