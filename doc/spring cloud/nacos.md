nacos 拉取多个配置
1. spring.cloud.nacos.config.shared-configs[0].data-id=?
2. spring.cloud.nacos.config.extension-configs[0].data-id=?


@RefreshScope 配置自动刷新的原理：原型模式，多例 Bean，在刷新后生成新的 Bean


临时实例：默认服务注册到 nacos 都是临时实例，客户端每隔5s向服务端发送一次心跳，如果超过15s没有收到客户端的心跳，会将其状态修改为非健康实例，如果30s没有收到客户端心跳，会将服务剔除
持久化实例：如果超过30s没有收到客户端心跳，不会将服务剔除

阈值保护，持久化实例可以保证少数存活的服务不会被压垮


Cluster 集群就近访问