Java通用方法规约源自JDK原码注释和Effective Java（作者Joshua Bloch）。

Java中的Object的equals, hashCode, clone, toString方法，Comparable.compareTo方法应该遵从一定的规约，有利于数据的存取、格式化和排序等，这些方法的规约在JDK源代码注释中都有描述。并且，该规约可以解决以下问题：

1. Object的hashCode方法返回的是什么
2. HashMap中数据的存取原理
3. 如何降低哈希冲突的概率
4. 如果JavaBean中有两个关键字段userGroupId和userId，那么应该如何实现其hashCode方法和equals方法
5. Double, String, 数组的hashCode是如何实现的。

## Object.equals(Object)
equals方法用于判断某个实例是否与当前实例逻辑相等。

无需重写equals方法的场景：
1. 类的每个实例本身是唯一的，例如 Thread, Enum
2. 父类已经重写了equals方法，并且该equals方法适用于子类。如AbstractList, AbstractMap等
3. 无需逻辑判断相等。例如， java.util.Collections,只提供方法，不提供数据

### equals方法规约
1. `自反性（reflexive）`: 对任何非空(`null`)的引用值`x`, `x.equals(x)`必须返回`true`
2. `对称性（symmetric）`: 对任何非空(`null`)的引用值`x`和`y`，当且仅当`y.equals(x)`返回`true`，且`x.equals(y)`必须返回`true`
3. `传递性（transitive）`: 对于任何非空(`null`)的引用值`x`,`y`和`z`，如果`x.equals(y)`返回`true`,且`y.equals(z)`返回`true`,那么`x.equals(z)`也必须返回`true`。
4. `一致性（consistent）`: 对于任何非空(`null`)的引用值`x`和`y`,只有`equals`的比较操作在实例中所用的信息没有被修改，多次调用`x.equals(y)`总会一致地返回相同的结果
5. `非空性（Non-nullity）`: 对于任何非空(`null`)的引用值`x`，`x.equals(null)`必须返回`false`.


### JDK equals实现参考：
- String.equals(Object) //比较实例->比较字符串长度->遍历char[]逐个比较
- Date.equals(Object) //比较毫秒数
- Arrays.equals(Object[],Object[]) //比较实例->比较数组长度->遍历数组逐个比较

## Object.hashCode()
hashCode方法返回实例的哈希码,用于支持散列表如HashMap等。**默认实现是将实例内部地址转化为int值。**

hash（散列）可以理解为将数据有规则的分散开来。为何要分散开？假设HashMap中很多Key的HashCode相同，这时map中会存在一个链表特别长，这样会严重影响map的性能。如果hashCode方法设计的足够巧妙，使得map中的数据均匀的散列到各个列表中，那么这个map的性能会相当好。

### hashCode方法规约
1. 同一实例上调用多次,hashCode方法必须返回相同的int值
2. 如果`ObjectA.equals(ObjectB)`为真，那么`ObjectA.hashCode()`等于`ObjectB.hashCode()`
3. 如果`ObjectA.equals(ObjectB)`为假，则不要求`ObjectA.hashCode()`等于`ObjectB.hashCode()`。但是，为不同的实例产生不同的hashCode可以提供HashMap等容器的性能。

### 常见数据类型的Hash算法
| 数据类型 | hash算法  |
| :----------------------|:---------------|
| Boolean                | (v?1:0) |
| byte\|char\|shrot\|int | (int)v |
| Long                   | (int)(v^(v>>>32))|
| Float                  | Float.vloatToIntBits(v)|
| Double                 | Double.doubleToLongBits(v) |
| String                 | s[0]\*31^(n-1) + s[1]\*31^(n-2) + ... + s[n-1] |
| 数组                   | Arrays.hashCode |

## Object.clone()
创建并返回当前实例的副本。

### clone方法规约
1. 对于任意实例`x`, `x.clone() != x`
2. 对于任意实例`x`, `x.clone().equals(x)`

需要注意的是`Object.clone`是`proctected`方法，要实现克隆必须重写该方法并实现 `Cloneable(空接口，仅作为标识)`，否则会抛出`CloneNotSupportedException`例外。

## Comparable.compareTo(T)
实现了`Comparable`接口的类的实例可以互相比较大小， `compareTo`方法返回负整数、0和正整数对英语当前实例小于、等于喝大于指定实例

实现该方放用于数组或集合元素的排序，例如：
- java.util.Arrays.sort(Object[]) (该方法在排序时会将元素强制转换为**Comparable**，如果没有实现**Comparable**接口的方法，则会抛出转换异常)
- java.util.Collections的方法`<T extends Comparable<? super T>> void sort(List<T> list)`

### compareTo方法规约
1. `x.compareTo(y) == y.compareTo(x)`, 如果`x.compareTo(y)`抛出异常则`y.compareTo(x)`也应该抛出异常
2. `传递性`： 如果`x.compareTo(y)>0 && y.compareTo(z) > 0`为真，则`x.compareTo(z) > 0`为真
3. 如果`x.compareTo(y) == 0`，则`x.compareTo(z) == y.compareTo(z)` 
4. 强烈推荐但是非必需`(x.compareTo(y) == 0) == (x.equals(y))`

与`Comparable`功能相似的接口有`Comparator`，区别是前者需要在类内部实现`Comparable`接口，而`Comparator`通常是作为函数式接口（`@FunctionalInterface`）使用。

## Object.toString()
返回当前实例的String表示格式

Object.toString()方法的默认返回格式为`className + @+ 十六进制的hashCode`,也就是 `getClass().getName() + @ + Integer.toHexString(hashCode())`