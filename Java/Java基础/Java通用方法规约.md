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
| byte\|char\|shrot\|int\| (int)v |
| Long                   | (int)(v^(v>>>32))|
| Float                  | Float.vloatToIntBits(v)|
| Double                 | Double.doubleToLongBits(v) |
| String                 | s[0]\*31^(n-1) + s[1]\*31^(n-2) + ... + s[n-1] |
| 数组                   | Arrays.hashCode |