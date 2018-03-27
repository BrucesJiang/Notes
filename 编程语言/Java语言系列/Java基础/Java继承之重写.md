# Java继承之重写过程中的某些问题

下列一段代码：

```java
public class Test{
    //public static void main(String[] args){
    public static void main(String args[]) {
        Super s = new Subclass();
        s.foo();
    }

    public static void main(int[] a) { //通过重载overloading构造同名方法

    }
}

class Super{
    void foo() {
        System.out.println("Super");
    }

    private void f() {
        System.out.println("Super");
    }
}

class Subclass extends Super {
    //重写父类的方法不能被'static'修饰，除非重写父类同样被
    //`static`修饰的方法，但是这里foo方法可以被`final`修饰
    static void foo() { 
        System.out.println("Subclass");
    }

    void foo(int a ) { 
        System.out.println("Subclass");   
    }

    void f() {
        System.out.println("Subclass");
    }
}
```

- `Subclass foo()`方法用`static`修饰，这里将会出现`This static method cannot hide the instance method from Super`编译时错误。

- 会出现编译错误，由于`Super`类的`f()`方法是`private`的，这里的编译错误会是`the method f() from the Super is not visible` 
