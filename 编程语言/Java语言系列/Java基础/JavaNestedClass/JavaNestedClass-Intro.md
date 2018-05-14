# Java嵌套类简介
本系列文章主要总结了Java嵌套类的概念及其特性。

首先，什么是Java嵌套类，[官方文档](https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html)中给出的定义是: **Define a class within another class.** ， 用中文表述就是： `可以将一个类的定义放到另外一个类的定义内部`。 需要注意的是嵌套类的概念和组合是完全不同的概念。

例如， 

```java
public class Outer {
    public class InnerClass {
        // inner fields, methods, etc.
    }
    public InnerClass getInnerInstance(){
        return new InnerClass();
    }

    public static StaticNestedClass {
        //inner fields, methods, etc.
        
    }
    public static void main(String[] args) {
        Outer outer = new Outer();
        Outer.InnerClass innerClass = outer.new InnterClass();
        //或者
        Outer.InnerClass innerClass = outer.getInnerInstance();
    
        Outer.StaticNestedClass
    }
}

```

嵌套类(nested class)被分为两类： `static`和`non-static`。 被声明为 `static`类型的嵌套类(nested class)被称作静态嵌套类`static nested class`。 `non-static`嵌套类(nested class)被称为内部类`(inner class)`。

一个嵌套类(nested class)是其外围类(enclosing class)的成员。 非静态嵌套类`Non-static nested class`(内部类， inner class)拥有其外围类(enclosing class)所有成员的访问权，即使它们被声明为`private`。`static nested class`静态嵌套类无权访问其外围类的任何成员。 作为外围类的一个成员， 嵌套类`nested class`可以被声明为`private`、`public`、`protected`或者`package private`类型。 能够在其它类中被调用的，只能声明为`public`或`package private`类型。

在官方文档中，提及嵌套类的优点：

1. It is a way of logically grouping classes that are only used in one place: If a class is useful to only one other class, then it is logical to embed it in that class and keep the two together. Nesting such "helper classes" makes their package more streamlined.
2. It increases encapsulation: Consider two top-level classes, A and B, where B needs access to members of A that would otherwise be declared private. By hiding class B within class A, A's members can be declared private and B can access them. In addition, B itself can be hidden from the outside world.
3. It can lead to more readable and maintainable code: Nesting small classes within top-level classes places the code closer to where it is used.

另外，更为重要的是，1） **作为接口机制的补充，实现多重继承。** 因为Java不支持多继承，支持实现多个接口。但有时候会存在一些使用接口很难解决的问题，这个时候我们可以利用内部类提供的、可以继承多个具体的或者抽象的类的能力来解决这些程序设计问题。可以这样说，接口只是解决了部分问题，而内部类使得多重继承的解决方案变得更加完整。 2） **更可靠的实现事件驱动系统。** 用来开发GUI的Java Swing使用了大量内部类，主要用来响应各种事件。Swing的工作就是在事件就绪的时候执行事件，至于事件具体怎么做，这由事件决定。这里面有两个问题：1.事件必须要用到继承2.事件必须能访问到Swing。所以必须把事件写成内部类。 3）**能够更加安全、可靠的实现闭包与回调。**  内部类是面向对象的闭包，因为它不仅包含创建内部类的作用域的信息，还自动拥有一个指向此外围类对象的引用，在此作用域内，内部类有权操作所有的成员，包括private成员。一般使用一个库或类时，是你主动调用人家的API，这个叫Call，有的时候这样不能满足需要，需要你注册（注入）你自己的程序（比如一个对象)，然后让人家在合适的时候来调用你，这叫Callback。当父类和实现的接口出现同名函数时，你又不想父类的函数被覆盖，回调可以帮你解决这个问题。

## 内部类的特点
如上代码所示，如果要实例化一个内部类对象，我们需要首先实例化要给外围类实例对象，然后使用下列语法创建一个内部类实例对象：

```java
OuterClass.InnerClass innerObject = outerObject.new InnerClass();
```

内部类是一种隐藏和组织代码的形式，下面我们将描述内部类的一些特点。

