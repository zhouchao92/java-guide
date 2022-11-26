## 非公平锁-->加锁  
不会判断是否需要排队
```java
final void lock() {
    // 1.尝试加锁
    if (compareAndSetState(0, 1))
        setExclusiveOwnerThread(Thread.currentThread());
    else
        // 2.加锁失败后再次尝试加锁，失败后加入队列
        acquire(1);
}

public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}

// tryAcquire -> nonfairTryAcquire
final boolean nonfairTryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        // 直接抢锁 
        if (compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    else if (current == getExclusiveOwnerThread()) {
        // 可重入的逻辑
        int nextc = c + acquires;
        if (nextc < 0) // overflow
            throw new Error("Maximum lock count exceeded");
        setState(nextc);
        return true;
    }
    return false;
}
```

## 公平锁-->加锁
```java
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;

    final void lock() {
        acquire(1);
    }

    /**
     * Fair version of tryAcquire.  Don't grant access unlessrecursive call or no waiters or is first.
     */
    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        // 1.判断是否需要排队，不需要排队则加锁
        if (c == 0) {
            if (!hasQueuedPredecessors() &&
                compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {
            // 2.重入判断，次数不能超过int最大值
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        return false;
    }
}

public final boolean hasQueuedPredecessors() {
    // The correctness of this depends on head being initialized before tail and on head.next being accurate if the current thread is first in queue.
    Node t = tail; // Read fields in reverse initialization order
    Node h = head;
    Node s;
    return h != t &&
        // 并发情况下，入队时先修改节点的pre，再修改tail节点的指向，最后将head节点的next指向当前节点
        ((s = h.next) == null || s.thread != Thread.currentThread());
}
```

## 解锁
```java
/**
 * 初始状态
 */
static final int INITIAL = 0;
/** 
 * waitStatus value to indicate thread has cancelled 
 * 由于在同步队列中等待的线程等待超时或者被中断，需要从同步队列中取消等待，节点进入该状态将不会变化
 */
static final int CANCELLED =  1;
/** 
 * waitStatus value to indicate successor's thread needs unparking 
 * 后继节点的线程处于等待状态，而当前节点的线程如果释放了同步状态或者被取消，将会通知后继节点，使后继节点的线程得以运行
 */
static final int SIGNAL    = -1;
/** 
 * waitStatus value to indicate thread is waiting on condition 
 * 节点在等待队列中，节点线程等待在Condition上，当其他线程对Condition调用了signal()方法后，该节点将会从等待队列中转移到同步队列中，加入到对同步状态的获取中
 */
static final int CONDITION = -2;
/**
 * waitStatus value to indicate the next acquireShared should unconditionally propagate
 * 表示下一次共享式同步状态获取将会无条件地被传播下去
 */
static final int PROPAGATE = -3;

public void unlock() {
    sync.release(1);
}

public final boolean release(int arg) {
    // 释放锁
    if (tryRelease(arg)) {
        Node h = head;
        
        if (h != null && h.waitStatus != 0)
            // 唤醒头节点的后继节点线程
            unparkSuccessor(h);
        return true;
    }
    return false;
}

protected final boolean tryRelease(int releases) {
    int c = getState() - releases;
    // 如果当前线程不是持有锁的线程则抛出异常不允许释放锁
    if (Thread.currentThread() != getExclusiveOwnerThread())
        throw new IllegalMonitorStateException();
    boolean free = false;
    if (c == 0) {
        // 如果释放锁后没有线程持有锁
        free = true;
        setExclusiveOwnerThread(null);
    }
    setState(c);
    return free;
}

private void unparkSuccessor(Node node) {
    /*
     * If status is negative (i.e., possibly needing signal) try
     * to clear in anticipation of signalling.  It is OK if this
     * fails or if status is changed by waiting thread.
     */
    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    /*
     * Thread to unpark is held in successor, which is normally
     * just the next node.  But if cancelled or apparently null,
     * traverse backwards from tail to find the actual
     * non-cancelled successor.
     */
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread);
}
```


