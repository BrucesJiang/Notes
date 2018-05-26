# JUC的13个原子类
Java 5后提供了`java.util.concurrent.atomic`包，这个包中的原子类操作提供了一种用法简单、性能高效、线程安全的方法更新一个变量。这其中有13个类隶属于4中类型的的原子更新方法，分别是 **原子更新基本类型、原子更新数组、原子更新引用和原子更新属性（字段）**，该包中的类基本都是使用`Unsafe`实现的包装类。

## 原子更新基本类型类
使用原子的方式更新基本类型，`java.util.concurrent.atomic`包提供了以下3个类：

1. AtomicBoolean 原子更新布尔类型
2. AtomicInteger 原子更新整型
3. AtomicLong    原子更新长整型

这三个类几乎提供了相同的API，这里仅仅对AtomicInteger加以说明；

- int addAndGet(int delta) 以原子方式将输入的数值与实例中的值（AtomicInteger里的value）相加，并返回结果
- boolean compareAndSet(int expect, int update): 如果输入的值等于预期值，则以原子方式将该值设置为输入值。
- int getAndIncrement() 以原子方式将当前值加1。注意返回的是自增前的值。
- void lazySet(int newValue) 最终会设置成newValue，使用lazySet设置值后，可能导致其他线程在之后的一小段时间内还可以读到旧的值。

## 原子更新数组
通过原子方式更新数组里的某个元素，`java.util.concurrent.atomic`包提供了以下4个类：

1. AtomicIntegerArray   原子更新整型数组里的元素
2. AtomicLongArray      原子更新长整数类型数组里的元素
3. AtomicReferenceArray 原子更新引用类型数组里的元素
4. AtomicIntegerArray   提供原子类型的方式更新数组里的整型，其常用方法如下：
    - int addAndGet(int i, int delta): 以原子方式输入值与数组中索引i的元素相加
    - boolean compareAndSet(int i, int expect, int update): 如果当前值等于预期值，则以原子方式将数组位置i的元素设置成upate值。

## 原子更新引用类型
原子更新基本类型的AtomicInteger, 只能更新一个变量，如果要原子更新多个变量，就需要使用这个原子更新引用类型提供的类。`java.util.concurrent.atomic`包提供了以下三个类：

1. AtomicReference 原子更新引用类型
2. AtomicReferenceFieldUpdater: 原子更新引用类型里的字段
3. AtomicMarkableReference: 原子更新带有标记位的引用类型。可以原子更新一个布尔类型的标记为和引用类型。构造方法是`AtomicMarkableReference(V initialRef, boolean initialMark)`。

## 原子更新字段类  
如果需要原子的更新某个类里的某个字段时，就需要使用原子更新字段类，`java.util.concurrent.atmoic`包提供了以下3个类进行原子字段更新：

1. AtomicIntegerFieldUpdater 原子更新整型的字段的更新器
2. AtomicLongFieldUpdater    原子更新长整型字段的更新器
3. AtomicStampedReference    原子更新带有版本号的引用类型。该类型将整数值与引用关联起来，可用于原子的更新数据和数据的版本号，可以解决使用CAS进行原子更新可能出现的ABA问题。

