# Java异常使用 -- Java Try with Resources
本文主要介绍Java 7的用于自动资源管理的增强功能`try-with-resuource`语句。

资源是一个程序执行完成后必须关闭的对象。例如，数据库连接的文件资源，JDBC资源或套接字资源。在Java 7之前，没有自动资源管理，我们应该在工作完成后明确的关闭资源。通常，它是在`try-catch`语句的`finally`块中完成的。当我们忘记关闭资源时，这种做法可能导致内存泄漏和性能问题。

让我们看看一个伪代码片段来理解这个Java尝试使用资源功能。

在Java 7之前
```java
try{
    //open resources like File, Database connection, Sockets etc
} catch (FileNotFoundException e) {
    // Exception handling like FileNotFoundException, IOException etc
}finally{
    // close resources
}
```

Java 7 try with resources的实现：

```java
try(// open resources here
    ){
    // use resources
} catch (FileNotFoundException e) {
    // exception handling
}
// resources are closed as soon as try-catch block is executed.
```
### Java 7以前资源管理示例

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Java6ResourceManagement {

    public static void main(String[] args) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("C:\\a.txt"));
            System.out.println(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

```

### Java 7 Try With Resources Example

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Java7ResourceManagement {

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new FileReader(
                "C:\\a.txt"))) {
            System.out.println(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Java 7 has introduced a new interface java.lang.AutoCloseable. To use any resource in try-with-resources, it must implement AutoCloseable interface else java compiler will throw compilation error.