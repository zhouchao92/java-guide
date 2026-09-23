#### ConcurrentHashMap 底层原理是什么？

![ConcurrentHashMap 底层原理](/pic/ConcurrentHashMap%20底层原理.png)

线程安全的 HashMap，分段加锁 / CAS + synchronized，在保证线程安全的同时尽量降低锁粒度，读操作基本无锁。

#### JDK1.7 和 JDK1.8 的区别？

|            | JDK1.7                                            | JDK1.8                                                |
|------------|---------------------------------------------------|-------------------------------------------------------|
| 底层结构   | Segment[] + HashEntry[]（数组 + 链表）            | Node[]（数组 + 链表 + 红黑树）                        |
| 锁粒度     | Segment（分段锁，一个 Segment 一把 ReentrantLock） | 桶（Node，锁单个桶的头节点）                           |
| 加锁方式   | ReentrantLock                                     | CAS + synchronized                                    |
| hash 计算  | 两次 hash                                         | 一次 hash（扰动函数 hash ^ (hash >>> 16)）             |
| 链表结构   | 只有链表                                          | 链表长度 >= 8 且数组长度 >= 64 时插入，树化为红黑树           |
| 并发度     | 默认 16（Segment 数量）                           | 理论上等于数组长度，每个桶都可并发                     |

#### JDK1.7 的实现原理

- 继承 ReentrantLock，每个 Segment 内部维护一个 HashEntry 数组 + 链表
- 写操作（put/remove）只锁对应的 Segment，其他 Segment 可并发读写
- 默认并发度 16，即最多 16 个线程同时写
- get 不加锁，HashEntry 的 value 使用 volatile 修饰，保证可见性
- 缺点：多个线程操作同一个 Segment 时仍会互斥；统计 size 需要锁住所有 Segment

```java
// JDK1.7 分段锁
static final class Segment<K,V> extends ReentrantLock implements Serializable {
    transient volatile HashEntry<K,V>[] table;
    transient int count;
    // ...
}
```

#### JDK1.8 的实现原理

- 废弃 Segment，直接使用 Node[] 数组，结构与 HashMap 一致（数组 + 链表 + 红黑树）
- 锁粒度从 Segment 细化到单个桶（头节点），不同桶可完全并发
- 首次插入：CAS 设置头节点（无锁）
- 桶已存在元素：synchronized 锁住头节点，再插入 / 更新
- 读操作无锁：依赖 volatile（Node.val、Node.next、table）保证可见性
- 链表 >= 8 且数组 >= 64 时树化，降低长链表的查找代价

```java
// JDK1.8 Node，val 和 next 都是 volatile
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    volatile V val;
    volatile Node<K,V> next;
}
```

#### put 方法流程（JDK1.8）

```java
final V putVal(K key, V value, boolean onlyIfAbsent) {
    int hash = spread(key.hashCode());
    // 1. table 为空则先初始化（CAS 加 synchronized）
    // 2. 桶为空：CAS 写入头节点，失败则自旋重试
    // 3. 桶不为空：synchronized 锁头节点
    //    a. 链表尾插，长度 >= 8 且数组 >= 64 则树化
    //    b. 红黑树则走 putTreeVal
    //    c. key 已存在则覆盖
    // 4. addCount() 更新计数，超阈值则扩容
}
```

流程要点：

1. 计算 hash：`spread(hashCode) = hash ^ (hash >>> 16)`，高 16 位参与运算，减少冲突
2. table 为空：`initTable()` 初始化，CAS 抢占 SIZECTL 字段，保证只初始化一次
3. 桶为空：`casTabAt()` CAS 放入头节点，失败说明有并发，自旋重试
4. 桶不为空：`synchronized (f)` 锁住头节点 f，然后链表尾插 / 树插入
5. 插入成功后 `addCount(1)`，通过 CAS 或 CounterCell 分段累加更新 size
6. 超过阈值 `transfer()` 扩容

#### get 方法为什么不用加锁？

```java
public V get(Object key) {
    Node<K,V>[] tab; Node<K,V> e, p; int eh; K ek;
    int h = spread(key.hashCode());
    if ((tab = table) != null &&
        (e = tabAt(tab, (tab.length - 1) & h)) != null) {
        if ((eh = e.hash) == h) {           // 头节点匹配
            if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                return e.val;                // val 是 volatile，直接读
        }
        else if (eh < 0)                     // 红黑树 / ForwardingNode
            return (p = e.find(h, key)) != null ? p.val : null;
        while ((e = e.next) != null)         // 遍历链表，next 是 volatile
            if (e.hash == h && eq(key, e.key))
                return e.val;
    }
    return null;
}
```

- `table`、`Node.val`、`Node.next` 均为 volatile，写操作对读操作立即可见
- 读写不互斥：写的时候只锁桶，读不加锁，也不会因为写而阻塞
- 注意：get 是弱一致性的，可能读到扩容前 / 扩容中的瞬时状态，但不会读到脏数据

#### size 是如何统计的？（LongAdder 思想）

JDK1.8 不再锁全部 Segment，而是类似 LongAdder 的分段计数：

- `baseCount`：无竞争时 CAS 直接累加
- `CounterCell[]`：CAS 失败则哈希到某个 CounterCell 上累加（分散竞争）
- `sumCount() = baseCount + Σ CounterCell[i]`

```java
private transient volatile long baseCount;
private transient volatile CounterCell[] counterCells;
```

#### 扩容是如何优化的？（多线程协同扩容）

`transfer()` 扩容时：

1. 计算每个线程负责的步长（stride），最少 16
2. 抢占一个 transferIndex，领到一段桶的迁移任务
3. 迁移完的桶头节点替换为 `ForwardingNode`（hash 为 MOVED = -1）
4. 其他线程 put 时遇到 ForwardingNode，帮助一起扩容（helpTransfer）
5. 迁移方式与 HashMap 类似：高低位拆分（`hash & oldCap`），避免重新取模

```java
// 迁移中发现别的桶正在扩容，主动帮忙
else if ((fh = f.hash) == MOVED)
    tab = helpTransfer(tab, f);
```

#### 与 Hashtable 的区别？

|       | Hashtable                       | ConcurrentHashMap        |
|-------|---------------------------------|--------------------------|
| 锁粒度 | 整个对象一把 synchronized 锁     | 桶级锁 / CAS             |
| 读操作 | 需要获取锁                      | 无锁（volatile）         |
| 扩容   | 单线程，容易出现并发问题          | 多线程协同扩容            |
| null  | key/value 都不允许 null         | key/value 都不允许 null  |
| 性能   | 并发低，竞争激烈                 | 高并发下性能远优于 Hashtable |

#### 常见面试追问

- 为什么不允许 null key/value？因为 get 返回 null 无法区分「不存在」和「值就是 null」，并发下歧义更大
- 为什么锁头节点而不是锁整个桶？头节点不变即可保证链表结构稳定，插入在尾部，读也不受影响
- 与 ConcurrentHashMap 1.7 相比 1.8 为什么更快？锁粒度更细（Segment -> 桶）、CAS 减少加锁次数、红黑树降低查找代价、多线程协同扩容
