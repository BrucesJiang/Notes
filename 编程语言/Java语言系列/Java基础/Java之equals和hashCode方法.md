# Java之equals和hashCode方法

`equals()`和`hashCode()`方法是`Object`类的实例方法，原生的`hashCode()`和`equals()`方法, 具体实现如下：
- `hashCode()`: 根据对象内存地址换算出来的一个值。
- `equals()`：严格判断一个对象是否相等（`object1 == object2`）。

### 为什么需要重写equals方法？

在我们的业务系统中判断对象时有时候需要的不是一种严格意义上的相等，而是一种业务上的对象相等。在这种情况下，原生的`equals`方法就不能满足我们的需求了所以这个时候我们需要重写`equals`方法，来满足我们的业务系统上的需求。

## 为什么需要同时重写equals和hashCode方法

我们先来看一下`Object.hashCode()`的通用约定（摘自《Effective Java》第45页）

1. 在一个应用程序执行期间，如果一个对象的`equals`方法做比较所用到的信息没有被修改的话，那么，对该对象调用`hashCode`方法多次，它必须始终如一地返回同一个整数。在同一个应用程序的多次执行过程中，这个整数可以不同，即这个应用程序这次执行返回的整数与下一次执行返回的整数可以不一致。
2. 如果两个对象根据equals(Object)方法是相等的，那么调用这两个对象中任一个对象的hashCode方法必须产生同样的整数结果。
3. 如果两个对象根据equals(Object)方法是不相等的，那么调用这两个对象中任一个对象的hashCode方法，不要求必须产生不同的整数结果。然而，程序员应该意识到这样的事实，对于不相等的对象产生截然不同的整数结果，有可能提高散列表（hash table）的性能。
4. 如果只重写了equals方法而没有重写hashCode方法的话，则会违反约定的第二条：相等的对象必须具有相等的散列码（hashCode）同时对于HashSet和HashMap这些基于散列值（hash）实现的类。HashMap的底层处理机制是以数组的方法保存放入的数据的(Node<K,V>[] table)，其中的关键是数组下标的处理。数组的下标是根据传入的元素hashCode方法的返回值再和特定的值异或决定的。如果该数组位置上已经有放入的值了，且传入的键值相等则不处理，若不相等则覆盖原来的值，如果数组位置没有条目，则插入，并加入到相应的链表中。检查键是否存在也是根据hashCode值来确定的。所以如果不重写hashCode的话，可能导致HashSet、HashMap不能正常的运作、
5. 如果我们将某个自定义对象存到HashMap或者HashSet及其类似实现类中的时候，如果该对象的属性参与了hashCode的计算，那么就不能修改该对象参数hashCode计算的属性了。有可能会移除不了元素，导致内存泄漏。

扩展：

在重写`equals`方法的时候，需要遵守下面的通用约定：

1. 自反性。 对于任意的引用值x，x.equals(x)一定为true。
2. 对称性。 对于任意的引用值x和y，当且仅当y.equals(x)返回true时，x.equals(y)也一定返回true。
3. 传递性。 对于任意的引用值x、y和z，如果x.equals(y)返回true，并且y.equals(z)也返回true，那么x.equals(z)也一定返回true。
4. 一致性。 对于任意的引用值x和y，如果用于equals比较的对象没有被修改的话，那么，对此调用x.equals(y)要么一致地返回true，要么一致的返回false。
5. 对于任意的非空引用值x，x.equals(null)一定返回false。
     
重写hashCode方法的大致方式：
1. 把某个非零常数值，比如说17（最好是素数），保存在一个叫result的int类型的变量中。
2. 对于对象中每一个关键域f（值equals方法中考虑的每一个域），完成一些步骤：
    1. 为该域计算int类型的散列吗c：
        - 如果该域是boolean类型，则计算（f？0:1）。
        - 如果该域是byte、char、short或者int类型，则计算（int）f。
        - 如果该域是float类型，则计算Float.floatToIntBits(f)。
        - 如果该域是long类型，则计算（int）（f ^ (f>>>32)）。
        - 如果该域是double类型，则计算Double.doubleToLongBits(f)得到一个long类型的值，然后按照步骤4，对该long型值计算散列值。
        - 如果该域是一个对象引用，并且该类的equals方法通过递归调用equals的方式来比较这个域，则同样对这个域递归调用hashCode。如果要求一个更为复杂的比较，则为这个域计算一个“规范表示”，然后针对这个范式表示调用hashCode。如果这个域的值为null，则返回0（或者其他某个常数）
        - 如果该域是一个数组，则把每一个元素当做单独的域来处理。也就是说，递归地应用上述规则，对每个重要的元素计算一个散列码，然后根据步骤下面的做法把这些散列值组合起来。
    2. 按照下面的公式，把步骤1中计算得到的散列码C组合到result中： result = 31*result+c。
3. 返回result。
4. 写完hashCode方法之后，问自己“是否相等的实例具有相等的散列码”。如果不是的话，找出原因，并修改。

另外，可以通过org.apache.commons.lang.builder.HashCodeBuilder这个工具类来方便的重写hashCode方法。