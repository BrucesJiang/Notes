# 同步数组之CopyOnWriteList类

## 写时复制(CopyOnWrite)
写时复制(CopyOnWrite, 简称COW)思想是计算机程序设计领域中的一种优化策略。 其核心思想是：如果有多个调用者(Callers)同时要求相同的资源（例如内存或者是磁盘上的数据存储），他们会获取指向相同资源的指针。当某个调用者试图修改资源内容时，系统会真正复制一份专用(private copy)给该调用者，而其他调用者见到的最初资源仍旧保持不变。 这个过程对其他调用者都是透明的(transparently)。该做法的优点是如果调用者没有修改资源，那么副本就不会被创建，因此多个调用者仅仅执行读取操作是，可以共享同一份资源。

## CopyOnWriteArrayList
CopyOnWriteArrayList是Java中的并发容器类，同时也是符合写入时复制思想的CopyOnWrite容器。

下面将通过CopyOnWriteArrayList的源码来了解写入时复制思想


部分主要源码与解析：

```java
final transient ReentrantLock lock = new ReentrantLock();
```
CopyOnWriteArrayList中有一个ReentrantLock锁，这是一个可重入的锁，提供了类似于synchronized的功能和内存语义，但是ReentrantLock的功能性更为全面。由于本文重点是介绍CopyOnWrite思想，所以对于ReentrantLock就不过多介绍，只要知道它是用来保证线程安全性的即可。

```java
//容器自身的数组，仅当使用getArray/setArray方法时才能获得
private transient volatile Object[] array;
```

下面这个两个方法是CopyOnWriteArrayList实现写入时复制的关键：
一个是获得当前容器数组的一个副本，另一个是将容器数组的引用指向一个修改之后的数组。

```java
final Ojbect[] getArray() {
	return array;
}


final void setArray(Object[] a) {
	array = a;
}
```
下面来看看使用了写入时复制的set方法：

```java
public E set(int index, E element) {
    final ReentrantLock lock = this.lock;
    lock.lock();//获得锁
    try {
        Object[] elements = getArray();//得到目前容器数组的一个副本
        E oldValue = get(elements, index);//获得index位置对应元素目前的值

        if (oldValue != element) {
            int len = elements.length;
            //创建一个新的数组newElements,将elements复制过去
            Object[] newElements = Arrays.copyOf(elements, len);
            //将新数组中index位置的元素替换为element
            newElements[index] = element;
            //这一步是关键，作用是将容器中array的引用指向修改之后的数组，即newElements
            setArray(newElements);
        } else {
            //index位置元素的值与element相等，故不对容器数组进行修改
            setArray(elements);
        }
        return oldValue;
    } finally {
        lock.unlock();//解除锁定
    }
}

```

可以看到，在set方法中，我们首先是获得了当前数组的一个拷贝获得一个新的数组，然后在这个新的数组上完成我们想要的操作。当操作完成之后，再把原有数组的引用指向新的数组。并且在此过程中，我们只拥有一个事实不可变对象，即容器中的array。这样一来就很巧妙地体现了CopyOnWrite思想。

本质上这也是读写分离的一种体现。当线程在对线程进行读或者写的操作时，其实操作的是不同的容器。这么一来我们可以对容器进行并发的读，而不需要加锁。实际上就是这么做的：

```java
public boolean contains(Object o) {
	Object[] elements = getArray();
	return indexOf(o, elements, 0, elements.length) >= 0;
}

public int indexOf(Object o) {
	Object[] elements = getArray();
	return indexOf(o, elements, 0, elements.length);	
} 
```
CopyOnWrite容器t的不足：

存在内存占用的问题，因为每次对容器结构进行修改的时候都要对容器进行复制，这么一来我们就有了旧有对象和新入的对象，会占用两份内存。如果对象占用的内存较大，就会引发频繁的垃圾回收行为，降低性能；
CopyOnWrite只能保证数据最终的一致性，不能保证数据的实时一致性。
所以对  于CopyOnWrite容器来说，只适合在读操作远远多于写操作的场景下使用，比如说缓存。

更多的 Linux内核分析及应用： 进程创建过程中用写时复制技术进行子进程内存地址空间的拷贝。