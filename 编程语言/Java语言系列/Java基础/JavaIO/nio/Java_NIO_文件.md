# Java NIO之文件(File)
Java NIO `Files`类（`java.nio.file.Files`）提供了几种用于处理文件系统中的文件的方法。 这个Java NIO文件教程将涵盖这些最常用的方法。`Files类`包含很多方法，所以如果你需要一个在这里没有描述的方法，那么检查JavaDoc。 `java.nio.file.Files`类与`java.nio.file.Path`实例一起使用，因此在使用Files类之前，我们需要了解Path类。

## Files.exists()
`Files.exists()`方法检查给定的路径(`Path`)在文件系统中是否存在。

可能创建文件系统中不存在的`Path`实例。 例如，如果要创建一个新目录，则应先创建相应的`Path`实例，然后创建该目录。

既然`Path`实例代表的路径可能不存在于文件系统中，那么就可以用`Files.exists()`方法检查路径是否真的存在。

下面是`Files.exists()`的示例：

```java
Path path = Path.get("data/logging.properties");

boolean pathExists = Files.exists(path, new LinkOption[]{LinkOption.NOFOLLOW_LINKS});
```

这个例子首先创建一个`Path`实例，指向我们想要检查是否存在的路径。 其次，该示例使用`Path`实例调用 `Files.exists()` 方法作为第一个参数。

注意 `Files.exists()` 方法的第二个参数。 该参数是影响 `Files.exists()` 如何确定路径是否存在的选项数组。 在上面的例子中，数组包含 `LinkOption.NOFOLLOW_LINKS` ，这意味着 `Files.exists()` 方法不应该跟随文件系统中的符号链接来确定路径是否存在。

## Files.createDirectory()
`Files.createDirectory()` 方法根据 `Path`实例创建一个新目录。 这是一个Java `Files.createDirectory()` 示例：

```java
Path path = Paths.get("data/subdir");

try {
    Path newDir = Files.createDirectory(path);
} catch(FileAlreadyExistsException e){
    // the directory already exists.
} catch (IOException e) {
    //something else went wrong
    e.printStackTrace();
}
```

第一行创建表示要创建的目录的`Path`实例。 在`try-catch`块内部，使用路径作为参数调用`Files.createDirectory()`方法。 如果创建目录成功，则会返回指向新创建路径的`Path`实例。

如果该目录已经存在，则会抛出`java.nio.file.FileAlreadyExistsException`异常。 如果出现其他问题，可能会抛出`IOException`异常。 例如，如果所需新目录的父目录不存在，则可能会抛出`IOException`。 父目录是要在其中创建新目录的目录。 因此，它意味着新目录的父目录。

## Files.copy()
`Files.copy()` 方法将文件从一个路径复制到另一个路径。 这里是一个Java NIO `Files.copy()` 例子：

```java
Path sourcePath      = Paths.get("data/logging.properties");
Path destinationPath = Paths.get("data/logging-copy.properties");

try {
    Files.copy(sourcePath, destinationPath);
} catch(FileAlreadyExistsException e) {
    //destination file already exists
} catch (IOException e) {
    //something else went wrong
    e.printStackTrace();
}
```
首先，该示例创建一个源和目标路径实例。 然后该示例调用`Files.copy()`，将两个`Path`实例作为参数传递。 这将导致源路径引用的文件被复制到目标路径引用的文件中。

如果目标文件已经存在，则抛出`java.nio.file.FileAlreadyExistsException`。 如果出现其他问题，则抛出`IOException`。 例如，如果要复制文件的目录不存在，则会抛出`IOException`。

## 覆盖现有文件(Overwriting Existing Files)
可以强制 `Files.copy()` 覆盖现有的文件。 这里有一个例子展示如何使用 `Files.copy()` 覆盖现有的文件：

```java
Path sourcePath      = Paths.get("data/logging.properties");
Path destinationPath = Paths.get("data/logging-copy.properties");

try {
    Files.copy(sourcePath, destinationPath,
            StandardCopyOption.REPLACE_EXISTING);
} catch(FileAlreadyExistsException e) {
    //destination file already exists
} catch (IOException e) {
    //something else went wrong
    e.printStackTrace();
}
```

注意 `Files.copy()` 方法的第三个参数。 如果目标文件已存在，此参数指示 `copy()` 方法覆盖现有文件。

## Files.move()
Java NIO `Files类` 还包含一个将文件从一个路径移动到另一个路径的功能。 移动文件与重命名文件相同，除了移动文件可以将文件移动到其他目录并在同一操作中更改其名称。 是的，`java.io.File`类也可以通过它的 `renameTo()` 方法来实现，但是现在在`java.nio.file.Files`类中也有文件移动功能。

下面是 `Files.move()` 的代码示例：

```java
Path sourcePath      = Paths.get("data/logging-copy.properties");
Path destinationPath = Paths.get("data/subdir/logging-moved.properties");

try {
    Files.move(sourcePath, destinationPath,
            StandardCopyOption.REPLACE_EXISTING);
} catch (IOException e) {
    //moving file failed.
    e.printStackTrace();
}
```

首先创建源路径和目标路径。 源路径指向要移动的文件，目标路径指向文件应移至的位置。 然后调用`Files.move()`方法。 这导致文件被移动。

注意传递给 `Files.move()` 的第三个参数。 该参数告诉 `Files.move()` 方法覆盖目标路径上的任何现有文件。 该参数实际上是可选的。

