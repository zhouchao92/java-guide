#### HashMap 底层原理是什么？
动态数组 + 链表 / 红黑树（jdk1.8）
默认初始化大小为 16（2的 n 次幂，提高散列程度，降低 hash 碰撞），负载因子为 0.75（泊松分布）
极值条件：

1. 在数组长度大于等于 64，链表长度大于 8 时升级为红黑树
2. 在链表长度小于等于 6 时由红黑树退化为链表

#### JDK1.8中对hash算法和寻址算法是如何优化的？
```java
// JDK1.8 
static final int hash(Object key) {     
    int h;     
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16); 
} 
```
举例：

- 1111 1111 1111 1111 1111 1010 0111 1100 （未优化的hash值）
- 0000 0000 0000 0000 1111 1111 1111 1111 （右移）
- 1111 1111 1111 1111 0000 0101 1000 0011 （异或）-> int 值，32位

hash算法优化：高低16位都参与运算，让低16位同时保持高低16位的特征，尽量避免一些hash值后续出现冲突，出现在数组里的同一个位置。

寻址算法优化：(n-1) & hash -> 数组里的一个位置

- 取模运算：性能比较差；
- (n-1) & hash 效果是跟hash对n取模的效果是一样的，但是性能要比对n取模要高很多。（数组的长度一直是2的N次方）
- 高16位的运算可以忽略，核心在于低16位的与运算。

#### HashMap如何解决hash碰撞问题？
hash 冲突问题，链表+红黑树，O(n) 和 O(logn)
get(key)，如果定位到数组的某个位置是一个链表，遍历链表，找到指定的 key-value对。假设链表很长，可能会导致遍历链表的性能很差，O(n)。
优化：如果链表的长度达到了一定的长度之后，会将链表转换为红黑树，遍历一棵红黑树找一个元素，O(logn)，性能会比链表高。

#### HashMap是如何进行扩容的？
2 倍扩容，rehash() 重新计算索引
假设数组长度为16

| **操作** | **值**                                                     |
|----------|------------------------------------------------------------|
| n-1      | 0000 0000 0000 0000 0000 0000 0000 1111                    |
| hash1    | 1111 1111 1111 1111 0000 1111 0000 0101                    |
| &结果    | 0000 0000 0000 0000 0000 0000 0000 0101 (5，index=5的位置) |
| hash2    | 1111 1111 1111 1111 0000 1111 0001 0101                    |
| &结果    | 0000 0000 0000 0000 0000 0000 0000 0101 (5，index=5的位置) |

数组的长度扩容2倍后为32，重新对每个hash值进行寻址，即用每个hash值跟新的数组长度-1进行&运算。

| **操作** | **值**                                                       |
|----------|--------------------------------------------------------------|
| n-1      | 0000 0000 0000 0000 0000 0000 1111 1111                      |
| hash1    | 1111 1111 1111 1111 0000 1111 0000 0101                      |
| &结果    | 0000 0000 0000 0000 0000 0000 0000 0101 (5，index=5的位置)   |
| hash2    | 1111 1111 1111 1111 0000 1111 0001 0101                      |
| &结果    | 0000 0000 0000 0000 0000 0000 0001 0101 (21，index=21的位置) |

判断二进制结果中是否多出一个 bit 的 1，如果没多，就是原来的 index，如果多了出来，就是 index+oldCap，通过这种方式，就避免了 rehash 的时候，用每个 hash 对新数组的长度取模，取模性能不高，位运算的性能比较高。

#### put方法分析
```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
			   boolean evict) {
	// 临时变量，提供性能==>避免频繁从堆获取元素
	Node<K,V>[] tab; Node<K,V> p; int n, i;
	if ((tab = table) == null || (n = tab.length) == 0)
		n = (tab = resize()).length;
	if ((p = tab[i = (n - 1) & hash]) == null)
		// 当前桶的位置不存在元素
		tab[i] = newNode(hash, key, value, null);
	else {
		// 元素存在==>判断是链表还是红黑树
		Node<K,V> e; K k;
		if (p.hash == hash &&
			((k = p.key) == key || (key != null && key.equals(k))))
			// 当前Node即为所添加的key
			e = p;
		else if (p instanceof TreeNode)
			// 红黑树增加节点
			e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
		else {
			// 遍历链表，尾插法插入key
			for (int binCount = 0; ; ++binCount) {
				if ((e = p.next) == null) {
					p.next = newNode(hash, key, value, null);
					if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
						// 转换为红黑树
						treeifyBin(tab, hash);
					break;
				}
				if (e.hash == hash &&
					((k = e.key) == key || (key != null && key.equals(k))))
					// 当前key在链表中存在
					break;
				p = e;
			}
		}
		if (e != null) { // existing mapping for key
			V oldValue = e.value;
			if (!onlyIfAbsent || oldValue == null)
				e.value = value;
			afterNodeAccess(e);
			return oldValue;
		}
	}
	++modCount;
	if (++size > threshold)
		resize();
	afterNodeInsertion(evict);
	return null;
}
```

