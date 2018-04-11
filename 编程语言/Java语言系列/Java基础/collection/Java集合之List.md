# Java集合之List
**列表（List）** 是一种基础的数据结构，通常可以通过 **按索引访问** 列表中的元素。另外，列表提供了对自身元素进搜索、排序和操作的方法。 

在Java语言中，列表有`java.util`包中的`List`接口指定。`List`接口继承了`Clolection`接口。

![List类继承](./images/list.png)

下列代码展示了`List`接口中一些基本的重要方法：
```java
public interface List<T> extends Collection<T> {
    T get(int idx);
    T set(int idx, T val);
    void add(int idx, T val);
    void remove(int idx);

    ListIterator<T> listIterator(int pos);
}
```
其中`get`和`set`可以访问或改变通过由位置索引`idx`给定的表中指定位置上的项。

`List`的类继承体系中主要有三个常用的实现类，分别是`Stack`，`Vector`, `ArrayList`和`LinkedList`。

`List`具有两种流行的实现方式。`ArrayList`提供了一种 **可增长数组实现**。使用`ArrayList`的有点在于，按秩访问`get`和`set`调用花费常数时间。其缺点是新项的插入和现有项的删除代价高昂。除非变动是在`ArrayList`末端进行。`LinkedList`类则提供了`List`的 **双向链表实现** 。使用`LinkedList`的优点在于，新项的插入和现有项的删除均开销很小，这里假设变动项的位置是已知的。这意味着，在表的前端进行添加和删除都是常数时间的操作，由此`LinkedList`提供了方法`addFirst`和`removeFirst`、`addLast`和 `removeLast`以及`getFrist`和`getLast`等方法高效地添加、删除和访问表两端的项。使用`LinkedList`的缺点是寻秩索引元素时时间成本昂贵，例如`get`和`set`操作，除非调用接近表端点。

`Vector`和`Stack`类是两个遗留类，其中`Stack`扩展了`Vector`。`Vector`具有和`ArrayList`基本相同的API，区别在于`Vector`所有方法都实现同步，也就是说`Vector`是线程安全的，这个特性在其子类`Stack`上也得到了体现。另外，`Stack`是栈这种数据结构的实现。因此，虽然`Stack`扩展了`Vector`，但是我们发现`Stack`类的API与`Vector`有很大不同。


