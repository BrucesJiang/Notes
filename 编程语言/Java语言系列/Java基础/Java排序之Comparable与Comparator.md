# Comparable和Comparator的比较
Java语言提供了两种对集合对象或者数组对象的排序方式：
- Comparable接口 -- 通过在集合内部定义的方法实现排序
- Comparator接口 -- 通过在集合外部定义的方法实现排序

## Comparable接口
`Comparable`位于`java.lang`包下。如果一个类的实例对象本身支持自比较，那么这个类需要实现该接口。例如，JDK提供的String、Integer本身就可以完成比较大小的操作，它们本身已经实现了`Comparable`接口。

`Comparable`接口的定义如下:

```java
pakcage java.lang;
import java.util.*;
public interface Comparable<T> {
    public int compareTo(T o);
}
```

实例

```java
public class Man implements Comparable<Man>{
    private int age;
    private String name;
 
    public Man(String name, int age)
    {
        this.name = name;
        this.age = age;
    }
    @Override
    public int compareTo(Man o)
    {
        return this.age-o.age;
    }
    @Override
    public String toString()
    {
        return name+":"+age;
    }
}

```
可以看到Man实现了Comparable接口中的compareTo方法。实现Comparable接口必须修改自身的类，即在自身类中实现接口中相应的方法。
```java
public static void main(String[] args){
    Man man0 = new Man("zzh",18);
    Man man1 = new Man("jj",17);
    Man man2 = new Man("qq",19);

    List<Man> list = new ArrayList<>();
    list.add(man0);
    list.add(man1);
    list.add(man2);

    System.out.println(list);
    Collections.sort(list);
    System.out.println(list);
}

```


## Comparator接口
`Comparator`位于`java.util`包下。如果需要控制某个类实例对象的次序，而类本身不支持排序(也就是说类没有实现Comparable接口)，我们可以创建一个该类的外部比较器来实现排序功能。此时，通过comparator接口就可以是实现一个外部比较器。

如果引用的为第三方jar包，这时候，没办法改变类本身，可是使用这种方式。Comparator是一个专用的比较器，当这个对象不支持自比较或者自比较方法不能满足要求时，可写一个比较器来完成两个对象之间大小的比较。

Comparator体现了一种 **策略模式(strategy design pattern)**，就是不改变对象自身，而用一个策略对象(strategy object)来改变它的行为。

`Comparator`接口的定义如下：
```java
package java.util;
public interface Comparator<T> {
    int compare(T o1, T o2);
    boolean equals(Object obj);
}
```

代码示例

```java
public final class Woman{
    private int age;
    private String name;
 
    public Woman(String name, int age)
    {
        this.name = name;
        this.age = age;
    }
    
    @Override
    public String toString()
    {
        return name+":"+age;
    }
}

```
排序实现

```java
public static void main(String[] args){
   Woman p1 = new Woman("zzh",18);
   Woman p2 = new Woman("jj",17);
   Woman p3 = new Woman("qq",19);
   List<Woman> list2 = new ArrayList<Woman>();
   list2.add(p1);
   list2.add(p2);
   list2.add(p3);
   System.out.println(list2);
   Collections.sort(list2,new Comparator<Woman>(){

       @Override
       public int compare(Woman o1, Woman o2)
       {
           if(o1 == null || o2 == null)
               return 0;
           return o1.getAge()-o2.getAge();
       }

   });
   System.out.println(list2);
}
```
在上述代码中，`public static <T> void sort(List<T> list, Comparator<? super T> c)`采用了内部类实现方式，实现`compare`方法对`Woman`实例对象的list进行排序。


