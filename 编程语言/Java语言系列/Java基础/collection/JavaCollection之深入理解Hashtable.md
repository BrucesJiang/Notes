# JavaCollection之深入理解Hashtable
Hashtable是遗留类，很多常用功能与HashMap类似，不同的是它继承自Dictionary类，并且是线程安全的，任一时间只有一个线程可以向Hashtable写入数据。但是，它的并发性不如`ConcurrentHashMap`，因为`ConcurrentHashMap`引入了分段锁。不建议在新代码中使用Hashtable，在不需要线程安全的场合可以用HashMap替换，需要保证线程安全的场合可以使用ConcurrentHashMap替换。

1. Hashtable是一个散列表，它存储的内容是键值对(Key-Value)映射
2. Hashtable继承自Dictionary类，实现了Map,Cloneable,java.io.Serializable接口
3. Hashtable是线程同步的，这也意味着它是线程安全的。
4. Hashtable更重要的属性是它的Key，Value都不可以是null
5. Hashtable注意其对hashCode()和equals()方法的重写。


## 使用方式

```java
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HashTableTest {

    public static void main(String args[]){
        Hashtable<String, Integer> table = new Hashtable<String, Integer>();
        
        //[1]添加元素
        table.put("zhangsan", 22);
        table.put("lisi", 33);
        table.put("wangwu", 44);
        
        //[2]toString()方式打印
        System.out.println(table.toString());
        
        //[3]Iterator遍历方式1--键值对遍历entrySet()
        Iterator<Entry<String, Integer>> iter = table.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>)iter.next();
            String key = entry.getKey();
            int value = entry.getValue();
            System.out.println("entrySet:"+key+" "+value);
        }
        
        System.out.println("====================================");
        
        //[4]Iterator遍历方式2--key键的遍历
        Iterator<String> iterator = table.keySet().iterator();
        while(iterator.hasNext()){
            String key = (String)iterator.next();
            int value = table.get(key);
            System.out.println("keySet:"+key+" "+value);
        }
        
        System.out.println("====================================");
        
        //[5]通过Enumeration来遍历Hashtable
        Enumeration<String> enu = table.keys();
        while(enu.hasMoreElements()) {
            System.out.println("Enumeration:"+table.keys()+" "+enu.nextElement());
        } 
            
    }
}

```

## 内部原理
1. **类继承体系**

```java
public class Hashtable<K,V> extends Dictionary<K,V> implements Map<K,V>, Cloneable, java.io.Serializable { 
    ...
} 
```
与HashMap不同的是Hashtable是继承Dictionary，实现了Map接口。Map是"key-value键值对"接口，Dictionary是声明了操作"键值对"函数接口的抽象类。 

2. **构造函数**
Hashtable提供了四个构造函数，如下：
```java
// 默认构造函数。  
public Hashtable()   
  
// 指定“容量大小”的构造函数  
public Hashtable(int initialCapacity)   
  
// 指定“容量大小”和“加载因子”的构造函数  
public Hashtable(int initialCapacity, float loadFactor)   
  
// 包含“子Map”的构造函数  
public Hashtable(Map<? extends K, ? extends V> t) 

```
上面的四个构造方法中，第三个是最重要的，指定初始化容量和构造因子。其余都是对该方法的调用。

```java
public Hashtable(int initialCapacity, float loadFactor) {    
    //验证初始容量    
    if (initialCapacity < 0)    
        throw new IllegalArgumentException("Illegal Capacity: "+    
                                           initialCapacity);    
    //验证加载因子    
    if (loadFactor <= 0 || Float.isNaN(loadFactor))    
        throw new IllegalArgumentException("Illegal Load: "+loadFactor);    

    if (initialCapacity==0)    
        initialCapacity = 1;    
        
    this.loadFactor = loadFactor;    
        
    //初始化table，获得大小为initialCapacity的table数组    
    table = new Entry[initialCapacity];    
    //计算阀值    
    threshold = (int)Math.min(initialCapacity * loadFactor, MAX_ARRAY_SIZE + 1);    
    //初始化HashSeed值    
    initHashSeedAsNeeded(initialCapacity);    
} 
```

