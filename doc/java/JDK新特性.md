### LTS 长期支持版本一览

Oracle JDK 约每半年一个功能版，其中 LTS（长期支持版）提供数年安全更新，生产环境通常只升级到 LTS：

| LTS  | 发布时间 | 代表特性（一句话）                          |
|------|----------|---------------------------------------------|
| 8    | 2014-03  | Lambda、Stream、函数式接口                  |
| 11   | 2018-09  | var（JDK 10 引入）、HttpClient、String 增强  |
| 17   | 2021-09  | 密封类、Record、文本块、instanceof 模式匹配  |
| 21   | 2023-09  | 虚拟线程、switch 模式匹配、Record 模式       |
| 25   | 2025-09  | 紧凑源文件、作用域值、紧凑对象头             |

以下按 LTS 分节；某特性若在非 LTS 版本正式（如 Record 于 16），仍归入其后第一个 LTS 便于查阅。


### JDK 8 新特性

1. default 接口默认方法（兼容 lambda 表达式）
2. lambda 表达式，lambda 作用域
3. 函数式接口（Function、Predicate、Consumer、Supplier 等）
4. 方法与构造函数引用（`::`）
5. 局部变量访问（effectively final）
6. 访问对象字段与静态变量（lambda 表达式 + 函数式编程）
7. Stream（map/filter/reduce、parallelStream）
8. 多重注解（重复注解、类型注解）
9. Optional（减少 NPE）
10. 日期时间 API（java.time，JSR 310：LocalDate、LocalDateTime、DateTimeFormatter）
11. CompletableFuture（异步编排）
12. Base64 编解码


### JDK 11 新特性（LTS）

1. **var 局部变量类型推断**（JDK 10 引入）：JDK 11 扩展到 lambda 参数（`var x ->`）
2. **HttpClient 标准化**：`java.net.http.HttpClient`，支持 HTTP/2，替代 HttpsURLConnection
3. **String 增强**：`isBlank()`、`strip()`、`lines()`、`repeat()`、`stripLeading()`、`stripTrailing()`
4. **Files 增强**：`Files.readString()`、`Files.writeString()`
5. **Optional 增强**：`or()`、`isEmpty()`
6. **单文件源码直接运行**：`java Hello.java`（无需 javac，JDK 11+）
7. **基于嵌套的访问控制**（JEP 181）：嵌套类之间访问私有成员无需合成桥接方法
8. **ZGC**（实验性，JEP 333）：低延迟垃圾回收器
9. **TLS 1.3** 默认开启
10. 移除 Java EE / Corba 模块，Nashorn 标记废弃（17 移除）


### JDK 17 新特性（LTS）

1. **密封类 sealed**（JEP 409）：限制可继承/实现的子类型，配合模式匹配穷举检查，`sealed / non-sealed / permits`
2. **Record 记录类**（JDK 16 正式，JEP 395）：不可变数据载体，自动生成 equals/hashCode/toString/访问器
3. **instanceof 模式匹配**（JDK 16 正式，JEP 394）：`if (obj instanceof String s)` 免强制转换
4. **文本块**（JDK 15 正式，JEP 378）：`"""` 多行字符串，自动处理换行与缩进
5. **switch 表达式**（JDK 14 正式，JEP 361）：`->` 箭头语法、`yield` 返回值
6. **Helpful NullPointerException**（JDK 14）：NPE 消息指出具体为哪个变量判空失败
7. **强封装 JDK 内部 API**（JEP 403）：默认拒绝反射访问 `sun.misc` 等内部类
8. 移除 Applet API；废弃 Security Manager（21 移除）
9. 重新实现旧版 Socket/SSLSocket；G1、ZGC、Shenandoah 性能持续优化


### JDK 21 新特性（LTS）

1. **虚拟线程 Virtual Threads 正式**（JEP 444，Loom 项目）：`Thread.ofVirtual().start()`、`Executors.newVirtualThreadPerTaskExecutor()`，海量轻量级线程，适合 IO 密集型；线程不再稀缺，阻塞不再是问题
2. **switch 模式匹配正式**（JEP 441）：`case String s`、`case null`、类型模式与解构
3. **Record 模式正式**（JEP 440）：解构 Record 组件，如 `case Point(var x, var y)`
4. **Sequenced Collections**（JEP 431）：新增 `SequencedCollection/SequencedSet/SequencedMap`，统一首尾访问：`getFirst()`、`getLast()`、`reversed()`
5. **未命名变量与模式 `_`**（JEP 443 正式于 22）：占位不关心的组件，`case Point(_, var y)`
6. **String Templates**（预览，JEP 430）：`STR."..."` 插值模板
7. **分代 ZGC**（JEP 439）：`-XX:+UseZGC -XX:+ZGenerational`，降低 GC 停顿与吞吐开销
8. 预览：结构化并发（Structured Concurrency）、作用域值（Scoped Values）
9. 移除 Security Manager；为禁止动态加载 Agent 做准备（JEP 451）


### JDK 25 新特性（LTS）

1. **紧凑源文件与实例 main 方法正式**（JEP 512）：可直接写语句与 `void main()`，无需 public class 包裹，脚本化更简洁
2. **作用域值 Scoped Values 正式**（JEP 506）：线程内/子线程共享不可变数据，替代 ThreadLocal，与虚拟线程、结构化并发配合更省空间与时间
3. **灵活的构造函数体正式**（JEP 513）：`super()`/`this()` 之前可先做参数校验等语句
4. **模块导入声明正式**（JEP 511）：`import module foo;` 一次导入模块导出的所有包
5. **紧凑对象头正式**（JEP 519）：64 位平台对象头从 128 位压到 64 位，显著降低堆占用、提升数据局部性；`-XX:+UseCompactObjectHeaders` 开启（默认关闭）
6. **密钥派生函数 API 正式**（JEP 510）：KDF 标准化，面向后量子混合加密
7. **分代 Shenandoah 正式**（JEP 521）
8. **AOT 优化**（JEP 514/515）：Ahead-of-Time 缓存创建简化 + 方法执行画像随缓存传递，缩短预热与启动时间
9. 预览/孵化：结构化并发（第 5 次预览）、稳定值 Stable Values（预览）、模式中基本类型（第 3 次预览）、Vector API（第 10 次孵化）
10. 移除 32 位 x86 端口（JEP 503）
