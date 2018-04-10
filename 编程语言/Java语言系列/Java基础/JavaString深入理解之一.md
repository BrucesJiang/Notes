# Java String深入理解之一
字符串对象或者其等价对象 (如`char`数组)，在内存中总是占据最大的空间块，因此如何高效地处理字符串，是提高系统整体性能的关键。

`String`对象可以认为是`char`数组的延伸和进一步封装，它主要由3部分组成：`char数组`、`偏移量`和`String的长度`。`char数组`表示`String的内容`，它是`String` 对象所表示字符串的超集。`String`的真实内容还需要由偏移量和长度在这个`char 数组`中进行定位和截取。

`String`有3个基本特点：
1. 不变性；
2. 针对常量池的优化；
3. 类的`final`定义。

不变性指的是`String`对象一旦生成，则不能再对它进行改变。`String` 的这个特性可以泛化成不变 `(immutable)`模式，即一个对象的状态在对象被创建之后就不再发生变化。不变模式的主要作用在于当一个对象需要被多线程共享，并且访问频繁时，可以省略同步和锁等待的时间，从而大幅提高系统性能。

针对常量池的优化指的是当两个`String`对象拥有相同的值时，它们只引用常量池中的同一个拷贝，当同一个字符串反复出现时，这个技术可以大幅度节省内存空间。

下面代码 `str1、str2、str4`引用了相同的地址，但是`str3` 却重新开辟了一块内存空间，虽然`str3`单独占用了堆空间，但是它所指向的实体和`str1`完全一样。 如代码所示：

```java
public class TestString {
    public static void main(String[] args){
        String str1 = "abc";
        String str2 = "abc";
        String str3 = new String("abc");
        String str4 = str1;
        System.out.println("is str1 = str2?"+(str1==str2));
        System.out.println("is str1 = str3?"+(str1==str3));
        System.out.println("is str1 refer to str3?"+(str1.intern()==str3.intern()));
        System.out.println("is str1 = str4"+(str1==str4));
        System.out.println("is str2 = str4"+(str2==str4));
        System.out.println("is str4 refer to str3?"+(str4.intern()==str3.intern()));
    }
}


```

```
//结果
is str1 = str2?true
is str1 = str3?false
is str1 refer to str3?true
is str1 = str4true
is str2 = str4true
is str4 refer to str3?true
```

## SubString 使用技巧
`String`的`substring`方法源码在最后一行新建了一个`String`对象，`new String(offset+beginIndex,endIndex-beginIndex,value)`；该行代码的目的是为了能高效且快速地共享`String`内的`char 数组对象`。但在这种通过偏移量来截取字符串的方法中，`String`的原生内容`value数组`被复制到新的子字符串中。设想，如果原始字符串很大，截取的字符长度却很短，那么截取的子字符串中包含了原生字符串的所有内容，并占据了相应的内存空间，而仅仅通过偏移量和长度来决定自己的实际取值。这种算法提高了速度却浪费了空间。

下面代码演示了使用`substring`方法在一个很大的`String` 独享里面截取一段很小的字符串，如果采用`String`的`substring` 方法会造成内存溢出，如果采用反复创建新的`String`方法可以确保正常运行。

```java
import java.util.ArrayList;
import java.util.List;
 
public class TestString{
    public static void main(String[] args){
        List<String> handler = new ArrayList<String>();
        for(int i=0;i<1000;i++){
            HugeStr h = new HugeStr();
            ImprovedHugeStr h1 = new ImprovedHugeStr();
            handler.add(h.getSubString(1, 5));
            handler.add(h1.getSubString(1, 5));
        }
    }
  
    static class HugeStr{
        private String str = new String(new char[800000]);
        public String getSubString(int begin,int end){
            return str.substring(begin, end);
        }
    }
  
    static class ImprovedHugeStr{
        private String str = new String(new char[10000000]);
        public String getSubString(int begin,int end){
            return new String(str.substring(begin, end));
        }
    }
}

```

输出结果：
```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
at java.util.Arrays.copyOf(Unknown Source)
at java.lang.StringValue.from(Unknown Source)
at java.lang.String.<init>(Unknown Source)
at StringDemo$ImprovedHugeStr.<init>(StringDemo.java:23)
at StringDemo.main(StringDemo.java:9)
```

`ImprovedHugeStr`可以工作是因为它使用没有内存泄漏的`String构造函数`重新生成了`String`对象，使得由`substring()`方法返回的、存在内存泄漏问题的`String`对象失去所有的强引用，从而被垃圾回收器识别为垃圾对象进行回收，保证了系统内存的稳定。

## 切割字符串

`String`的`split`方法支持传入正则表达式帮助处理字符串，但是简单的字符串分割时性能较差。

