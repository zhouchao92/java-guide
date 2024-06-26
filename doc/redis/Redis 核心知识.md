#### NoSQL（Not Only Sql）
不使用sql命令操作数据库软件
目前NoSql不能完全替代关系型数据库，使用关系型数据库结合NoSql数据库进行项目完成
NoSql数据库当作缓存工具来使用

- 把关系数据库中某些**使用频率较高**的的内容，不仅仅存储到关系型数据库，还存到NoSql数据库中
- 要考虑NoSql和关系型数据库之间的**同步**问题

![Redis核心知识脑图](/pic/Redis%20核心知识脑图.jpeg)

#### redis 持久化机制 RDB 和 AOF
RDB：把数据以快照的形式保存在磁盘上。

**RDB的三种触发方式**
save：该命令会阻塞当前 Redis 服务器，执行 save 命令期间，Redis 不能处理其他命令，直到 RDB 过程完成为止。执行完成时候如果存在老的 RDB 文件，就把新的替代掉旧的。我们的客户端可能都是几万或者是几十万，这种方式显然不可取。
![save命令](/pic/Redis%20rdb%20save命令.png)
bgsave：执行该命令时，Redis 会在后台异步进行快照操作，快照同时还可以响应客户端请求，具体操作是Redis 进程执行 fork 操作创建子进程，RDB 持久化过程由子进程负责，完成后自动结束。阻塞只发生在 fork 阶段，一般时间很短。基本上 Redis 内部所有的RDB操作都是采用 bgsave 命令。
![bgsave命令](/pic/Redis%20rdb%20bgsave命令.png)
自动触发：redis.conf 手动配置
![save命令对比bgsave命令](/pic/Redis%20rdb%20save命令对比bgsave命令.png)

①优势
1. RDB 文件紧凑，全量备份，非常适合用于进行备份和灾难恢复。
2. 生成 RDB 文件的时候，redis主进程会 fork 一个子进程来处理所有保存工作，主进程不需要进行任何磁盘 IO 操作。
3. RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快。

②劣势
1. RDB 快照是一次全量备份，存储的是内存数据的二进制序列化形式，存储上非常紧凑。
2. 当进行快照持久化时，会开启一个子进程专门负责快照持久化，子进程会拥有父进程的内存数据，父进程修改内存子进程不会反应出来，所以在快照持久化期间修改的数据不会被保存，可能丢失数据。

**AOF持久化原理**
![AOF运行原理](/pic/Redis%20aof%20运行原理.png)
文件重写原理：为了压缩 aof 的持久化文件，redis 提供了 bgrewriteaof 命令。将内存中的数据以命令的方式保存到临时文件中，同时会 fork 出一条新进程来将文件重写。重写 aof 文件的操作，并没有读取旧的 aof 文件，而是将整个内存中的数据库内容用命令的方式重写了一个新的 aof 文件，这点和快照有点类似。
![AOF文件重写原理](/pic/Redis%20aof%20文件重写原理.png)
**AOF三种触发机制**
（1）每修改同步 always：同步持久化 每次发生数据变更会被立即记录到磁盘 性能较差但数据完整性比较好
（2）每秒同步 everysec：异步操作，每秒记录 如果一秒内宕机，有数据丢失
（3）不同步 no：从不同步
![AOF三种触发机制](/pic/Redis%20aof%20三种触发机制.png)

①优势

1. AOF 可以更好的保护数据不丢失，一般 AOF 会每隔1秒，通过一个后台线程执行一次 fsync 操作，最多丢失1秒钟的数据。
2. AOF 日志文件没有任何磁盘寻址的开销，写入性能非常高，文件不容易破损。
3. AOF 日志文件即使过大的时候，出现后台重写操作，也不会影响客户端的读写。
4. AOF 日志文件的命令通过非常可读的方式进行记录，这个特性非常适合做灾难性的误删除的紧急恢复。比如某人不小心用 flushall 命令清空了所有数据，只要这个时候后台 rewrite 还没有发生，那么就可以立即拷贝 AOF 文件，将最后一条 flushall 命令给删了，然后再将该 AOF 文件放回去，就可以通过恢复机制，自动恢复所有数据。

②劣势

1. 对于同一份数据来说，AOF 日志文件通常比 RDB 数据快照文件更大
2. AOF开启后，支持的写 QPS 会比 RDB 支持的写 QPS 低，因为 AOF 一般会配置成每秒 fsync 一次日志文件，当然，每秒一次 fsync，性能也还是很高的
3. 以前 AOF 发生过 bug，就是通过 AOF 记录的日志，进行数据恢复的时候，没有恢复一模一样的数据出来。

