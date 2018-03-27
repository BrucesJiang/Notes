# Java的抽象类(Abstract class in Java)
Java中的抽象类（Abstract Class）除了能够包括默认实现方法以外，其余都和接口（Interface）类似。抽象类可以有没有主体的抽象方法，也可以有实现的方法。

`Abstract`关键字用于创建抽象类和方法。Java中的抽象类不能实例化（be instantiated）。抽象类主要用于为子类提供扩展和实现抽象方法的基础，并覆盖或使用抽象类中实现的方法。

例如：

```java
//abstract class
public abstract class Person {
    
    private String name;
    private String gender;
    
    public Person(String nm, String gen){
        this.name=nm;
        this.gender=gen;
    }
    
    //abstract method
    public abstract void work();
    
    @Override
    public String toString(){
        return "Name="+this.name+"::Gender="+this.gender;
    }

    public void changeName(String newName) {
        this.name = newName;
    }   
}
```
`work()`方法只有方法签名没有方法体。下面是一个扩展了抽象类`Person`的实现类

```java
public class Employee extends Person {
    
    private int empId;
    
    public Employee(String nm, String gen, int id) {
        super(nm, gen);
        this.empId=id;
    }

    @Override
    public void work() {
        if(empId == 0){
            System.out.println("Not working");
        }else{
            System.out.println("Working as employee!!");
        }
    }
    
    public static void main(String args[]){
        //coding in terms of abstract classes
        Person student = new Employee("Dove","Female",0);
        Person employee = new Employee("Pankaj","Male",123);
        student.work();
        employee.work();
        //using method implemented in abstract class - inheritance
        employee.changeName("Pankaj Kumar");
        System.out.println(employee.toString());
    }

}
```
上述代码中，子类`Employee`从父类`Person`中继承了它的属性和方法。

另请注意，在Employee类中使用Override注释。

**抽象类的重点**:
- `abstract`关键字用于创建一个抽象类，抽象类中既可以包含没有方法体的抽象方法，也可以包含实现方法
- 抽象类不能被实例化
- `abstract`关键字可以用于创建一个没有方法体的抽象方法
- 如果一个类包含了抽象方法（被`abstract`关键字修饰），那么这个类只能抽象类，否则该类无法通过编译
- 抽象类不一定含有抽象方法
- 如果一个抽象类中没有任何实现方法，那么最好使用接口。因为Java中不允许多重继承，但是允许实现多个接口。
- 抽象类的子类必须实现其父类所有的抽象方法，除该子类也是抽象类
- 接口中所有的方法都是抽象方法，除非这些接口方法是`static`或`default`。接口中被`static`修饰或 `default`方法在**Java 8**中被加入，更多的细节请参考[Java 8 interface changes -- static method and default medthod](https://www.journaldev.com/2752/java-8-interface-changes-static-method-default-method)
- 抽象类可以在不提供方法实现的情况下，实现接口。也就是说，抽象方法可以实现接口，并且实现时不必实现接口的抽象方法
- Java抽象类用于为所有子类提供常用方法实现或提供默认实现。（Java Abstract class is used to provide common method implementation to all the subclasses or to provide default implementation.）
- 
如果有`main()`方法，我们可以象其他任何类一样在java中运行抽象类。

![Abstract class in Java](https://www.journaldev.com/1582/abstract-class-in-java?utm_source=website&utm_medium=sidebar&utm_campaign=Core-Java-Sidebar-Widget)