对比`split`方法和`StringTokenizer`类的处理字符串性能，代码如下所示。
```java
class SplitAndStringtokenizer {
    public static void main(String[] args) {
        String orgStr = null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 100000; i++) {
            sb.append(i);
            sb.append(",");
        }
        orgStr = sb.toString();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            orgStr.split(",");
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);

        start = System.currentTimeMillis();
        String orgStr1 = sb.toString();
        StringTokenizer st = new StringTokenizer(orgStr1, ",");
        for (int i = 0; i < 100000; i++) {
            st.nextToken();
        }
        st = new StringTokenizer(orgStr1, ",");
        end = System.currentTimeMillis();
        System.out.println(end - start);

        start = System.currentTimeMillis();
        String orgStr2 = sb.toString();
        String temp = orgStr2;
        while (true) {
            int j = temp.indexOf(",");
            if (j < 0)
                break;
            splitStr = temp.substring(0, j);
            temp = temp.substring(j + 1);
        }
        temp = orgStr2;
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}

```
输出结果：
```
39015
16
15
```

### **切分字符串方式讨论**

String 的 split 方法支持传入正则表达式帮助处理字符串，操作较为简单，但是缺点是它所依赖的算法在对简单的字符串分割时性能较差。清单 5 所示代码对比了 String 的 split 方法和调用 StringTokenizer 类来处理字符串时性能的差距。

split 借助于数据对象及字符查找算法完成了数据分割，适用于数据量较少场景。

## 合并字符串
由于`String`是不可变对象，因此，在需要对字符串进行修改操作时 (如字符串连接、替换)，`String`对象会生成新的对象，所以其性能相对较差。但是`JVM`会对代码进行彻底的优化，将多个连接操作的字符串在编译时合成一个单独的长字符串。针对超大的`String`对象，我们采用`String`对象连接、使用`concat`方法连接、使用`StringBuilder`类等多种方式，代码如下所示：

```java
public class StringConcat {
    public static void main(String[] args) {
        String str = null;
        String result = "";

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            str = str + i;
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);

        start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            result = result.concat(String.valueOf(i));
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);

        start = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append(i);
        }
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}

```
输出结果：
```
375
187
0
```

虽然第一种方法编译器判断`String`的加法运行成`StringBuilder` 实现，但是编译器没有做出足够聪明的判断，每次循环都生成了新的`StringBuilder` 实例从而大大降低了系统性能。

`StringBuffer`和`StringBuilder`都实现了`AbstractStringBuilder` 抽象类，拥有几乎相同的对外借口，两者的最大不同在于`StringBuffer`对几乎所有的方法都做了同步，而`StringBuilder`并没有任何同步。由于方法同步需要消耗一定的系统资源，因此，`StringBuilder`的效率也好于`StringBuffer`。但是，在多线程系统中，`StringBuilder` 无法保证线程安全，不能使用。代码如下所示：

```java
public class StringBufferandBuilder {
    public StringBuffer contents = new StringBuffer();
    public StringBuilder sbu = new StringBuilder();

    public void log(String message) {
        for (int i = 0; i < 10; i++) {
            /*
             * contents.append(i); contents.append(message);
             * contents.append("\n");
             */
            contents.append(i);
            contents.append("\n");
            sbu.append(i);
            sbu.append("\n");
        }
    }

    public void getcontents() {
        // System.out.println(contents);
        System.out.println("start print StringBuffer");
        System.out.println(contents);
        System.out.println("end print StringBuffer");
    }

    public void getcontents1() {
        // System.out.println(contents);
        System.out.println("start print StringBuilder");
        System.out.println(sbu);
        System.out.println("end print StringBuilder");
    }

    public static void main(String[] args) throws InterruptedException {
        StringBufferandBuilder ss = new StringBufferandBuilder();
        runthread t1 = new runthread(ss, "love");
        runthread t2 = new runthread(ss, "apple");
        runthread t3 = new runthread(ss, "egg");
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }

}

class runthread extends Thread {
    String message;
    StringBufferandBuilder buffer;

    public runthread(StringBufferandBuilder buffer, String message) {
        this.buffer = buffer;
        this.message = message;
    }

    public void run() {
        while (true) {
            buffer.log(message);
            // buffer.getcontents();
            buffer.getcontents1();
            try {
                sleep(5000000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}

```
输出结果:

```
start print StringBuffer
0123456789
end print StringBuffer
start print StringBuffer
start print StringBuilder
01234567890123456789
end print StringBuffer
start print StringBuilder
01234567890123456789
01234567890123456789
end print StringBuilder
end print StringBuilder
start print StringBuffer
012345678901234567890123456789
end print StringBuffer
start print StringBuilder
012345678901234567890123456789
end print StringBuilder
```
`StringBuilder`数据并没有按照预想的方式进行操作。`StringBuilder`和`StringBuffer` 的扩充策略是将原有的容量大小翻倍，以新的容量申请内存空间，建立新的`char数组`，然后将原数组中的内容复制到这个新的数组中。因此，对于大对象的扩容会涉及大量的内存复制操作。如果能够预先评估大小，会提高性能。