# Java容器工具类之Collections

## 查找、替换操作

```java
//二分查找元素，元素是有序的并且实现Comparable接口
<T> int binarySearch(List<? extends Comparable<? super T>> list, T key);

//二分查找有序数组，自定义比较器
<T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> c);

//根据元素的自然顺序，返回最大的元素。 类比int min(Collection coll)
<T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll);

//根据定制排序，返回最大元素，排序规则由Comparatator类控制。类比int min(Collection coll, Comparator c)
<T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll, Comparator<? super T> cmp);

//用元素obj填充list中所有元素
<T> void fill(List<? super T> list, T obj);

//统计元素出现次数
int frequency(Collection<?> c, Object o);

//统计targe在list中第一次出现的索引，找不到则返回-1，类比int lastIndexOfSubList(List source, list target).
int indexOfSubList(List<?> list, List<?> target);

//返回指定源列表中最后一次出现的指定目标列表的起始位置，如果没有此类出现，则返回-1
int lastIndexOfSubList(List<?> source, List<?> target);

//用新元素替换旧元素。
boolean replaceAll(List<?> list, T oldVal, T newVal);
```
代码示例：
```java
import java.util.ArrayList;
import java.util.Collections;

public class CollectionsTest {
    public static void main(String[] args) {
        ArrayList num =  new ArrayList();
        num.add(3);
        num.add(-1);
        num.add(-5);
        num.add(10);
        System.out.println(num);
        System.out.println(Collections.max(num));
        System.out.println(Collections.min(num));
        Collections.replaceAll(num, -1, -7);
        System.out.println(Collections.frequency(num, 3));
        Collections.sort(num);
        System.out.println(Collections.binarySearch(num, -5));
    }
}

```


## 排序操作

```java
//反转列表
void reverse(List<?> list);

//使用指定的随机来源随机排列指定的列表
void shuffle(List<?> list);

//按照自然序升序排序
<T extends Comparable<? super T>> void sort(List<?> list);

//定制排序，Comparator为比较器
<T> void sort(List list<?>, Comparator<? super T> c)

//交换两个索引位置的元素
void swap(List<?> list, int i, int j);

//旋转列表。当distance为整数时，将list后distance个元素整体按序移动到前面。
//当distance为负数时，将list前distance个元素整体按序移动到后面
void rotate(List<?> list, int distance);
```
代码示例：

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CollectionsTest {
    public static void main(String[] args) {
        ArrayList nums =  new ArrayList();
        nums.add(8);
        nums.add(-3);
        nums.add(2);
        nums.add(9);
        nums.add(-2);
        System.out.println(nums);
        Collections.reverse(nums);
        System.out.println(nums);
        Collections.sort(nums);
        System.out.println(nums);
        Collections.shuffle(nums);
        System.out.println(nums);
        //下面只是为了演示定制排序的用法，将int类型转成string进行比较
        Collections.sort(nums, new Comparator() {
            
            @Override
            public int compare(Object o1, Object o2) {
                // TODO Auto-generated method stub
                String s1 = String.valueOf(o1);
                String s2 = String.valueOf(o2);
                return s1.compareTo(s2);
            }
            
        });
        System.out.println(nums);
    }
}
```



## 同步控制
Collections中几乎对每个集合都定义了同步控制方法，例如 SynchronizedList(), SynchronizedSet()等方法，来将集合包装成线程安全的集合。下面是Collections将普通集合包装成线程安全集合的用法，
```java
<T> Collection<T> synchronizedCollection(Collection<T> c);

<T> List<T> synchronizedList(List<T> list);

<K, V> Map<K, V> synchronizedMap(Map<K, V> m);

<K, V> NavigableMap<K, V> synchronizedNavigableMap<NavigableMap<K, V> m);

<T> NavigableSet<T> synchroinzedNavigableSet(NavigableSet<T> s);

<T> Set<T> synchronizedSet(Set<T> s);