**RDB和AOF到底该如何选择**？
![Redis 持久化策略差异](/pic/Redis%20持久化策略差异.png)

#### Redis 过期键删除策略有哪些？
**过期机制**
在过期精度：

- <= 2.4版本 0~1s
- >= 2.6版本 0~1ms

过期和持久化：时钟校验（时间戳），不是以相对时间作为有效期，而是绝对时间。
淘汰过期 keys 方式：主动和被动，当客户端尝试访问时，key 会被发现并主动地过期。对于有些过期的 keys，不会主动访问，设置 keys 的过期时间，过期的 keys 从密钥空间中删除。
**过期键删除策略**

- 定时删除：在设置键的过期时间的同时，创建一个定时器，让定时器在键的过期时间来临时，立即执行对键的删除操作。
- 惰性删除：放任过期键不管，每次从键空间中获取键时，检查该键是否过期，如果过期，就删除该键，如果没有过期，就返回该键。（被动删除策略）
- 定期删除：每隔一段时间，程序对数据库进行一次检查，删除里面的过期键，至于要删除哪些数据库的哪些过期键，则由算法决定。

[Redis系列(五)：Redis的过期键删除策略 - 申城异乡人 - 博客园](https://www.cnblogs.com/zwwhnly/p/12689792.html)

#### Redis 缓存如何回收，即 Redis 的淘汰策略有哪些？
lru（latest recently used）最近最少使用，如果数据最近被访问过，那么将来被访问的几率也更高
lfu（latest frequently used）最不常使用，如果一个数据在最近一段时间内使用次数很少，那么在将来一段时间内被使用的可能性也很小

- **noeviction 策略**：当 Redis 缓存达到了 maxmemory 配置的值后，再有写入请求到来时，redis 将不再提供写入服务，直接响应错误
- **volatile-ttl 策略**：在筛选时，会针对设置了过期时间的键值对，根据过期时间的先后进行删除，越早过期的越先被删除
- **volatile-random 策略**：在设置了过期时间的键值对中，进行随机删除
- **volatile-lru 策略**：使用 LRU 算法筛选设置了过期时间的键值对
- **volatile-lfu 策略**：使用 LFU 算法选择设置了过期时间的键值对
- **allkeys-random 策略**：从所有键值对中随机选择并删除数据
- **allkeys-lru 策略**：使用 LRU 算法在所有数据中进行筛选
- **allkeys-lfu 策略**：使用 LFU 算法在所有数据中进行筛选

[https://segmentfault.com/a/1190000039705492](https://segmentfault.com/a/1190000039705492)

#### Redis 集群方案有哪些？

- 主从复制集群，手动切换
- 带有哨兵的 HA 的主动复制集群（zookeeper）
- 客户端实现路由索引的分片集群
- 使用中间件代理层的分片集群（解耦）
- redis 自身的 cluster 分片集群

#### Redis 主从复制原理是什么？
全量同步：一般发生在 slave 初始化阶段，slave 需要将 master 上的所有数据都复制一份。
 1）从服务器连接主服务器，发送 SYNC 命令；
 2）主服务器接收到 SYNC 命名后，开始执行 BGSAVE 命令生成 RDB 文件并使用缓冲区记录此后执行的所有写命令； 
 3）主服务器 BGSAVE 执行完后，向所有从服务器发送快照文件，并在发送期间继续记录被执行的写命令； 
 4）从服务器收到快照文件后丢弃所有旧数据，载入收到的快照； 
 5）主服务器快照发送完毕后开始向从服务器发送缓冲区中的写命令；
 6）从服务器完成对快照的载入，开始接收命令请求，并执行来自主服务器缓冲区的写命令； 
![Redis 主从复制原理](/pic/Redis%20主从复制原理.png)

增量同步：Redis 增量复制是指 slave 初始化后开始正常工作时主服务器发生的写操作同步到从服务器的过程。 
增量复制的过程主要是主服务器每执行一个写命令就会向从服务器发送相同的写命令，从服务器接收并执行收到的写命令。
**Redis主从同步策略**
主从刚刚连接的时候，进行全量同步；全量同步结束后，进行增量同步。当然，如果有需要，slave 在任何时候都可以发起全量同步。redis 策略是，无论如何，首先会尝试进行增量同步，如不成功，要求从机进行全量同步。
**注意点**
如果多个 slave 断线了，需要重启的时候，因为只要 slave 启动，就会发送 sync 请求和主机全量同步，当多个同时出现的时候，可能会导致 master IO 剧增宕机。 

