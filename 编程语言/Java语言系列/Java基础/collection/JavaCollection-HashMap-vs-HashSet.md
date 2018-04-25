# 比较 HashMap 和 HashSet
`HashMap`和`HashSet`都是Java Collection Framework框架的一部分，但是来自两个不同的继承体系。在Java Collection Framework中`HashMap`的顶层父类是`Map`接口，而`HashSet`的顶层父类是`Collection`（`Set`扩展了`Collection`接口）。

## HashSet
`HashSet`实现了`Set`接口，它不允许集合中有重复的值，当我们提到`HashSet`时，第一件事情就是在将对象存储在`HashSet`之前，要先确保对象重写`equals()方法`和`hashCode()方法`，这样才能比较对象的值是否相等，以确保`set`中没有储存相等的对象。`如果我们没有重写这两个方法，将会使用这个方法的默认实现。`

`public boolean add(Object o)`方法用来在`Set`中添加元素，当元素值重复时则会立即返回`false`，如果成功添加的话会返回`true`。

## HashMap
`HashMap`实现了`Map`接口，`Map`接口对键值对进行映射。`Map`中不允许重复的键。`Map`接口有两个基本的实现，`HashMap`和`TreeMap`。`TreeMap`保存了对象的排列次序，而`HashMap`则不能。`HashMap`允许`键和值为null`。`HashMap`是`非synchronized`的，但Java Collection Framework框架提供方法能保证`HashMap synchronized`，这样多个线程同时访问`HashMap`时，能保证`只有一个线程`更改`Map`。

`public Object put(Object Key,Object value)`方法用来将元素添加到`map`中。
## HashMap和HashSet区别
| HashMap | HashSet |
|:-----:|:---:|
| HashMap实现了Map接口 | HashSet实现了Set接口 |
| HashMap储存键值对    | HashSet仅仅存储对象  |
| 使用put()方法将元素放入map中 | 使用add()方法将元素放入set中 |
| HashMap中使用键对象来计算hashcode值 |   HashSet使用成员对象来计算hashcode值，对于两个对象来说hashcode可能相同，所以equals()方法用来判断对象的相等性，如果两个对象不同的话，那么返回false |


## HashSet
1. HashSet class implements the Set interface
2. In HashSet, we store objects(elements or values) e.g. If we have a HashSet of string elements then it could depict a set of HashSet elements: {“Hello”, “Hi”, “Bye”, “Run”}
3. HashSet does not allow duplicate elements that mean you can not store duplicate values in HashSet.
4. HashSet permits to have a single null value.
5. HashSet is not synchronized which means they are not suitable for thread-safe operations until unless synchronized explicitly.
```
                      add      contains next     notes
HashSet               O(1)     O(1)     O(h/n)   h is the table 
```

## HashMap
1. HashMap class implements the Map interface
2. HashMap is used for storing key & value pairs. In short, it maintains the mapping of key & value (The HashMap class is roughly equivalent to Hashtable, except that it is unsynchronized and permits nulls.) This is how you could represent HashMap elements if it has integer key and value of String type: e.g. {1->”Hello”, 2->”Hi”, 3->”Bye”, 4->”Run”}
3. HashMap does not allow duplicate keys however it allows having duplicate values.
4. HashMap permits single null key and any number of null values.
5. HashMap is not synchronized which means they are not suitable for thread-safe operations until unless synchronized explicitly.[similarity]
```
                       get      containsKey next     Notes
 HashMap               O(1)     O(1)        O(h/n)   h is the table 
 ```