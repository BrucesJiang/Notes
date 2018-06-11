# Java NIO之异步文件通道(AsynchronousFileChannel)
在Java 7中，`AsynchronousFileChannel` 被添加到Java NIO。 `AsynchronousFileChannel` 可以异步读写文件。 本教程将解释如何使用 `AsynchronousFileChannel`。

## 1 创建一个异步文件通道(AsynchronousFileChannel)
可以通过其静态方法 `open()` 创建一个 `AsynchronousFileChannel` 。 这里是创建一个 `AsynchronousFileChannel` 的例子：

```java
Path path = Paths.get("data/test.xml");

AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
```
`open()` 方法的第一个参数是指向 `AsynchronousFileChannel`将与之关联的文件的 `Path`实例。 第二个参数是一个或多个打开的选项，它告诉 `AsynchronousFileChannel`将在底层文件上执行哪些操作。 在这个例子中，我们使用了 `StandardOpenOption.READ`，这意味着文件将被打开并可读。

## 2 读数据
您可以通过两种方式从 `AsynchronousFileChannel` 读取数据。 每种读取数据的方式都会调用 `AsynchronousFileChannel` 的 `read()` 方法之一。 这两种读取数据的方法将在以下章节中进行介绍。

### 2.1 通过Future读取数据
从 `AsynchronousFileChannel` 读取数据的第一种方法是调用返回 `Future`的 `read()` 方法。 以下是调用 `read()` 方法的方式：

```java
Future<Integer> operation = fileChannel.read(buffer, 0);
```

`read()` 方法的这个版本将 `ByteBuffer`作为第一个参数。 从 `AsynchronousFileChannel` 读取的数据被读入这个 `ByteBuffer`。 第二个参数是文件中开始读取的字节位置。
即使读取操作尚未完成，`read()` 方法也会立即返回。 您可以通过调用 `read()` 方法返回的 `Future` 实例的 `isDone()` 方法来检查读取操作何时完成。

这里是一个更长的例子，展示了如何使用这个版本的 `read()` 方法：

```java
AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

ByteBuffer buffer = ByteBuffer.allocate(1024);
long position = 0;

Future<Integer> operation = fileChannel.read(buffer, position);

while(!operation.isDone());

buffer.flip();
byte[] data = new byte[buffer.limit()];
buffer.get(data);
System.out.println(new String(data));
buffer.clear();
```

此示例创建一个 `AsynchronousFileChannel`，然后创建一个 `ByteBuffer` 作为参数传递给 `read()`方法以及`0`的位置。在调用 `read()` 之后，该示例将循环，直到返回的 `Future` 的 `isDone()` 方法返回 `true`。 当然，这不是CPU的高效使用——但不知何故，需要等待读取操作完成。一旦读取操作完成数据读入 `ByteBuffer`，然后到一个字符串并打印到 `System.out`。

### 2.2 通过CompletionHandler读数据
从 `AsynchronousFileChannel` 读取数据的第二种方法是调用以 `CompletionHandler` 作为参数的 `read()` 方法版本。 以下是你如何调用这个 `read()` 方法：

```java
fileChannel.read(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        System.out.println("result = " + result);

        attachment.flip();
        byte[] data = new byte[attachment.limit()];
        attachment.get(data);
        System.out.println(new String(data));
        attachment.clear();
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
});
```

一旦读取操作完成，将调用 `CompletionHandler的completed()` 的 `completed()` 方法。 `completed()`方法有两个参数，一个`Integer`类型的参数表示读入多少byte的数据，一个ByteBuffer类型被传递给`read()`函数的参数。这个`ByteBuffer`类型的参数是`read()`方法的第三个参数。在这种情况下，它也是读取数据的ByteBuffer。 我们可以自由选择要附加的对象。如果读取操作失败，则会调用 `CompletionHandler` 的 `failed()` 方法。
 
## 3 写数据
就像读数据一样，可以通过两种方式将数据写入 `AsynchronousFileChannel`。 写入数据的每种方法都调用 `AsynchronousFileChannel`的 `write()` 方法之一。 

这两写数据的方法将在下面的章节中介绍。

### 3.1 通过Future写数据
`AsynchronousFileChannel`还使我们够异步地写入数据。 下面是一个完整的Java `AsynchronousFileChannel` 写入示例：

```java
Path path = Paths.get("data/test-write.txt");
AsynchronousFileChannel fileChannel =  AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);

ByteBuffer buffer = ByteBuffer.allocate(1024);
long position = 0;

buffer.put("test data".getBytes());
buffer.flip();

Future<Integer> operation = fileChannel.write(buffer, position);
buffer.clear();

while(!operation.isDone());

System.out.println("Write done");

```

首先，`AsynchronousFileChannel`处于写模式。 然后 一个`ByteBuffer`被创建并且有一些数据被写入。 然后`ByteBuffer`中的数据被写入文件。 最后，示例代码检查返回的`Future`，以查看写操作是否完成。

注意，上述代码正常工作的前提是文件已经存在。如果文件不存在，`write()`方法将会抛出`java.nio.file.NoSuchFileException`例外。

可以使用下列代码保证`Path`指示的文件确实已经存在：

```java
if(!Files.exists(path)) {
	Files.cerateFile(path);
}
```

## 通过CompletionHandler写数据
还可以使用 `CompletionHandler` 将数据写入 `AsynchronousFileChannel`，以告诉我们何时完成写入以替代 `Future`。


下面是一个通过 `CompletionHandler` 向 `AsynchronousFileChannel` 写入数据的例子：

```java
Path path = Paths.get("data/test-write.txt");
if(!Files.exists(path)){
    Files.createFile(path);
}
AsynchronousFileChannel fileChannel = 
    AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);

ByteBuffer buffer = ByteBuffer.allocate(1024);
long position = 0;

buffer.put("test data".getBytes());
buffer.flip();

fileChannel.write(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        System.out.println("bytes written: " + result);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        System.out.println("Write failed");
        exc.printStackTrace();
    }
});
```

当写入操作完成时， `CompletionHandler` 的 `complete()` 方法将会被调用。 如果由于一些原因导致写入失败，`failed()`方法将会被调用。

注意，这里是如何将`ByteBuffer`当作`attachment`使用的——这个对象被传递给`CompletionHandler`的方法。