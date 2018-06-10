# Java NIO之管道(Pipe)
Java NIO通道(`Pipe`)是两个线程之间的单向数据连接(one-way data connection)。一个管道`Pipe`拥有一个源通道(`source channel`)和一个`sink`通道(`sink channel`)。 数据会被写到`sink通道`，然后从`source通道`读取。 

下面是`Pipe`原理的图示：

![pipe-internals](./images/pipe-internals.png)

## 创建一个管道
通过调用`Pipe.open()`方法打开一个管道`Pipe`，就像下面这样：

```java
Pipe.SinkChannel sinkChannel = pipe.sink();
```

## 向管道中数据
通过调用`wirte()`方法向`sinkChannel`通道中写入数据，就像下面这样：

```java
String newData = "New String to write to file..." + System.currentTimeMillis();

ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());

buf.flip();

while(buf.hasRemaining()) {
	sinkChannel.write(buf);
}
```

## 从管道中读数据
从读取管道的数据，需要访问source通道，像这样：

```java
Pipe.SourceChannel sourceChannel = pipe.source();
```

可以通过调用`read()`方法，从`source channel`中去读数据，就像这样：

```java
ByteBuffer buf = ByteBuffer.allocate(48);
int bytesRead = inChannel.read(buf);
```

`read()`方法返回的`int`类型结果表明有多少byte数据读入`Channel`。