3. **成员变量及其含义**
- table是一个Entry[]数组类型，而Entry实际上就是一个单向链表。哈希表的"key-value键值对"都是存储在Entry数组中的。 
- count是Hashtable的大小，它是Hashtable保存的键值对的数量。 
- threshold是Hashtable的阈值，用于判断是否需要调整Hashtable的容量。threshold的值="容量*加载因子"。
- loadFactor就是加载因子。
- modCount是用来实现fail-fast机制的
```java
private transient Entry[] table;  
// Hashtable中元素的实际数量  
private transient int count;  
// 阈值，用于判断是否需要调整Hashtable的容量（threshold = 容量*加载因子）  
private int threshold;  
// 加载因子  
private float loadFactor;  
// Hashtable被改变的次数  
private transient int modCount = 0;  
```

4. 主要方法实现以及其注释

从下面的代码中我们可以看出，Hashtable中的key和value是不允许为空的，当我们想要想Hashtable中添加元素的时候，首先计算key的hash值，然后通过hash值确定在table数组中的索引位置，最后将value值替换或者插入新的元素，如果容器的数量达到阈值，就会进行扩充。

```java
public synchronized V put(K key, V value) {    
    // 确保value不为null    
    if (value == null) {    
        throw new NullPointerException();    
    }    

    /*  
     * 确保key在table[]是不重复的  
     * 处理过程：  
     * 1、计算key的hash值，确认在table[]中的索引位置  
     * 2、迭代index索引位置，如果该位置处的链表中存在一个一样的key，则替换其value，返回旧值  
     */    
    Entry tab[] = table;    
    int hash = hash(key);    //计算key的hash值    
    int index = (hash & 0x7FFFFFFF) % tab.length;     //确认该key的索引位置    
    //迭代，寻找该key，替换    
    for (Entry<K,V> e = tab[index] ; e != null ; e = e.next) {    
        if ((e.hash == hash) && e.key.equals(key)) {    
            V old = e.value;    
            e.value = value;    
            return old;    
        }    
    }    

    modCount++;    
    if (count >= threshold) {  //如果容器中的元素数量已经达到阀值，则进行扩容操作    
        rehash();    
        tab = table;    
        hash = hash(key);    
        index = (hash & 0x7FFFFFFF) % tab.length;    
    }    

    // 在索引位置处插入一个新的节点    
    Entry<K,V> e = tab[index];    
    tab[index] = new Entry<>(hash, key, value, e);    
    //容器中元素+1    
    count++;    
    return null;    
}    
```

同样获取元素也是需要先获取索引值，然后遍历数组，最后返回元素。

```java
public synchronized V get(Object key) {    
    Entry tab[] = table;    
    int hash = hash(key);    
    int index = (hash & 0x7FFFFFFF) % tab.length;    
    for (Entry<K,V> e = tab[index] ; e != null ; e = e.next) {    
        if ((e.hash == hash) && e.key.equals(key)) {    
            return e.value;    
        }    
    }    
    return null;    
} 

```

## Hashtable和HashMap
（1）基类不同：HashTable基于Dictionary类，而HashMap是基于AbstractMap。Dictionary是什么？它是任何可将键映射到相应值的类的抽象父类，而AbstractMap是基于Map接口的骨干实现，它以最大限度地减少实现此接口所需的工作。

（2）null不同：HashMap可以允许存在一个为null的key和任意个为null的value，但是HashTable中的key和value都不允许为null。

（3）线程安全：HashMap时单线程安全的，Hashtable是多线程安全的。

（4）遍历不同：HashMap仅支持Iterator的遍历方式，Hashtable支持Iterator和Enumeration两种遍历方式。


### [HashMap 和 Hashtable 之间的不同?](http://stackoverflow.com/questions/40471/differences-between-hashmap-and-hashtable)
Java中 [HashMap](http://docs.oracle.com/javase/8/docs/api/index.html?java/util/HashMap.html) 和 [Hashtable](http://docs.oracle.com/javase/8/docs/api/index.html?java/util/Hashtable.html)的不同是什么？

非多线程应用中使用哪个更有效率？

解决方案

Java中HashMap和HashTable有几个不同点：

1. Hashtable 是同步的，然而 HashMap不是。 这使得HashMap更适合非多线程应用，因为非同步对象通常执行效率优于同步对象。
2. Hashtable 不允许 null 值和键。HashMap允许有一个 null 键和人一个 NULL 值。
3. HashMap的一个子类是[LinkedHashMap](http://java.sun.com/javase/7/docs/api/java/util/LinkedHashMap.html)。所以，如果想预知迭代顺序（默认的插入顺序），只需将HashMap转换成一个LinkedHashMap。用Hashtable就不会这么简单。

因为同步对你来说不是个问题，我推荐使用HashMap。如果同步成为问题，你可能还要看看 [ConcurrentHashMap](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentHashMap.html) 
