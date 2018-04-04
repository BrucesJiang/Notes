# Java Collection之深入理解HashMap
通过[`Map`](./Java集合之Map.md)，我们知道`HashMap`根据键的`hashCode`值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度，但遍历顺序却是不确定的。 `HashMap`最多只允许一条记录的键为`null`，允许多条记录的值为`null`。`HashMap`非线程安全，即任一时刻可以有多个线程同时写`HashMap`，可能会导致数据的不一致。如果需要满足线程安全，可以用 `Collections`的`synchronizedMap`方法使`HashMap`具有线程安全的能力，或者使用`ConcurrentHashMap`。下面是JDK 8官方文档对HashMap给出的详细描述:

`
Hash table based implementation of the Map interface. This implementation provides all of the optional map operations, and permits null values and the null key. (The HashMap class is roughly equivalent to Hashtable, except that it is unsynchronized and permits nulls.) This class makes no guarantees as to the order of the map; in particular, it does not guarantee that the order will remain constant over time. 

This implementation provides constant-time performance for the basic operations (get and put), assuming the hash function disperses the elements properly among the buckets. Iteration over collection views requires time proportional to the "capacity" of the HashMap instance (the number of buckets) plus its size (the number of key-value mappings). Thus, it's very important not to set the initial capacity too high (or the load factor too low) if iteration performance is important. 

An instance of HashMap has two parameters that affect its performance: initial capacity and load factor. The capacity is the number of buckets in the hash table, and the initial capacity is simply the capacity at the time the hash table is created. The load factor is a measure of how full the hash table is allowed to get before its capacity is automatically increased. When the number of entries in the hash table exceeds the product of the load factor and the current capacity, the hash table is rehashed (that is, internal data structures are rebuilt) so that the hash table has approximately twice the number of buckets. 

As a general rule, the default load factor (.75) offers a good tradeoff between time and space costs. Higher values decrease the space overhead but increase the lookup cost (reflected in most of the operations of the HashMap class, including get and put). The expected number of entries in the map and its load factor should be taken into account when setting its initial capacity, so as to minimize the number of rehash operations. If the initial capacity is greater than the maximum number of entries divided by the load factor, no rehash operations will ever occur. 

If many mappings are to be stored in a HashMap instance, creating it with a sufficiently large capacity will allow the mappings to be stored more efficiently than letting it perform automatic rehashing as needed to grow the table. Note that using many keys with the same hashCode() is a sure way to slow down performance of any hash table. To ameliorate impact, when keys are Comparable, this class may use comparison order among keys to help break ties. 

Note that this implementation is not synchronized. If multiple threads access a hash map concurrently, and at least one of the threads modifies the map structurally, it must be synchronized externally. (A structural modification is any operation that adds or deletes one or more mappings; merely changing the value associated with a key that an instance already contains is not a structural modification.) This is typically accomplished by synchronizing on some object that naturally encapsulates the map. If no such object exists, the map should be "wrapped" using the Collections.synchronizedMap method. This is best done at creation time, to prevent accidental unsynchronized access to the map:

```java
Map m = Collections.synchronizedMap(new HashMap(...));
```
The iterators returned by all of this class's "collection view methods" are fail-fast: if the map is structurally modified at any time after the iterator is created, in any way except through the iterator's own remove method, the iterator will throw a ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future. 

Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in the presence of unsynchronized concurrent modification. Fail-fast iterators throw ConcurrentModificationException on a best-effort basis. Therefore, it would be wrong to write a program that depended on this exception for its correctness: the fail-fast behavior of iterators should be used only to detect bugs. 
`
我们知道`HashMap`其实是抽象数据结构符号表（Symbol Table）的一种实现。符号表是一种存储键值对的数据结构，支持两种最基本的操作:`插入（put）`，即将一组新的键值对存入表中；`查找（get）`，即根据给定的键得到相应的值。下文，我将主要结合源码，从存储结构、常用方法分析、扩容以及安全性等方面深入分析HashMap的工作原理。

## 存储结构
从逻辑存储结构上来讲，HashMap是`数组+链表/红黑树`（JDK1.8中添加了红黑树部分）实现的，如下图所示：

![hashMap内存结构图](./images/hashMap内存结构图.png)

我们主要从以下两个方面分析HashMap的存储结构： 数据是如何组织的？这样的组织结构由什么好处？

从JDK 1.8源代码可知，`HashMap`类中有一个非常重要的字段——`Node[] table`，也就是`哈希桶数组`。定义`Node`元素的源代码实现如下：

```java
/**
 * 在这个内部类定义过程中final的用法，还没有弄明白？待续
 */
static class Node<K, V> implements Map.Entry<K, V> {
    final int hash; // 用于定位索引位置
    final K key;
    V value;
    Node<V, V> next; //链表的下一个元素指针

    Node(int hash, K key, V value, Node<K, V> next) { ... }
    public final K getKey(){ ... }
    public final V getValue(){ ... }
    public final String toString(){ ... }
    public final int hashCode(){ ... }
    public final V setValue(V newValue){ ... }
    public final boolean equals(Object o) { ... }
}

```
阅读源代码，我们知道，`Node`是`HashMap`的内部类，实现了`Map.Entry<K, V>`接口，本质上定义了一个键值对(Key-Value)。上图中的黑色圆点代表了`Node`对象。







