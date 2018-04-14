# Java集合之Queue
队列 `Queue` 是一种只允许在一端进行插入操作，而另一端进行取出操作的  **线性表结构**。`Queue`是一种先进先出（First In First Out）的数据结构， 允许插入的一端是叫做 **队尾**，允许删除的一端叫做 **队首** 。严格说来，这种数据结构也是按照某种规则处理独立的元素，因此在Java中，这种数据结构的实现了`Collection`接口， 并扩展了`AbstractCollection`类。下面是Java Collection Framework中队列这种数据结构顶层接口规范的定义：

```java
public interface Queue<E> extends Collection<E> {
    boolean add(E e); //向队尾添加元素
    boolean offer(E e); //向队尾添加元素
    E revmove(); // 移除并返回队首元素
    E pool(); // 移除并返回队首元素
    E element(); //取队首元素，但不移除
    E peek(); //取队首元素，但不移除
}
```

从存储形式来讲，Java提供了基于线性存储和基于链式存储两种线性表形式的队列实现。另一方面，考虑到线程安全，Java同样提供了保证线程安全和非线程安全两种实现方式。

我们首先看一下Java Collection Framework中`Queue`的类继承体系:

![Java Collection Framework](./images/queue.png)