```
代码示例：
```java
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SynchronizedTest {
    public static void main(String[] args) {
        Collection c = Collections.synchronizedCollection(new ArrayList());
        List list = Collections.synchronizedList(new ArrayList());
        Set s = Collections.synchronizedSet(new HashSet());
        Map m = Collections.synchronizedMap(new HashMap());
    }
}
```

## 设置不可变（只读）集合

Collections提供了三类方法返回一个不可变集合，

1. emptyXXX(), 返回一个空的只读集合

```java
<T> Enumeration<T> emptyEnumeration();

<T> Iterator<T> emptyIterator();

<T> List<T> emptyList();

<T> ListIterator<T> emptyListIterator();

<K, V> Map<K, V> emptyMap();

<K, V> NavigableMap<K, V> emptyNavigableMap();

<E> NavigableSet<E> emptyNavigableSet();

<K, V> SortedMap<K, V> emptySortedMap();

<E> SortedSet<E> emptySortedSet();

<T> Enumeration<T> enumeration(Collection<T> c);

```

2. singleXXX()，返回只包含指定对象的不可变集合。

```java
<T> Set<T> singleton(T o);

<T> List<T> singletonList(T o);

<K, V> Map<K, V> singletonMap(K key, V value);
```

3. unmodifiablleXXX()，返回指定集合对象的 **只读视图**。

```java
<T> List<T> unmodifiableList(List<? extends T> list);

<K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> m);

<K, V> NavigableMap<K, V> unmodifiableNavigableMap(NavigableMap<K, ? extends V> m);

<T> NavigableSet<T> unmodifiableNavigableSet(NavigableSet<T> s);

<T> Set<T> unmodifiableSet(Set<? extends T> s);

<K, V> SortedMap<K, V> unmodifiableSortMap(SortedMap<K, ? extends V> m);

<T> SortedSet<T> unmodifiableSorted(SortedSet<T> s);
```
代码示例：

```java
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UnmodifiableCollection {
    public static void main(String[] args) {
        List lt = Collections.emptyList();
        Set st = Collections.singleton("avs");
        
        Map mp = new HashMap();
        mp.put("a",100);
        mp.put("b", 200);
        mp.put("c",150);
        Map readOnlyMap = Collections.unmodifiableMap(mp);
        
        //下面会报错
        lt.add(100);
        st.add("sdf");
        mp.put("d", 300);
    }
}

```


## 其它

```java
//如果两个指定 collection 中没有相同的元素，则返回 true。
boolean disjoint(Collection<?> c1, Collection<?> c2);

//一种方便的方式，将所有指定元素添加到指定 collection 中。
//示范： Collections.addAll(flavors, "Peaches 'n Plutonium", "Rocky Racoon");
<T> boolean addAll(Collection<? super T> c, T... a);

//返回一个比较器，它强行反转指定比较器的顺序。

//如果指定比较器为 null，则此方法等同于 reverseOrder()（换句话说，它返回一个比较器，该比较器将强行反转实现 Comparable 接口那些对象 collection 上的自然顺序）。
<T> Comparator<T> reverseOrder();

//指定比较器
<T> Comparator<T> reverseOrder(Comparator<T> cmp);

```
代码示例，求交集：
```java
public void testIntersection() {
    List<String> list1 = new ArrayList<String>();
    List<String> list2 = new ArrayList<String>();
    
    // addAll增加变长参数
    Collections.addAll(list1, "大家好", "你好","我也好");
    Collections.addAll(list2, "大家好", "a李四","我也好");
    
    // disjoint检查两个Collection是否的交集
    boolean b1 = Collections.disjoint(list, list1);
    boolean b2 = Collections.disjoint(list, list2);
    System.out.println(b1 + "\t" + b2);
    
    // 利用reverseOrder倒序
    Collections.sort(list1, Collections.reverseOrder());
    System.out.println(list1);
}
```