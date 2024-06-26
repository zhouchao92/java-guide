#### synchronized 的锁升级过程？
无锁状态，当第一个线程获取到锁时，升级为偏向锁并记录线程ID，在下一次线程请求锁时，会偏向于这个线程，当第二个线程获取锁时(存在竞争)，先撤销成无锁状态再升级为轻量级锁，并保持自旋，当自旋到一定次数或者有其他线程竞争锁时，升级为重量级锁，阻塞线程。
[Synchronized锁升级：无锁-＞ 偏向锁 -＞ 轻量级锁 -＞ 重量级锁_枫陵的博客-CSDN博客_java锁的升级](https://blog.csdn.net/weixin_43899792/article/details/124634419)

#### synchronized 和 Lock 的区别？
|              | synchronized                                                         | Lock                                          |
|--------------|----------------------------------------------------------------------|-----------------------------------------------|
| 存在层次     | 关键字，JVM 层                                                       | Java 接口                                     |
| 锁的获取     | 假设线程 A 持有锁，线程 B 等待，如果线程 A 阻塞，线程 B 会一直等待   | 视情况而定，可以通过 tryLock() 尝试获取锁     |
| 锁的释放     | 1. 获取锁的线程执行完代码后释放锁；2. 线程执行发生异常，JVM 会释放锁 | 在 finally 中必须释放锁，不然容易造成线程死锁 |
| 锁类型       | 可重入、不可中断、非公平                                             | 可重入、可中断 公平                           |
| 性能         | 适用于少量同步                                                       | 适用于大量同步                                |
| 支持锁的场景 | 独占锁                                                               | 公平锁与非公平锁                              |
| 底层原理     | monitor(优化后的是cas)                                               | aqs                                           |
