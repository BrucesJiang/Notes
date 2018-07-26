# Java语法糖之自动装箱和自动拆箱

本文通过代码测试以及反编译来讲述Java自动装箱和自动拆箱的基本原理。

## 自动装箱和自动拆箱

如下代码：

```java
package test;

public class Test {
	public static void main(String[] args) {
		Integer i = 10;  //自动装箱
		int j = i;       //自动拆箱
		System.out.print(j);
	}
	
}
```

`javap -l -v Test.class`反编译后结果：

```java
Classfile /F:/Source/eclipse/java-base/target/test/Test.class
  Last modified 2018-7-26; size 656 bytes
  MD5 checksum 226f006f0350302bcfc40facef965b11
  Compiled from "Test.java"
public class test.Test
  SourceFile: "Test.java"
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Class              #2             //  test/Test
   #2 = Utf8               test/Test
   #3 = Class              #4             //  java/lang/Object
   #4 = Utf8               java/lang/Object
   #5 = Utf8               <init>
   #6 = Utf8               ()V
   #7 = Utf8               Code
   #8 = Methodref          #3.#9          //  java/lang/Object."<init>":()V
   #9 = NameAndType        #5:#6          //  "<init>":()V
  #10 = Utf8               LineNumberTable
  #11 = Utf8               LocalVariableTable
  #12 = Utf8               this
  #13 = Utf8               Ltest/Test;
  #14 = Utf8               main
  #15 = Utf8               ([Ljava/lang/String;)V
  #16 = Methodref          #17.#19        //  java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
  #17 = Class              #18            //  java/lang/Integer
  #18 = Utf8               java/lang/Integer
  #19 = NameAndType        #20:#21        //  valueOf:(I)Ljava/lang/Integer;
  #20 = Utf8               valueOf
  #21 = Utf8               (I)Ljava/lang/Integer;
  #22 = Methodref          #17.#23        //  java/lang/Integer.intValue:()I
  #23 = NameAndType        #24:#25        //  intValue:()I
  #24 = Utf8               intValue
  #25 = Utf8               ()I
  #26 = Fieldref           #27.#29        //  java/lang/System.out:Ljava/io/PrintStream;
  #27 = Class              #28            //  java/lang/System
  #28 = Utf8               java/lang/System
  #29 = NameAndType        #30:#31        //  out:Ljava/io/PrintStream;
  #30 = Utf8               out
  #31 = Utf8               Ljava/io/PrintStream;
  #32 = Methodref          #33.#35        //  java/io/PrintStream.print:(I)V
  #33 = Class              #34            //  java/io/PrintStream
  #34 = Utf8               java/io/PrintStream
  #35 = NameAndType        #36:#37        //  print:(I)V
  #36 = Utf8               print
  #37 = Utf8               (I)V
  #38 = Utf8               args
  #39 = Utf8               [Ljava/lang/String;
  #40 = Utf8               i
  #41 = Utf8               Ljava/lang/Integer;
  #42 = Utf8               j
  #43 = Utf8               I
  #44 = Utf8               SourceFile
  #45 = Utf8               Test.java
{
  public test.Test();
    descriptor: ()V
    flags: ACC_PUBLIC
    LineNumberTable:
      line 3: 0
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0       5     0  this   Ltest/Test;
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #8                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Ltest/Test;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    LineNumberTable:
      line 5: 0
      line 6: 6
      line 7: 11
      line 8: 18
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0      19     0  args   [Ljava/lang/String;
          6      13     1     i   Ljava/lang/Integer;
         11       8     2     j   I
    Code:
      stack=2, locals=3, args_size=1
         0: bipush        10
         2: invokestatic  #16                 // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
         5: astore_1
         6: aload_1
         7: invokevirtual #22                 // Method java/lang/Integer.intValue:()I
        10: istore_2
        11: getstatic     #26                 // Field java/lang/System.out:Ljava/io/PrintStream;
        14: iload_2
        15: invokevirtual #32                 // Method java/io/PrintStream.print:(I)V
        18: return
      LineNumberTable:
        line 5: 0
        line 6: 6
        line 7: 11
        line 8: 18
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      19     0  args   [Ljava/lang/String;
            6      13     1     i   Ljava/lang/Integer;
           11       8     2     j   I
}

```

从上述反编译代码中，我们可以发现，代码在执行自动装箱时底层自动调用了`Integer.valueOf()`方法，在执行自动拆箱时底层自动调用了`Integer.intValue()`方法。
这也就是自动拆箱和自动装箱的基本原理。

## 注意事项

在使用自动装箱和自动拆箱时需要十分小心 **空指针异常（java.lang.NullPointerException）**。 

另外，还需要注意 **缓存现象**，例如下列代码；

```java
public class TestMain {
	public static void main(String[] args) {
		Integer i1 = 100;
		Integer i2 = 100;
		Integer i3 = 200;
		Integer i4 = 200;

		System.out.println(i1 == i2);
		System.out.println(i3 == i4);

		Double d1 = 100.0;
		Double d2 = 100.0;
		Double d3 = 200.0;
		Double d4 = 200.0;

		System.out.println(d1 == d2);
		System.out.println(d3 == d4);
	}
}
```

运行结果为：

```java
true
false
false
false
```

产生这样结果的原因是: `Byte, Short, Integer, Long, Character`这几个包装类的`valueOf()`方式是以128为分界线做了缓存，如果是`i >= IntegerCache.low && i <= IntegerCache.high`,则取缓存中的值， 否则就是重新创建Integer对象。而`Float,Double`则不会，直接创建相应的对象。

