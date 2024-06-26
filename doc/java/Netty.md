#### 零拷贝是什么？
零拷贝技术就是一种避免CPU将数据从一块存储拷贝到另外一块存储的技术，在数据考虑的同时，允许CPU执行其他任务来实现。即减少数据拷贝和共享总线操作的次数，消除传输数据在存储器之间不必要的中间拷贝次数，从而有效的提高数据传输效率。（在传输文件时，不需要将文件内容拷贝到用户空间，而是直接在内核空间中传输到网络的方式，避免了用户空间和内存空间之间的拷贝，从而提升了系统的整体性能。）

#### Netty 的线程模型是怎么样的？
同时支持 Reactor 单线程模型、Reactor 多线程模型和 Reactor 主从多线程模型，可根据启动参数进行切换

服务端启动时，通常会创建两个 NioEventLoopGroup 实例，对应了两个独立的 Reactor 线程池，bossGroup 负责处理客户端请求的连接，workerGroup 负责处理 IO相关的操作、执行系统 Task、定时任务 Task。用户可根据服务端引导类 ServerBootstrap 配置参数选择 Reactor 线程模型，进而最大限度地满足用户地定制化需求。
![Netty线程模型](/pic/Netty线程模型.png)

#### 高性能体现在哪些方面？
- nio模型，用最少的资源做更多的事情
- 内存零拷贝
- 内存池设计，申请的内存可以重用，主要是指直接内存，内部实现是用一颗二叉查找树管理分配内存情况
- 无锁串行化处理读写
- 高性能序列化协议，protobuf 协议
- 高性能并发编程，volatile，cas 和原子类，线程安全的容器
