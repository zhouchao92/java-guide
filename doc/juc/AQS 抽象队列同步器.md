![抽象队列同步器模型](/pic/%E6%8A%BD%E8%B1%A1%E9%98%9F%E5%88%97%E5%90%8C%E6%AD%A5%E5%99%A8%E6%A8%A1%E5%9E%8B.png)

它维护了一个 volatile int state（代表共享资源）和一个 FIFO 线程等待队列（多线程争用资源被阻塞时会进入此队列）。

_可重入_  
同步器的实现是 AQS 核心，以 ReentrantLock 为例，state 初始化为 0，表示未锁定状态。
1. A 线程lock()时，会调用 tryAcquire()独占该锁并将 state+1。
2. 此后，其他线程再 tryAcquire()时就会失败，直到 A 线程 unlock()到 state=0（即释放锁）为止，其它线程才有机会获取该锁。当然，释放锁之前，A 线程自己是可以重复获取此锁的（state 会累加），这就是可重入的概念。
3. 但要注意，获取多少次就要释放多么次，这样才能保证 state 是能回到零态的。

以 CountDownLatch 以例，任务分为 N 个子线程去执行，state 也初始化为 N（注意 N 要与线程个数一致）。这 N 个子线程是并行执行的，每个子线程执行完后 countDown()一次，state 会 CAS 减 1。等到所有子线程都执行完后(即 state=0)，会 unpark()主调用线程，然后主调用线程就会从 await()函数返回，继续后余动作。