获取链接，获取执行引擎，sql解析，sql优化

1.innodb 在收到一个 update 语句，会根据条件查询找到数据所在的页，并将该页缓存到 Buffer Pool 中
2.执行 update 语句，修改 Buffer Pool 中的数据
3.针对 update 语句生成 redolog 对象，并存入 LogBuffer
4.针对 update 语句生成 undolog日志
5.事务提交->将 redolog 对象持久化，（其他机制）将 Buffer Pool 中的缓存数据持久化到磁盘中
  事务回滚->根据 undolog 日志进行回滚