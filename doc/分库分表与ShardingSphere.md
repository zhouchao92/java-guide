## 分库分表的作用是什么？
- 数据体量大
- 数据生产速度快
- 数据访问频繁

![数据库产品转型方案](/pic/%E6%95%B0%E6%8D%AE%E5%BA%93%E4%BA%A7%E5%93%81%E8%BD%AC%E5%9E%8B%E6%96%B9%E6%A1%88.png)

## 垂直分片与水平分片
垂直分片：按照业务维度将表拆分到不同的数据库中，专库专用，分担数据库压力  
水平分片：按某种规则将单表数据拆分到多张表中  

## 如何设计分片数据方案？
分库分表策略：
1. 取模分片，<font color=00aff0>数据存放比较均匀</font>，<font color=red>扩容需要大量数据迁移</font>
2. 范围分片，<font color=00aff0>扩容不需要迁移数据</font>，<font color=red>数据存放不均匀，容易产生数据倾斜</font>
3. 业务场景灵活定制分片策略

### 如何不迁移数据，实现集群动态扩缩容，同时又能保证数据分布相对均匀？
1. 整体按范围分片，保证扩容时老数据不需要迁移；
2. 范围内，按照取模分片，让每个范围内数据分布大致均匀分布

## 分库分表需要解决的问题？
- 主键唯一性
- 分布式事务
- SQL 路由
- 结果归并

## 什么时候需要分库分表？
- 预估数据量，阿里开发手册建议预估三年内<font color=00aff0>单表数据量大于500万</font>或者<font color=00aff0>单表数据文件大于2G</font>，需要考虑分库分表。
- 预估数据趋势，订单类持续高速增长的数据需要尽早考虑分库分表，并预留空间，用户类后期增长会放缓的数据，可以延后考虑分库分表。
- 预估应用场景，<font color =red>由于频繁变更分片键，需要同时做数据迁移</font>，所以对于分片键变更频繁的数据，不适合进行分库分表。
- 预估业务复杂度，<font color =red>业务逻辑与分片逻辑绑定，会给SQL执行带来很多限制</font>，如果对数据的查询逻辑变化非常大，不建议分库分表。

## 分库分表与多数据源切换
分库分表不仅是管理多个数据源，还有对SQL的优化、改写、归并等一系列的解决方案，关注的是SQL语句；

多数据源切换仅切换多个数据源。

### 多数据源切换方案
- AbstractRoutingDataSource/AbstractDataSource
- DynamicDataSource

## ShardingSphere

## Inline 策略
根据单一分片键进行精确分片

例：
- 库m1,m2 表course_1,course_2
- 分片键 cid 
- 分片算法，`m$->{cid%2+1}.course_$->{((cid+1)%4).intdiv(2)+1}`

<font color=red>不支持范围查询</font>

## Standard 策略
根据<font color = 00aff0>单一分片键</font>进行精确或范围分片（指定两种分片表达式）

## Complex 策略
根据<font color = 00aff0>多个分片键</font>进行精确或范围分片

## Hint策略
与SQL语句无关的分片策略（强行指定）

## ShardingSphere的三个坑
1. 会忽略 schema（库.表 ==> 表）
2. union 不会对第二个虚拟表进行转换
3. 不支持 having 语法

## 新版本规则 5.x
可拔插（策略的组合）

## ShardingSphere 扩展接口
ShardingKeyGenerator + Java SPI 机制（classpath:/META-INF/services/类全路径名）

## 零迁移数据扩容与基因法分片【HOT】
零迁移数据扩容方案
![零迁移数据扩容方案](/pic/%E9%9B%B6%E8%BF%81%E7%A7%BB%E6%95%B0%E6%8D%AE%E6%89%A9%E5%AE%B9%E6%96%B9%E6%A1%88.png)


基因法分片：支持多个分片键提高查询效率
![基因法分片](/pic/%E5%9F%BA%E5%9B%A0%E6%B3%95%E5%A4%9A%E5%88%86%E7%89%87%E6%9F%A5%E8%AF%A2.png)

## 读写分离与分库分表
读写分离在程序上只能做到将读和写的请求分开，实际的读库和写库的数据同步需要依赖数据库主从同步的方案实现（以MySQL的主从同步为例）

1. 写请求-->主库（一主多从）
2. 从库订阅主库的 binlog，主库发生数据变更时，从库 io 线程写入 relaylog
3. 从库 sql 线程执行 relaylog，从库数据实现同步

其他方案：
- 数据副本，基于Zookeeper实现，节点将写入数据日志提交到Zookeeper，其他节点通过Zookeeper收到写入的日志
- 分布式表 ===> 分片
