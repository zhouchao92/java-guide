## GC 怎么判断对象可以被回收？
根可达：由根节点可以连接到达为有效对象，其他的视为垃圾。

- JVM stack main 虚拟机栈引用的对象
- native method stack 本地方法栈引用的对象
- runtime constant pool 方法区运行时的常量池
- static references in method area 方法区内静态引用对象

四种引用类型：

- 强引用：通过关键子 new 的对象，强引用指向的对象任何时候都不会被回收
- 软引用：当 JVM 堆空间不足时会被回收，一个类的软引用可以被 java.lang.ref.SoftRefrence 持有
- 弱引用：GC 时，只要发现就会被回收，一个类的弱引用可以被 java.lang.ref.WeakRefrence 持有
- 虚引用：用于跟踪对象，随时可以被回收，PhantomRefrence 持有

当对象变成 GC Roots 不可达时，GC 会判断对象是否覆盖 finalize() 方法，若未覆盖直接回收，反之，若对象未执行过 finalize() 方法，将其放入 F-Queue 队列，由一低优先级线程执行该队列中对象的 finalize() 方法，执行后再次进行 GC Roots 的可达性判断，若仍不可达则回收，反之，对象保持存活。