如果移动文件失败，`Files.move()` 方法可能会引发`IOException`。 例如，如果文件已经存在于目标路径中，并且已经省去了 `StandardCopyOption.REPLACE_EXISTING`选项，或者要移动的文件不存在等。

## Files.delete()
`Files.delete()` 方法可以删除文件或目录。 这是一个Java `Files.delete()` 示例：

```java
Path path = Paths.get("data/subdir/logging-moved.properties");

try {
    Files.delete(path);
} catch (IOException e) {
    //deleting file failed
    e.printStackTrace();
}
```
首先创建指向要删除的文件的路径。 其次调用 `Files.delete()` 方法。 如果 `Files.delete()` 由于某种原因（例如文件或目录不存在）无法删除文件，则抛出`IOException`。


## Files.walkFileTree()
`Files.walkFileTree()` 方法包含递归遍历目录树的功能。 `walkFileTree()` 方法将`Path`实例和`FileVisitor`作为参数。 `Path`实例指向您想要遍历的目录。 `FileVisitor`在转换过程中被调用。

在解释遍历如何工作之前，首先是`FileVisitor`接口：

```java
public interface FileVisitor {

    public FileVisitResult preVisitDirectory(
        Path dir, BasicFileAttributes attrs) throws IOException;

    public FileVisitResult visitFile(
        Path file, BasicFileAttributes attrs) throws IOException;

    public FileVisitResult visitFileFailed(
        Path file, IOException exc) throws IOException;

    public FileVisitResult postVisitDirectory(
        Path dir, IOException exc) throws IOException {

}
```

在使用之前必须自己实现`FileVisitor`接口，并将实现的实例传递给 `walkFileTree()` 方法。 `FileVisitor` 实现的每个方法将在目录遍历期间的不同时间被调用。 如果您不需要挂接所有这些方法，则可以扩展 `SimpleFileVisitor` 类，该类包含 `FileVisitor` 接口中所有方法的默认实现。

这里是一个 `walkFileTree()` 示例：

```java
Files.walkFileTree(path, new FileVisitor<Path>() {
  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    System.out.println("pre visit dir:" + dir);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    System.out.println("visit file: " + file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    System.out.println("visit file failed: " + file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    System.out.println("post visit directory: " + dir);
    return FileVisitResult.CONTINUE;
  }
});
```

`FileVisitor` 实现中的每个方法在遍历期间的不同时间被调用：

在访问任何目录之前调用`preVisitDirectory()` 方法。 `postVisitDirectory()` 方法在访问目录后立即调用。

`visitFile()` 方法在文件遍历期间访问的每个文件都被调用。 它不需要目录——只有文件。 如果访问文件失败，将调用 `visitFileFailed()` 方法。 例如，如果没有正确的权限，或者其他问题出错了。

这四个方法中的每一个都返回一个 `FileVisitResult` 枚举实例。 `FileVisitResult` 枚举包含以下四个选项：
- CONTINUE
- TERMINATE
- SKIP_SIBLINGS
- SKIP_SUBTREE

通过返回其中一个值，被调用的方法可以决定文件的走向应该如何继续。

`CONTINUE` 意味着文件散步应该照常继续。

`TERMINATE` 表示文件散步现在应该终止。

`SKIP_SIBLINGS` 表示应该继续文件传播，但不访问此文件或目录的任何兄弟。

`SKIP_SUBTREE` 表示文件行走应继续，但不访问此目录中的条目。 如果从 `preVisitDirectory()` 返回此值，该值只有一个函数。 如果从任何其他方法返回，它将被解释为`CONTINUE`。

## 检索文件(Searching For Files)
以下是一个 `walkFileTree()`，它扩展了 `SimpleFileVisitor` 以查找名为 `README.txt` 的文件：

```java
Path rootPath = Paths.get("data");
String fileToFind = File.separator + "README.txt";

try {
  Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      String fileString = file.toAbsolutePath().toString();
      //System.out.println("pathString = " + fileString);

      if(fileString.endsWith(fileToFind)){
        System.out.println("file found at path: " + file.toAbsolutePath());
        return FileVisitResult.TERMINATE;
      }
      return FileVisitResult.CONTINUE;
    }
  });
} catch(IOException e){
    e.printStackTrace();
}
```

## 递归删除目录(Deleting Directories Recursively)
`Files.walkFileTree()` 也可用于删除包含其中所有文件和子目录的目录。 如果 `Files.delete()` 方法为空，它将只删除一个目录。 通过遍历所有目录并删除每个目录中的所有文件（在 `visitFile()` 内），然后删除目录本身（在 `postVisitDirectory()` 内），可以删除包含所有子目录和文件的目录。 这是一个递归的目录删除示例：

```java
Path rootPath = Paths.get("data/to-delete");

try {
  Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      System.out.println("delete file: " + file.toString());
      Files.delete(file);
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      Files.delete(dir);
      System.out.println("delete dir: " + dir.toString());
      return FileVisitResult.CONTINUE;
    }
  });
} catch(IOException e){
  e.printStackTrace();
}
```

## Additional Methods in the Files Class
The `java.nio.file.Files` class contains many other useful functions, like functions for creating symbolic links, determining the file size, setting file permissions etc. Check out the JavaDoc for the `java.nio.file.Files` class for more information about these methods.