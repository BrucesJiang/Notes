# Java 8中的接口变化
Java 8中接口的更改增加了`静态方法`和`默认方法`特性。在Java 8之前，我们只能在接口中使用方法声明。但是从Java 8开始，我们可以在接口中使用默认方法和静态方法。

接口设计一直是一项艰巨的工作。因为如果我们想在接口中添加额外的方法，则需要对所有实现类进行更改。随着接口的老化，实现它的类的数量可能会增长到无法扩展接口的成都。这就是为什么在设计应用程序时，大多数框架提供了一个基本的实现类，然后我们扩展并覆盖适用于我们应用程序的方法。

本文将要介绍默认接口方法和静态接口方法以及Java 8中增加它们的原因。

## Java 接口默认方法
为了在Java接口中创建一个默认方法，我们需要用`default`关键字修饰方法签名(method signature)。

```java
public interface Interface1 {
    void method1(String str);
    default void log(String str) {
        System.out.println("Interface logging: " + str);
    }
}

```
上述代码中`log(String str)`是接口`Interface1`中的默认方法。当一个类实现接口`Interface1`时，并不强制为接口的默认方法提供实现。这个特性将帮助我们扩展接口和其他方法，我们需要做的只是提供一个默认实现。

如下另外一个接口:

```java
public interface Interface2 {
    void method2();
    default void log(String str) {
        System.out.println("Interfance logging: " + str);
    }
}
```
我们知道Java不允许我们扩展多个类，因为这样做会导致**钻石问题（Diamond Problem）**。所谓钻石问题指的是编译器无法决定使用那种超类方法。使用默认方法，钻石问题也会出现在`Interface2`中。如果一个类同时实现了`Interface1`和`Interface2`，并且没有实现通用的默认方法，编译器无法决定选择哪一个。

扩展多个接口是Java不可或缺的组成部分，我们可以在核心Java类以及大多数企业应用程序和框架中找到它。因此，为了确保这个问题不会发生在接口中，必须为接口的通用默认方法提供一个实现。因此，如果一个类正在实现上述两个接口，它将不得不为`log()`方法提供实现，否则编译器会抛出编译时错误（Complier Time Error）。

一个同时实现了`Interface1`和`Interface2`两个接口的类:

```java
public class Class implements Interface1, Interface2{
    @Override
    public void method1(){

    }

    @Override
    public void method2() {

    }

    @Override
    public void log(String str) {
        System.out.println("Class logging: " + str);
        Interface1.print("ABC");
        Interface2.print("CDE");
    }
}

```
Java接口默认方法的关键点：
1. Java接口的默认方法将帮助我们扩展接口，而不用担心会破坏实现类。
2. Java接口默认方法弥合了接口和抽象类之间的差异。
3. Java 8接口的默认方法将帮助我们避免实现类（utility class），因此所有的Collections类方法都可以在接口本身中提供实现。
4. Java接口的默认方法将帮助我们移除基类实现类，我们可以提供一个默认实现，并且实现类可以选择要覆盖哪一个。
5. 在接口中引入默认方法的主要原因之一是在Java 8中增强Collections API以支持lambda表达式。
6. 如果类继承体系中的任何类具有相同签名的方法，则默认方法变得不相关。默认方法不能从`java.lang.Object`中覆盖方法。推理非常简单，这是因为`Object`是所有Java类的基类。因此，即使我们将`Object`类的方法定义为接口中的默认方法，它也是无用的，因为子类始终使用`Object`类方法。这就是为什么要避免混淆的原因，接口中不能有覆盖`Object`类方法的默认方法。
7. Java接口默认方法也被称为`Defender Method`或虚拟扩展方法`Virtual Extension Method`。


## Java接口静态方法
除了我们不能在实现类中重写它们之外，Java接口静态方法与默认方法类似。 这个特性有助于我们避免在实现类中出现效果较差的实现方法。我们来看一个简单的例子。

```java
public interface Data {
    default void print(String str) {
        if(!isNull(str)) {
            System.out.println("Data print:" + str);
        }
    }

    static boolean isNull(String str) {
        System.out.println("Interface Null Check");
        return str == null ? true : "".equals(str)?true:false
    }
}

```
现在我们来看一个实现类，该类具有`isNull()`方法，但实现效果较差。
```java
public class DataImpl implements Data {

    public boolean isNull(String str) {
        System.out.println("Impl Null Check");

        return str == null ? true : false;
    }
    
    public static void main(String args[]){
        MyDataImpl obj = new MyDataImpl();
        obj.print("");
        obj.isNull("abc");
    }
}

```
注意，`isNull(String str)`是一种简单的类方法，它不会覆盖接口方法。 例如，如果我们将`@Override`注解添加到`isNull()`方法中，则会导致编译器错误。

现在，当我们运行应用程序时，我们得到以下输出。
```java
Interface Null Check
Impl Null Check
```
如果我们将接口方法从静态变为默认，我们将得到以下输出。
示例代码:
```java
public interface Data {
    default void print(String str) {
        if(!isNull(str)) {
            System.out.println("Data print:" + str);
        }
    }

    default boolean isNull(String str) {
        System.out.println("Interface Null Check");
        return str == null ? true : "".equals(str)?true:false
    }
}

public class DataImpl implements Data {
    
    @Override //注意这里一定要加上这个注解，表明是实现接口的
    public boolean isNull(String str) {
        System.out.println("Impl Null Check");

        return str == null ? true : false;
    }
    
    public static void main(String args[]){
        MyDataImpl obj = new MyDataImpl();
        obj.print("");
        obj.isNull("abc");
    }
}


```
运行结果：
```java
Impl Null Check
MyData Print::
Impl Null Check
```
Java接口静态方法仅对接口方法可见，如果我们从`DataImpl`类中移除`isNull()`方法，我们将无法将其用于`DataImpl`对象。然而像其他静态方法一样，我们可以使用类名称直接调用接口静态方法。 例如，一个有效的调用方式是：
```java
boolean result = Data.isNull("abc");
```

Java接口静态方法的关键点：
1. Java interface static method is part of interface, we can’t use it for implementation class objects.
2. Java interface static methods are good for providing utility methods, for example null check, collection sorting etc.
3. Java interface static method helps us in providing security by not allowing implementation classes to override them.
4. We can’t define interface static method for Object class methods, we will get compiler error as “This static method cannot hide the instance method from Object”. This is because it’s not allowed in java, since Object is the base class for all the classes and we can’t have one class level static method and another instance method with same signature.
5. We can use java interface static methods to remove utility classes such as Collections and move all of it’s static methods to the corresponding interface, that would be easy to find and use.

## Java Functional Interfaces
Before I conclude the post, I would like to provide a brief introduction to Functional interfaces. An interface with exactly one abstract method is known as `Functional Interface`.

A new annotation `@FunctionalInterface` has been introduced to mark an interface as Functional Interface. `@FunctionalInterface` annotation is a facility to avoid accidental addition of abstract methods in the functional interfaces. It’s optional but good practice to use it.

Functional interfaces are long awaited and much sought out feature of Java 8 because it enables us to use lambda expressions to instantiate them. A new package java.util.function with bunch of functional interfaces are added to provide target types for lambda expressions and method references. We will look into functional interfaces and lambda expressions in the future posts.

