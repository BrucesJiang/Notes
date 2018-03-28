# Java中接口与抽象类比较
抽象类与接口的区别是流行的面试问题之一。抽象类和接口是Java编程语言的核心部分。 选择接口还是抽象类是每个设计师面临的设计决策。

## 抽象类和接口之间的差异
1. `abstract`关键字可以修饰类（抽象类）和方法（抽象方法），`abstract`修饰的方法只能存在于抽象类中（这是不对的，接口中的方法也可以修饰，只是abstract在修饰接口中的方法时不起作用，因为该方法隐含为绝对抽象）；`interface`关键字只能声明接口
2. 子类用`extends`关键字扩展抽象类，并且子类需要实现抽象父类中的所有抽象方法，除非子类也是抽象类；子类用`implements`关键字实现接口，并且子类需要实现接口声明的所有方法。
3. 抽象类中可以包含实现方法，但是接口中除了`default`和`static`方法外(Java 8中添加的新特性)，所有方法必须保证绝对抽象。
4. 抽象类含有构造方法，但是接口不含构造方法
5. 抽象类具有普通Java类的所有功能，除了我们不能实例化它。我们可以使用`abstract`关键字来创建一个类的抽象，但接口是一个完全不同的类型，只能有公共静态的常量和公共的方法声明。
6. 抽象类方法的访问修饰符(access modifier)可以设置为`public, private, protected, static`；接口方法的访问修饰符可以设置为`public, abstract` 
7. 一个子类只能扩展一个抽象类()，但是能够实现多个方法
8. 抽象类可以扩展其它类，也能实现接口，但是接口只能扩展其他接口(并且这种扩展是多重继承)
9. 如果一个抽象类拥有类入口方法`main()`，我们就可以运行它，但是接口不能，因为不能含有`main`方法实现。(**但是，这个区别在Java 8之后就没有了**)
10. 接口用于定义子类的契约，而抽象类也定义契约，但它可以为子类提供其他方法实现。

我们展示一些特性:
```java
public interface A extends B, C{
    
    public static void main(String args[]){
        try{
            throw new IOException("Hello");
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(int[] args){ //重载
        System.out.println("ddd");
    }
    
    
    public void a(); //abstract是无效的，因为interface中的方法默认是绝对抽象的
    public abstract void b();
}

```

## 接口 & 抽象类
选择接口还是抽象类为子类提供契约是一个设计决策，取决于很多因素，让我们看看接口是什么时候是最佳选择，什么时候我们可以使用抽象类。

1. Java不支持多级继承，所以每个类只能扩展一个超级类。但是一个类可以实现多个接口。所以大部分时间接口对于为类继承体系和契约提供基础是一个很好的选择。在接口方面编码也是java编程的最佳实践之一。
2. 如果合同中有很多方法，那么抽象类就更有用了，因为我们可以为所有子类通用的一些方法提供默认实现。 同样，如果子类不需要实现特定的方法，他们可以避免提供实现，但在接口的情况下，子类将不得不为所有方法提供实现，即使它没有用，实现只是空块。
3. 如果我们的基础契约不断变化，那么接口可能会导致问题，因为我们无法在不更改所有实现类的情况下向接口声明其他方法，而抽象类可以提供默认实现并仅更改实际实现的实现类 使用新的方法。

Actually most of the times, using Interfaces and abstract classes together is the best approach for designing a system, for example in JDK java.util.List is an interface that contains a lot of methods, so there is an abstract class java.util.AbstractList that provides skeletal implementation for all the methods of List interface so that any subclass can extend this class and implement only required methods.

We should always start with an interface as base and define methods that every subclasses should implement and then if there are some methods that only certain subclass should implement, we can extend the base interface and create a new interface with those methods. The subclasses will have option to chose between the base interface or the child interface to implement according to its requirements. If the number of methods grows a lot, its not a bad idea to provide a skeletal abstract class implementing the child interface and providing flexibility to the subclasses to chose between interface and abstract class.