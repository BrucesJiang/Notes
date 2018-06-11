# Java NIO之路径(Path)
Java Path接口是Java NIO 2更新的一部分，Java NIO在Java 6和Java 7中接收Java Path，并且该接口已添加到Java 7中的Java NIO。`Path`接口位于`java.nio.file`包中，所以Java Path接口的完全限定名称是`java.nio.file.Path`。

Java Path实例表示文件系统中的路径。路径可以指向文件或目录。路径可以是绝对的或相对的。绝对路径包含从文件系统根目录到它指向的文件或目录的完整路径。相对路径包含相对于其他路径的文件或目录的路径。相对路径可能听起来有点混乱。别担心。我将在稍后的Java NIO Path教程中更详细地解释相关路径。

在某些操作系统中，不要将文件系统路径与路径环境变量混淆。 `java.nio.file.Path`接口与路径环境变量无关。

在许多方面，`java.nio.file.Path`接口与`java.io.File`类相似，但有一些细微差别。但是，在很多情况下，您可以使用Path接口替换File类的使用。

## 创建一个Path实例
为了使用`java.nio.file.Path`实例，我们必须创建一个`Path`实例。 可以通过`Path类(java.nio.file.Paths)`的静态方法`Paths.get()`。下面是`Paths.get()`的代码示例：

```java
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathExample {
	public static void main(String[] args){
		Path path = Paths.get("/data/log.txt");
	}
}
```

### 创建绝对路径(Absolute Path)
通过以绝对文件作为参数调用`Paths.get()`工厂方法来创建绝对路径。 以下是创建表示绝对路径的`Path`实例的示例：

```java
Path path = Paths.get("/data/log.txt");
```

绝对路径就是`/data/log.txt`。

### 创建相对路径(Relative Path)
相对路径是指从一个路径（基本路径）指向目录或文件的路径。 相对路径的完整路径（绝对路径）是通过将基本路径与相对路径相结合而得到的。

Java NIO `Path类`也可以用于处理相对路径。 可以使用`Paths.get(basePath，relativePath)`方法创建相对路径。 以下是Java中的两个相对路径示例：

```java
Path projects = Paths.get("/data", "projects");

Path file     = Paths.get("/data", "projects/a-project/myfile.txt");
```
第一个示例创建一个指向路径（目录）`/data/projects`的Java Path实例。 第二个示例创建一个指向路径（文件）`/data/projects/a-project/myfile.txt`的Path实例。

使用相对路径时，可以在路径字符串中使用两个特殊代码。 这些代码是：

- .
- ..

这个 `.` 表示“当前目录”。 例如，如果你创建一个像这样的相对路径：

```java
Path currentDir = Paths.get(".");
System.out.println(currentDir.toAbsolutePath());
```
那么Java Path实例对应的绝对路径就是执行上述代码的应用程序所在的目录。

如果 `.` 在路径字符串的中间使用，它只是意味着与路径指向的那个目录相同的目录。 以下是一个路径示例，说明：

```java
Path currentDir = Paths.get("/data/projects/./a-project");
```

`..` 的意思是“父目录”或“一个目录”。 以下是一个Path Java示例，说明：

```java
Path parentDir = Paths.get("..");
```
此示例创建的Path实例将对应于启动运行此代码的应用程序的目录的父目录。

如果在路径字符串中间使用 `..`，它将对应于在路径字符串中的那一点上改变一个目录。 例如：

```java
String path = "/data/projects/a-project/../another-project";
Path parentDir2 = Paths.get(path);
```
此示例创建的Java Path实例将与此绝对路径相对应：

```java
/data/projects/another-project
```

`a-project`目录后面的 `..` 将目录更改为父目录项目，然后将路径从那里引用到另一个项目目录中。


这个 `.` 和 `..` 也可以与两个字符串`Paths.get()`方法结合使用。 以下是两个Java `Paths.get()` 示例，其中显示了简单的示例：

```java
Path path1 = Paths.get("/data/projects", "./a-project");

Path path2 = Paths.get("/data/projects/a-project", "../another-project");
```

Java NIO Path类可用于处理相对路径的更多方法。 本教程后面将详细介绍。
## Path.normalize()
`Path接口`的`normalize()`方法可以规范路径。 规范化意味着它将删除所有 `.` 和 `..` 位于路径字符串的中间，并解析路径字符串引用的路径。 这是一个Java `Path.normalize()` 示例：

```java
String originalPath =  "/data/projects/a-project/../another-project";

Path path1 = Paths.get(originalPath);
System.out.println("path1 = " + path1);

Path path2 = path1.normalize();
System.out.println("path2 = " + path2);
```
此路径示例首先在中间创建一个带有 `..` 的路径字符串。 然后，该示例从该路径字符串中创建一个`Path`实例，并将该`Path`实例输出（实际上是打印`Path.toString()`）。

该示例然后在创建的`Path`实例上调用`normalize()`，该实例返回一个新的`Path`实例。 这个新的规范化的`Path`实例也被打印出来。

这是从上面的例子打印的输出：

```java
path1 = /data/projects/a-project/../another-project
path2 = /data/projects/another-project
```


正如您所看到的，规范化路径不包含 `a-project/..` 部分，因为这是多余的。 删除的部分没有添加任何内容到最终的绝对路径。