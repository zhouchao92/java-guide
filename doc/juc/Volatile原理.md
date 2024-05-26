#### volatile 的可见性和指令重排是怎么实现的？
每个线程在获取锁之后会在自己的工作内存来操作共享变量，操作完成之后将工作内存中的副本回写到主内存，并且在其它线程从主内存将变量同步回自己的工作内存之前，共享变量的改变对其是不可见的。即其他线程的本地内存中的变量已经是过时的，并不是更新后的值。

volatile 保证可见性的原理是在**每次访问变量时都会进行一次刷新**，因此每次访问都是主内存中最新的版本。即**保证变量修改的实时可见性**。
read ->load->use
read->load-use->store->write （缓存行锁定）
CPU缓存一致性协议 MESI ，开启后CPU会启动总线嗅探机制，当变量发生变化后，会将工作内存的变量失效。

指令重排序是JVM为了优化指令，提高程序运行效率，在不影响**单线程**程序执行结果的前提下，尽可能地提高并行度。编译器、处理器也遵循这样一个目标。（指令重排序包括编译器重排序和运行时重排序）

volatile 关键字通过提供“**内存屏障**”的方式来防止指令被重排序，为了实现 volatile 的内存语义，编译器在生成字节码时，会在指令序列中插入内存屏障来禁止特定类型的处理器重排序。

- 写操作前插入 StoreStore 屏障，写操作后插入 StoreLoad 屏障
- 读操作前插入 LoadLoad 屏障，读操作后插入 LoadStore 屏障

volatile **不保证操作的原子性**
```java
public class Singleton {
	private static volatile Singleton singleton;
    private Singleton {}
    public static Singleton getInstance(){
    	if(singleton == null){
        	synchronized (Singleton.class){
            	if(singleton == null){
                    // 非原子操作，volatile 禁止指令重排
                    // 1.申请内存，2.初始化对象，如果没有初始化，直接将申请到的内存空间地址赋给变量，半对象
                    // 半初始化问题
                    // 先 new 出对象，初始化属性，再 赋值 
                	singleton = new Singleton();
                }
            }
        }
        return singleton;
    }
}
```