### 提供了到外围类的链接
我们知道外围类的创建，伴随者内部类的生成。在生成一个内部类对象时，该对象与创建它的外围类实例对象（enclosing object）之间会产生一种联系，所以它能访问其外围对象的所有成员，而且不需要任何条件，包括被声明为`private`的成员。（这与C++的嵌套类的设计非常不同，在C++中知识单纯的名字隐藏机制，与外围类没有联系，也没有隐含的访问权）我们举个例子来说明情况：

```java
public interface Selector {
    boolean end();
    Object current();
    void next();
}

public class Sequence {
    private Object[] items;
    private int next;
    public Sequence(int size) {
        items = new Object[size];
    }

    public void add(Object x) {
        if(next < items.length) {
            items[next++] = x;
        }
    }

    public Selector selector() {
        return new SequenceSelector();
    }
    
    private class SequenceSelector implements Selector {
        private int i = 0;
        @Override public Object current() {
            return items[i];
        }
        @Override public boolean end() {
            return i == items.length;
        }
        @Override public void next() {
            if(i < items.length ) i ++;
        }
    }
    
    public static void main(String[] args) {
        Sequence sequence  = new Sequence(10);
        for(int i = 0; i < 10; i ++) {
            sequence.add(Integer.toString(i));
        }
        Selector selector = sequence.selector();
        while(!selector.end()) {
            System.out.println(selector.current() + " ");
            selector.next();
        }
    }
}
```
我们在上述代码中，利用“迭代器”设计模式实现了一个例子，用于展示 **内部类自动拥有对其外围类所有成员的访问权**。 其原因在于，当某个外部类对象创建其内部类对象时，该内部类对象必定会捕获一个指向外围类对象的引用。因此，当在内部类中访问其外围类成员时，就是使用了创建时捕获的外围类对象引用。 我们需要声明的一点是正如我们所看到的： **内部类的对象只能在与其外围类对象相关的情况下才能被创建，如果嵌套类是静态的(static nested class),则无法创建该引用。**

### 内部类的使用方式--向上转型
我们将内部类向上转型为其基类，尤其是转型为接口时，就会发现内部类的可用性。我们知道，从实现了某个接口的对象得到该接口的引用与向上转型为这个对象的基类，本质上效果是一样的。 这种用法我们在上述`Sequence`类的编写中已经用到。通过这种写法，我们内部类能够被定义为`private`，使得编程人员能够很好的隐藏类的实现细节。

另外，还有两种特殊的内部类： **局部内部类**和 **匿名内部类**， 我们将单独总结。

## 静态嵌套类(Static Nest Class)
如果不需要内部类的实例对象与外围类之间有联系，那么可以将嵌套类声明为`static`。 区别于内部类(inner class)对象隐式地保存了一个外围类对象引用； 当内部类是`static`类型时，便没有该引用了。这也就意味着：

1. 要创建静态嵌套类的实例对象，并不需要其外围类的对象
2. 不能从静态嵌套类的实例对象中访问非静态的外围类对象

```java
public class Parcel {
    private static class ParcelContents implements Contents {
        private int = 11;
        public int value() { return i;}
    }

    protected static class ParcelDestination implements Destination {
        private String label;
        private ParcelDestination(String whereTo) {
            label = whereTo;
        }
    }
    public String readLabel(){ return label;}
    //Nested classes can contain other static elements
    public static void f(){}
    static int x = 10;
    static class AnotherLevel{
        public static void f() {
            static int x = 10;
        }
    }
    public static Destination destination(String s) {
        return new ParcelDestination(s);
    }
    public static Contents contants() {
        return new ParcelContants();
    }

    public static void main(String[] args) {
        Contants c = contants();
        Destination d = destination("Haha");
    }
}
```
与类方法和变量一样，静态嵌套类与其外部类相关联。 像静态类方法一样，静态嵌套类不能直接引用其封闭类中定义的实例变量或方法：它只能通过对象引用来使用它们。

下面是官方文档给出的方法：

Static nested classes are accessed using the enclosing class name:
```java
OuterClass.StaticNestedClass
```
For example, to create an object for the static nested class, use this syntax:
```java
OuterClass.StaticNestedClass nestedObject = new OuterClass.StaticNestedClass();
```
此时，我们可以将静态嵌套类当作是一个静态方法。

## 接口内部的类-- 静态嵌套类
正常情况下，不能在接口内不放置任何代码，但是静态嵌套类可以作为接口的一部分。接口中的任何类都默认的是`static`和`public`的。