## 比较
从上述定义和代码示例可以发现：`Comparable`对实现它的类的实例对象进行整体排序，而`Comparator`是一个专用比较器，当某个对象不支持自比较或者比较方法不能满足排序要求是，可以通过`Comparator`实现一个专用比较器来实现两个对象之间大小的比较。我们可以说，`Compoarator`通过策略模式(strategy design pattern)，在不改变对象自身的情况，用一个策略对象(strategy object)来改变排序对象的行为。例如，在用`Collections`类的`sort`方法进行排序时，如果自定义类不指定排序器(通过实现Comparator)，那么就会执行**自然顺序排序**。如下列Collections API: `Sorts the specified list into ascending order, according to the natural ordering of its elements. All elements in the list must implement the Comparable interface`。 这里所谓的`自然顺序`就是实现 `Comparable`接口设定的排序方式。如果自定义类没有实现`Comparable`接口，那么就必须指定排序器。

若一个类实现了Comparable 接口，实现 Comparable 接口的类的对象的 List 列表 ( 或数组)可以通过 Collections.sort（或 Arrays.sort）进行排序。此外，实现 Comparable 接口的类的对象 可以用作 “有序映射 ( 如 TreeMap)” 中的键或 “有序集合 (TreeSet)” 中的元素，而不需要指定比较器。

##### 注意
在Comparator接口中定义了两个方法，为什么继承的时候只实现了一个方法 ？
实际上，当一个类没有显式继承父类的时候，会有一个默认的父类，即`java.lang.Object`，在Object类中有一个方法即为equals方法，所以这里并不强制要求实现Comparator接口的类要实现equals方法，直接调用父类的即可，虽然显式的实现了equals()方法会更好。

在《Effective Java》一书中，作者Joshua Bloch推荐大家在编写自定义类的时候尽可能的考虑实现一下`Comparable`接口。因为，一旦实现了`Comparable`接口，它就可以跟许多泛型算法以及依赖于改接口的集合实现进行协作。我们付出很小的努力就可以获得非常强大的功能。

事实上，Java平台类库中的所有值类都实现了`Comparable`接口。如果我们正在编写一个值类，它具有非常明显的内在排序关系，比如按字母顺序、按数值顺序或者按年代顺序，那就应该坚决考虑实现这个接口。`compareTo`方法不但允许进行简单的等同性进行比较，而且语序执行顺序比较，除此之外，它与Object的equals方法具有相似的特征，它还是一个泛型。类实现了Comparable接口，就表明它的实例具有内在的排序关系，为实现Comparable接口的对象数组进行排序就这么简单： Arrays.sort(a);对存储在集合中的Comparable对象进行搜索、计算极限值以及自动维护也同样简单。列如，下面的程序依赖于String实现了Comparable接口，它去掉了命令行参数列表中的重复参数，并按字母顺序打印出来, 例如：

```java
public class WordList{
    public static void main(String args[]){
        Set<String> s = new TreeSet<String>();
        Collections.addAll(s,args);
        System.out.println(s);
    }
}
```

Comparable 是排序接口；若一个类实现了 Comparable 接口，就意味着`该类支持排序`。而 Comparator 是比较器；我们若需要控制某个类的次序，可以建立一个 `该类的比较器`来进行排序。前者应该比较固定，和一个具体类相绑定，而后者比较灵活，它可以被用于各个需要比较功能的类使用。可以说前者属于 **静态绑定**，而后者可以 **动态绑定**。我们不难发现：`Comparable` 相当于 **内部比较器**，而 `Comparator` 相当于 **外部比较器**。

## 总结
1. 如果类没有实现`Comparable`接口，又想对两个类的实例对象进行比较(或者类实现了`Comparable`接口，但是`compareTo`方法实现的比较算法不能满足需求)， 那么可以实现`Comparator`接口，自定义一个比较器。
2. 实现Comparable接口的方式比实现Comparator接口的耦合性 要强一些，如果要修改比较算法，要修改`Comparable`接口的实现类，而实现`Comparator`的类是在外部进行比较的，不需要对实现类有任何修改。从这个角度说，其实有些不太好，尤其在我们将实现类的`.class`文件打成一个`.jar文`件提供给开发者使用的时候。实际上实现`Comparator` 接口的方式后面会写到就是一种典型的策略模式