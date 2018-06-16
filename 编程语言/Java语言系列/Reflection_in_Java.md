# Reflection in Java
In the article, I will introduce the Reflection in Java through an example. 

Reflection is an API which is used to examine or modify the behavior of methods, classes, interfaces at runtime.

- The required classes for reflection are provided under `java.lang.reflect` package.
- Reflection gives us information about the class to which an object belongs and also the methods of that class which can be executed by using the object.
- Through reflection we can invoke methods at runtime irrespective of the access specifier used with them.

Reflection can be used to get Information about - 

1. **Class** The `getClass()` method is used to get the name of the class to which an object belongs.
2. **Constructor**  The `getConstructors()` method is used to get the public constructors of the class to which an object belongs.
3. **Method** The `getMethods()` method is used to get the public methods of the class to which an object belongs.

An example:

```java
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


// A simple Java Program to demonstrate the use of reflection

//class whose object is to be created 
class Test {
	//create a private field
	private String s;
	
	//create a public constructor
	public Test(){ s = "Test"; }
	
	//create a public method with no arguments
	public void method1() {
		System.out.println("The String is " + s);
	}
	
	//create a public method with an argument with int type
	public void method2(int n) {
		System.out.println("The number is " + n);
	}
	
	//create a private method with no arguments
	private void method3() {
		System.out.println("Private Method invoked");
	}
}



public class Demo {
	public static void main(String[] args) throws Exception{
		Test obj = new Test();
		
		//Creating class object from the object using
		//getclass method
		Class<?> clazz = obj.getClass();
		System.out.println("The name of class is " + clazz.getName());
		
		//Getting the constructor of the class through the object of the class
		Constructor<?> constructor = clazz.getConstructor();
		System.out.println("The name of constructor is " + constructor.getName());
		
		System.out.println("The public method are :");
		
		//Getting methods of the class through the object of the class by using getMethods
		Method[] methods = clazz.getMethods();
		
		//print method's name
		for(Method m : methods) {
			System.out.println(m.getName());
		}
		
		//creates object of desired method by providing the method name 
		//and parameter class as arguments to the getDeclaredMethod
		Method methodcall = clazz.getDeclaredMethod("method2", int.class);
		methodcall.invoke(obj, 19); 
		
		//creates object of the desired field by providing
		//the name of field as argument to the getDeclaredField method
		Field field = clazz.getDeclaredField("s");
		
		//allows the object to access the field irrespective 
		//of the access specifier used with the field
		field.setAccessible(true);
		
		//takes object and the new value to be assigned 
		// to the field as arguments
		field.set(obj, "JAVA");
		
		//creates object of  desired method providing the method name 
		//and parameter class as arguments to the getDeclaredMethod
		Method methodcall2 = clazz.getDeclaredMethod("method1");
		
		//invokes the method at runtime
		methodcall2.invoke(obj);
		
		//create object of the desired method by providing 
		//the name of method as argument to the getDeclaredMethod
		Method methodcall3 = clazz.getDeclaredMethod("method3");
		
		//allows the object to access the method irrespective
		//of the access specifier used with the method
		methodcall3.setAccessible(true);
		
		//invokes the method at runtime
		methodcall3.invoke(obj);
	}
}

```
It's output:
```java
The name of class is reflection.Test
The name of constructor is reflection.Test
The public method are :
method2
method1
wait
wait
wait
equals
toString
hashCode
getClass
notify
notifyAll
The number is 19
The String is JAVA
Private Method invoked
```

### Important Observations:
1. We can invoke an method through reflection if we know its name and paramter types. We use below two methods for this purpose **getDeclaredMethod()**: To create an object of method to be invoked. The syntax for this method is:

```java
Class.getDeclaredMethod(name, parameterType)
name - the name of the method whose object is to be created 
parameterType - parameter is an arrray of Class objects 
```

**invoke()**: To invoke a method of the class at runtime, we use following method-

```java
Method.invoke(Object, parameter)
Object - the Object which Method belongs to
If the method of the class does not accept any parameter then null is passed as argument
```

2. Through reflection we can **access the private variables and methods of a class** with the help of its class object and invoke the method by using the object as discussed above. We use below two methods for this purpose.

**Class.getDeclaredField(FieldName)** : Used to get the private field. Returns an object of type Field for specified field name.
**Field.setAccessible(true)** : Allows to access the field irrespective of the access modifier used with the field.

### Advantages of Using Reflection:
- **Extensibility Features**: An application may make use of external, user-defined classes by creating instances of extensibility objects using their fully-qualified names.
- **Debugging and testing tools** : Debuggers use the property of reflection to examine private members on classes.

## Drawbacks:

- **Performance Overhead**: Reflective operations have slower performance than their non-reflective counterparts, and should be avoided in sections of code which are called frequently in performance-sensitive applications.
- **Exposure of Internals**: Reflective code breaks abstractions and therefore may change behavior with upgrades of the platform


## References

- [深入解析Java反射（1） - 基础](https://www.sczyh30.com/posts/Java/java-reflection-1/#%E4%B8%80%E3%80%81%E5%9B%9E%E9%A1%BE%EF%BC%9A%E4%BB%80%E4%B9%88%E6%98%AF%E5%8F%8D%E5%B0%84%EF%BC%9F)
- [Reflection in Java](https://www.geeksforgeeks.org/reflection-in-java/)
- [The Reflection API](https://docs.oracle.com/javase/tutorial/reflect/index.html)
- [Java Reflection Tutorial](http://tutorials.jenkov.com/java-reflection/index.html)