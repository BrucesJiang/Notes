# Java的接口
Java中的接口提供了一种实现抽象的方法。Java接口也用于定义子类实现的契约。

例如，假设我们要创建一个由多个形状组成的图形。 在这里，我们可以创建一个接口`Shape`并定义不同类型的`Shape`对象将实现的所有方法。 为了简单起见，我们只能保留两种方法 - `draw()`绘制形状和`getArea()`，它将返回形状的区域。

## Java 接口示例

```java
public interface Shape {

    //implicitly public, static and final
    public String LABLE="Shape";
    
    //interface methods are implicitly abstract and public
    void draw();
    
    double getArea();
}
```

## Java接口的关键点
- `interface`关键字用于构建接口
- 接口不能被实例化
- 接口提供了一种绝对的抽象。抽象类提供了一种不彻底的抽象，也就是说抽象类中可以包含抽象方法和非抽象方法，但是接口中必须全是抽象方法。
- 由于我们无法实例化接口，接口不能拥有构造方法
- 默认情况下，接口的任何属性都是`public`，`static` 和 `final`，所以我们不需要为属性提供访问修饰符，即使提供了访问修饰符，编译器也不会编译它。
- 一个接口不能扩展任何类，但可以扩展另一个接口。 `public interface Shapeextends Cloneable {}`是扩展另一个接口的接口示例。**实际上，Java在接口中提供了多重继承，这意味着一个接口可以扩展多个接口**。
- `implements`关键字被类用来实现一个接口。
- 实现接口的类必须为其所有方法提供实现，除非它是抽象类。 例如，我们可以像这样在抽象类中实现上面的接口：ShapeAbs.java
- 我们应该总是试图用接口而不是实现来编写程序，以便我们事先知道实现类将始终提供实现，并且在将来如果有更好的实现到达，我们可以轻松地切换到实现。

```java
public abstract class ShapeAbs implements Shape {
    @Override
    public double getArea() {
        // TODO Auto-generated method stub
        return 0;
    }
}
```

## 接口实现的示例
```java
public class Circle implements Shape {

    private double radius;

    public Circle(double r){
        this.radius = r;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing Circle");
    }
    
    @Override
    public double getArea(){
        return Math.PI*this.radius*this.radius;
    }

    public double getRadius(){
        return this.radius;
    }
}
```
`Circle`类实现了接口中定义的所有方法，并且它也有一些自己的方法，如`getRadius()`。 接口实现可以有多种类型的构造函数。 让我们看看`Shape`接口的另一个接口实现。

```java
public class Rectangle implements Shape {

    private double width;
    private double height;
    
    public Rectangle(double w, double h){
        this.width=w;
        this.height=h;
    }
    @Override
    public void draw() {
        System.out.println("Drawing Rectangle");
    }

    @Override
    public double getArea() {
        return this.height*this.width;
    }
}

```


## 接口的优劣
1. 优势
  - `interface`为所有的实现类提供了一个契约，所以它在接口方面的编码很好，因为实现类不能删除我们正在使用的方法。
  - 接口对于定义类型和在代码中创建顶级层次结构的起点是很好的。
  - 由于java类可以实现多个接口，因此在大多数情况下最好使用接口作为超类。
2. 劣势
  - 在设计我们的项目时，我们需要非常小心地选择接口方法，因为我们不能在接下来的时间点添加从接口中删除的任何方法，它将导致所有实现类的编译错误。有时这会导致我们的代码中扩展基本接口的接口很多，难以维护。
  - 如果实现类有它自己的方法，我们不能直接在我们的代码中使用它们，因为`Object`类型是一个没有这些方法的接口。例如，在上面的代码中，我们将得到代码`shape.getRadius()`的编译错误。为了克服这个问题，我们可以使用类型转换并使用像这样的方法,虽然类类型转换有其自身的缺点。
  
```java
Circle c = (Circle) shape;
c.getRadius();
```

**注意:** Java 8通过引入默认方法和静态方法实现改变了接口的定义。 有关更多详细信息，请阅读[Java 8 Interface](https://www.journaldev.com/2752/java-8-interface-changes-static-method-default-method)。