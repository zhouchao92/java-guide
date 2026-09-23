#### 零拷贝是什么？

零拷贝技术是一种避免 CPU 将数据从一块存储拷贝到另一块存储的技术，减少数据拷贝次数，消除传输数据在存储器之间不必要的中间拷贝，从而提高数据传输效率。

传统 IO 读写文件再发送到网络（`read` + `write`）：文件 → 内核读缓冲区 → 用户缓冲区 → 内核发送缓冲区 → 网卡，共 4 次拷贝、4 次上下文切换；Linux 下借助 `sendfile` 可让数据直接在内核空间从文件传输到网卡，避免拷贝到用户空间。

Netty 中零拷贝的体现：

- **文件传输**：`FileRegion`（`DefaultFileRegion`）底层调用 `transferTo`/`sendfile`，文件内容不经过用户空间
- **CompositeByteBuf**：将多个 ByteBuf 逻辑上合并为一个，避免内存拷贝
- **ByteBuf 切片**：`slice`/`retainedSlice` 返回缓冲区的视图，共享底层内存而非复制数据
- **消息体读写**：`ByteBuf` 支持按需扩容与池化复用，减少无谓的内存分配拷贝

#### Netty 的线程模型是怎么样的？

同时支持 Reactor 单线程模型、Reactor 多线程模型和 Reactor 主从多线程模型，可根据启动参数进行切换。

| 模型               | 说明                                                             | 缺陷                           |
|--------------------|------------------------------------------------------------------|--------------------------------|
| Reactor 单线程     | 一个线程处理 accept、读写与业务逻辑                              | 单线程成为瓶颈，无法应对高并发 |
| Reactor 多线程     | accept 由单线程完成，读写与业务交给多个工作线程                  | accept 单线程仍是瓶颈          |
| Reactor 主从多线程 | 主线程池只负责接收连接（accept），从线程池负责 IO 读写与业务处理 | —                              |

服务端启动时，通常会创建两个 `NioEventLoopGroup` 实例，对应了两个独立的 Reactor 线程池：

- **bossGroup**：负责处理客户端请求的连接（accept）
- **workerGroup**：负责处理 IO 相关的操作、执行系统 Task、定时任务 Task

用户可根据服务端引导类 `ServerBootstrap` 配置参数选择 Reactor 线程模型（`EventLoopGroup` 即单线程模型，一个 boss + 一个 worker 即多线程模型，默认 boss 1 线程 + worker 2×CPU 线程即主从模型）。

核心规则（one loop per thread）：

- 每个 `EventLoop` 持有一个 Selector，一个线程绑定多个 Channel，事件在其内串行处理
- 一个 Channel 生命周期内绑定固定一个 EventLoop，不会发生线程切换
- EventLoop 单线程串行执行，避免锁竞争；业务 Handler 若阻塞 EventLoop 会拖垮该线程上所有 Channel，耗时业务应交给独立线程池

![Netty线程模型](/pic/Netty线程模型.png)

#### 高性能体现在哪些方面？

- **NIO 多路复用模型**：基于 epoll 的 Selector，一个线程管理大量连接，用最少的资源做更多的事情
- **内存零拷贝**：文件传输用 `FileRegion` 走 sendfile，`CompositeByteBuf`、`slice` 避免数据复制
- **内存池设计**：申请的内存可以重用，主要是指直接内存（DirectByteBuf），内部实现是用一颗二叉查找树管理分配内存情况，减少 GC 压力
- **无锁串行化处理读写**：每个 EventLoop 单线程串行处理自己的 Channel，避免锁竞争；多 EventLoop 间按 Channel 分组，写入不加锁
- **高性能序列化协议**：protobuf 协议，体积小、解析快
- **高性能并发编程**：volatile、CAS 和原子类、线程安全的容器
- **可扩展的流水线**：`ChannelPipeline` + `ChannelHandler` 责任链，编解码、限流等逻辑即插即用
- **TCP 参数可配置**：`SO_BACKLOG`、`TCP_NODELAY`、读写水位线（`WRITE_BUFFER_WATER_MARK`）等按场景调优

#### 粘包与拆包是什么？怎么解决？

TCP 是面向字节流的，不保留消息边界：发送方多次 write 的数据可能被合并（粘包），一次发送的数据也可能被拆开（拆包）。

常见解决方案：

| 方案     | 说明                                       | Netty 组件                     |
|----------|--------------------------------------------|--------------------------------|
| 固定长度 | 每条消息固定字节数，不足等待               | `FixedLengthFrameDecoder`      |
| 分隔符   | 以特定分隔符结束一条消息                   | `DelimiterBasedFrameDecoder`   |
| 长度域   | 消息头声明消息体长度，按长度读取（最常用） | `LengthFieldBasedFrameDecoder` |

#### ByteBuf 和 ByteBuffer 的区别？

|          | ByteBuffer（JDK）                            | ByteBuf（Netty）                           |
|----------|----------------------------------------------|--------------------------------------------|
| 读写指针 | 单一 position，读写切换需 `flip()`/`clear()` | 独立 readerIndex、writerIndex，无需 flip   |
| 扩容     | 容量固定，需手动分配                         | 自动扩容                                   |
| 内存     | 堆内存/直接内存                              | 堆内存/直接内存 + **池化复用**             |
| 拷贝     | `copy()` 独立副本                            | `slice`/`duplicate` 可共享底层内存，零拷贝 |