#### 红黑树
```java
final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
							   int h, K k, V v) {
	Class<?> kc = null;
	boolean searched = false;
	TreeNode<K,V> root = (parent != null) ? root() : this;
	for (TreeNode<K,V> p = root;;) {
		int dir, ph; K pk;
		if ((ph = p.hash) > h)
			dir = -1;
		else if (ph < h)
			dir = 1;
		else if ((pk = p.key) == k || (k != null && k.equals(pk)))
			return p;
		else if ((kc == null &&
				  (kc = comparableClassFor(k)) == null) ||
				 (dir = compareComparables(kc, k, pk)) == 0) {
			if (!searched) {
				TreeNode<K,V> q, ch;
				searched = true;
				if (((ch = p.left) != null &&
					 (q = ch.find(h, k, kc)) != null) ||
					((ch = p.right) != null &&
					 (q = ch.find(h, k, kc)) != null))
					return q;
			}
			dir = tieBreakOrder(k, pk);
		}

		TreeNode<K,V> xp = p;
		if ((p = (dir <= 0) ? p.left : p.right) == null) {
			Node<K,V> xpn = xp.next;
			TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
			if (dir <= 0)
				xp.left = x;
			else
				xp.right = x;
			xp.next = x;
			x.parent = x.prev = xp;
			if (xpn != null)
				((TreeNode<K,V>)xpn).prev = x;
			moveRootToFront(tab, balanceInsertion(root, x));
			return null;
		}
	}
}
```
```java
final void treeify(Node<K,V>[] tab) {
	TreeNode<K,V> root = null;
	for (TreeNode<K,V> x = this, next; x != null; x = next) {
		next = (TreeNode<K,V>)x.next;
		x.left = x.right = null;
		if (root == null) {
			x.parent = null;
			x.red = false;
			root = x;
		}
		else {
			K k = x.key;
			int h = x.hash;
			Class<?> kc = null;
			for (TreeNode<K,V> p = root;;) {
				int dir, ph;
				K pk = p.key;
				if ((ph = p.hash) > h)
					dir = -1;
				else if (ph < h)
					dir = 1;
				else if ((kc == null &&
						  (kc = comparableClassFor(k)) == null) ||
						 (dir = compareComparables(kc, k, pk)) == 0)
					dir = tieBreakOrder(k, pk);

				TreeNode<K,V> xp = p;
				if ((p = (dir <= 0) ? p.left : p.right) == null) {
					x.parent = xp;
					if (dir <= 0)
						xp.left = x;
					else
						xp.right = x;
					root = balanceInsertion(root, x);
					break;
				}
			}
		}
	}
	moveRootToFront(tab, root);
}
```
```java
final Node<K,V> untreeify(HashMap<K,V> map) {
	Node<K,V> hd = null, tl = null;
	for (Node<K,V> q = this; q != null; q = q.next) {
		Node<K,V> p = map.replacementNode(q, null);
		if (tl == null)
			hd = p;
		else
			tl.next = p;
		tl = p;
	}
	return hd;
}
```
```java
final Node<K,V>[] resize() {
	Node<K,V>[] oldTab = table;
	int oldCap = (oldTab == null) ? 0 : oldTab.length;
	int oldThr = threshold;
	int newCap, newThr = 0;
	if (oldCap > 0) {
		if (oldCap >= MAXIMUM_CAPACITY) {
			threshold = Integer.MAX_VALUE;
			return oldTab;
		}
		else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
				 oldCap >= DEFAULT_INITIAL_CAPACITY)
			newThr = oldThr << 1; // double threshold
	}
	else if (oldThr > 0) // initial capacity was placed in threshold
		newCap = oldThr;
	else {               // zero initial threshold signifies using defaults
		newCap = DEFAULT_INITIAL_CAPACITY;
		newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
	}
	if (newThr == 0) {
		float ft = (float)newCap * loadFactor;
		newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
				  (int)ft : Integer.MAX_VALUE);
	}
	threshold = newThr;

	Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
	table = newTab;
	if (oldTab != null) {
		for (int j = 0; j < oldCap; ++j) {
			Node<K,V> e;
			if ((e = oldTab[j]) != null) {
				oldTab[j] = null;
				if (e.next == null)
					// 链表只有1个元素
					newTab[e.hash & (newCap - 1)] = e;
				else if (e instanceof TreeNode)
					// 红黑树
					((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
				else { // preserve order
					Node<K,V> loHead = null, loTail = null;
					Node<K,V> hiHead = null, hiTail = null;
					// 1个链表拆分为2个链表
					Node<K,V> next;
					do {
						next = e.next;
						if ((e.hash & oldCap) == 0) {
							// 位置不变
							if (loTail == null)
								loHead = e;
							else
								loTail.next = e;
							loTail = e;
						}
						else {
							if (hiTail == null)
								hiHead = e;
							else
								hiTail.next = e;
							hiTail = e;
						}
					} while ((e = next) != null);
					if (loTail != null) {
						loTail.next = null;
						newTab[j] = loHead;
					}
					if (hiTail != null) {
						hiTail.next = null;
						newTab[j + oldCap] = hiHead;
					}
				}
			}
		}
	}
	return newTab;
}
```
```java
final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
	TreeNode<K,V> b = this;
	// Relink into lo and hi lists, preserving order
	TreeNode<K,V> loHead = null, loTail = null;
	TreeNode<K,V> hiHead = null, hiTail = null;
	int lc = 0, hc = 0;
	for (TreeNode<K,V> e = b, next; e != null; e = next) {
		next = (TreeNode<K,V>)e.next;
		e.next = null;
		if ((e.hash & bit) == 0) {
			if ((e.prev = loTail) == null)
				loHead = e;
			else
				loTail.next = e;
			loTail = e;
			++lc;
		}
		else {
			if ((e.prev = hiTail) == null)
				hiHead = e;
			else
				hiTail.next = e;
			hiTail = e;
			++hc;
		}
	}

	if (loHead != null) {
		if (lc <= UNTREEIFY_THRESHOLD)
			// 退化
			tab[index] = loHead.untreeify(map);
		else {
			tab[index] = loHead;
			if (hiHead != null) // (else is already treeified)
				loHead.treeify(tab);
		}
	}
	if (hiHead != null) {
		if (hc <= UNTREEIFY_THRESHOLD)
			// 退化
			tab[index + bit] = hiHead.untreeify(map);
		else {
			tab[index + bit] = hiHead;
			if (loHead != null)
				hiHead.treeify(tab);
		}
	}
}
```

