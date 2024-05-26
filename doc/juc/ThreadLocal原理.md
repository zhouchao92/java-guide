#### ThreadLocal 的原理是什么？
```java
public class Thread implements Runable {
  ThreadLocal.ThreadLocalMap threadLocals = null;
  ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
}
public class ThreadLocal<T> { 
  public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = t.threadLocals;
    if (map != null) {
      ThreadLocalMap.Entry e = map.getEntry(this);
      if (e != null) {
        T result = (T) e.value;
        return result;
      }
    }
    // 初始化存放线程的 map
    return setInitialValue();
  }

  private T setInitialValue() {
    T value = null;
    Thread t = Thread.currentThread();
    ThreadLocalMap map = t.threadLocals;
    if (map != null)
      map.set(this, value);
    else
      t.threadLocals = new ThreadLocalMap(this, value)
    return value;
  }

  public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = t.threadLocals;
    if (map != null)
      map.set(this, value);
    else
      t.threadLocals = new ThreadLocalMap(this, value)
  }

  /**
    * 在不使用时移出，防止内存泄漏
    */
  public void remove() {
    ThreadLocalMap m = getMap(Thread.currentThread());
    if (m != null)
      m.remove(this);
  }
}
```
#### ThreadLocal 的内存泄漏问题？
ThreadLocalMap 的 key 是线程属于弱引用，当线程被回收时，key 会被回收，但是 value 属于强引用不会被回收，容易出现内存泄漏问题
![ThreadLocal 原理](/pic/ThreadLocal%20内存泄漏.jpeg)

