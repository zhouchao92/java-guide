#### MVCC（Multi-Vesion Concurrent Control 多版本并发控制）解决的问题是什么？
读写：可能造成事务的隔离性问题，脏读、幻读、不可重复读  
写写：可能存在更新丢失问题

MVCC是一种用来解决读写冲突的无锁并发控制，为事务分配单项增长的时间戳，为每个修改保存一个 version，版本与事务时间戳相关，读操作只读该事务前的数据库快照。

- 读写不需要阻塞数据库
- 解决脏读、幻读、不可重复读，不能解决更新丢失问题

#### MVCC实现原理
主要依赖于记录中的三个隐藏字段，undolog，read view 来实现的。

三个隐藏字段：

- DB_TRX_ID：6字节，最近修改事务id，记录创建这条记录或者最后一次修改记录的事务id
- ROLL_PTR：7字节，回滚指针，指向这条记录的上一个版本，用于配合 undolog，指向上一个旧版本
- ROW_ID ：6字节，隐藏的主键，如果数据表没有主键，innodb会自动生成

undolog：回滚日志，在进行数据修改操作的时候方便回滚的日志

- insert，事务提交后丢弃
- update 和 delete，产生的日志需要在快照读的需要访问到，不能随意删除，只有在快照读或事务回滚不涉及该日志时，对应的日志才会被 purge 线程统一清除（当数据发生更新和删除操作时只设置老记录的 deleted_bit，并不是真正的将过时的记录删除，为了节省磁盘空间，innodb 有专门的 purge 线程来清除 deleted_bit 为 true 的记录，如果某个记录的 deleted_bit = true 并且 DB_TRX_ID 相对于 purge 线程的 read view 可见，则这条记录一定时可以被清除的）

read view：事务进行快照读操作产生的读视图，记录并维护系统当前活跃事务的 id （事务 id 值是递增的）  
可见性算法：将被修改的数据的最新记录中的 DB_TRX_ID 对比其他活跃事务的 id，如果 DB_TRX_ID 和 read view 的属性不符合可见性，通过 DB_ROLL_PTR 回滚指针取出 undolog 中的 DB_TRX_ID 对应的旧记录

read view 可见性规则  
- trx_list 数值列表，维护 read view 生成时刻系统活跃的事务 id  
- up_limit_id 记录 trx_list 最小 id  
- low_limit_id read view 生成时系统尚未分配的下一个事务 id  
![MVCC(Read View)](/pic/MVCC(Read%20View).jpeg)  


RC、RR 级别下 innodb 的快照读不同点：
1. 在 RR 级别下，某个事务的对某条记录的快照读会创建一个快照，将当前系统活跃的其他事务记录起来，此后在调用快照读时，还是使用同一个 read view，即只要当前事务在其他事务提交更新前使用过快照读，那么之后的快照读使用都是同一个 read view，所以对之后的修改不可见
2. 在 RC 级别下，每次快照读都会生成新的快照和 read view，因此在事务中可见其他事务提交的更新
