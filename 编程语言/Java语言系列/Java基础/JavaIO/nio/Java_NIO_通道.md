# 通道-Channel
本文概述了Java NIO的工作原理，基本实现和它们的用法。

## 1 通道概述
Java NIO的通道类似流，但又有些不同： 

- 既可以从通道中读取数据，又可以写数据到通道。但流的读写通常是单向的（typically one-way）。
- 通道可以异步（asynchronously）读写。
- 通道中的数据总是要先读到一个Buffer，或者总是要从一个Buffer中写入。

正如上面所说，我们可以从通道读取数据到缓冲区，从缓冲区写入数据到通道。 如下图所示:

![overview-channels-buffers](./images/overview-channels-buffers.png)

## 2 通道(Channel)实现
下面是`Java NIO`最重要的实现类：
- FileChannel
- DatagramChannel
- SocketChannel
- ServerSocketChannel

`FileChannel`： 从文件中读写数据
`DatagramChannel`: 通过UDP读写网络中的数据
`SocketChannel`: 通过TCP读写网络中的数据
`ServerSocketChannel`: 像Web服务器那样，监听新进来的TCP连接。对么每一个新进来的连接都会创建一个`SocketChannel`

代码示例：

`FileChannel`读取数据到`Buffer`中：

```java
RandomAccessFile aFile = new RandomAccessFile("/data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();

ByteBuffer buf = ByteBuffer.allocate(48);

int bytesRead = inChannel.read(buf);
while(bytesRead != -1) {

	System.out.println("Read " + bytesRead);
	buf.flip();

	while(buf.hasRemaining()) {
		System.out.println((char)buf.get());
	}

	buf.clear();
	bytesRead = inChannel.read(buf);
}
aFile.close();
```
注意`buf.flip()`调用。 首先读取数据到`Buffer`，然后反转`Buffer`,接着再从`Buffer`中读取数据。

## 3 通道之间的数据传输
在Java NIO中，如果两个通道中有一个`FileChannel`，那你可以直接将数据从一个`channel`传输到另外一个`channel`。

### 3.1 transferFrom() 
`FileChannel`的`transferFrom()`方法可以将数据从源通道传输到`FileChannel`中（这个方法在JDK文档中的解释为将字节从给定的可读取字节通道传输到此通道的文件中）。下面是一个简单的例子：

```java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");  
FileChannel      fromChannel = fromFile.getChannel();  
  
RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");  
FileChannel      toChannel = toFile.getChannel();  
  
long position = 0;  
long count = fromChannel.size();  
  
toChannel.transferFrom(position, count, fromChannel); 
```

方法的输入参数position表示从position处开始向目标文件写入数据，count表示最多传输的字节数。如果源通道的剩余空间小于 count 个字节，则所传输的字节数要小于请求的字节数。 

此外要注意，在SoketChannel的实现中，SocketChannel只会传输此刻准备好的数据（可能不足count字节）。因此，SocketChannel可能不会将请求的所有数据(count个字节)全部传输到FileChannel中。 

### 3.2 transferTo() 
transferTo()方法将数据从FileChannel传输到其他的channel中。下面是一个简单的例子：

```java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");  
FileChannel      fromChannel = fromFile.getChannel();  
  
RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");  
FileChannel      toChannel = toFile.getChannel();  
  
long position = 0;  
long count = fromChannel.size();  
  
fromChannel.transferTo(position, count, toChannel);  
```
是不是发现这个例子和前面那个例子特别相似？除了调用方法的FileChannel对象不一样外，其他的都一样。 

上面所说的关于SocketChannel的问题在transferTo()方法中同样存在。SocketChannel会一直传输数据直到目标buffer被填满。

## 4 文件通道-FileChannel
`Java NIO`中的`FileChannel`是一个连接到文件的通道。可以通过文件通道读写文件。 

`FileChannel`无法设置为非阻塞模式，它总是运行在阻塞模式下。 

### 4.1 打开FileChannel 
在使用`FileChannel`之前，必须先打开它。但是，我们无法直接打开一个`FileChannel`，需要通过使用一个`InputStream、OutputStream或RandomAccessFile`来获取一个`FileChannel`实例。下面是通过`RandomAccessFile`打开`FileChannel`的示例： 

```java
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");  
FileChannel inChannel = aFile.getChannel();  
```

### 4.2 从FileChannel读取数据 
调用多个`read()`方法之一从`FileChannel`中读取数据。如： 

```java
ByteBuffer buf = ByteBuffer.allocate(48);  
int bytesRead = inChannel.read(buf);  
```
首先，分配一个`Buffer`。 从`FileChannel`中读取的数据将被读到`Buffer`中。 然后，调用`FileChannel.read()`方法。该方法将数据从`FileChannel`读取到`Buffer`中。`read()`方法返回的`int`值表示了有多少字节被读到了`Buffer`中。如果返回`-1`，表示到了文件末尾。 

### 4.3 向FileChannel写数据 
使用`FileChannel.write()`方法向`FileChannel`写数据，该方法的参数是一个`Buffer`。如： 

```java
String newData = "New String to write to file..." + System.currentTimeMillis();  
  
ByteBuffer buf = ByteBuffer.allocate(48);  
buf.clear();  
buf.put(newData.getBytes());  
  
buf.flip();  
  
while(buf.hasRemaining()) {  
    channel.write(buf);  
}  
```

注意`FileChannel.write()`是在`while`循环中调用的。因为无法保证`write()`方法一次能向`FileChannel`写入多少字节，因此需要重复调用`write()`方法，直到`Buffer`中已经没有尚未写入通道的字节。 

### 4.4 关闭FileChannel 
用完FileChannel后必须将其关闭。如： 

```java
channel.close();  
```

### 4.5 FileChannel的position方法 
有时可能需要在`FileChannel`的某个特定位置进行数据的读/写操作。可以通过调用`position()`方法获取`FileChannel`的当前位置。 也可以通过调用`position(long pos)`方法设置`FileChannel`的当前位置。 

代码示例： 

```java
long pos = channel.position();  
channel.position(pos +123);  
```

如果将位置设置在文件结束符之后，然后试图从文件通道中读取数据，读方法将返回-1 —— 文件结束标志。 

如果将位置设置在文件结束符之后，然后向通道中写数据，文件将撑大到当前位置并写入数据。这可能导致“文件空洞”，磁盘上物理文件中写入的数据间有空隙。 

### 4.6 FileChannel的size方法 
`FileChannel`实例的`size()`方法将返回该实例所关联文件的大小。如： 

```java
long fileSize = channel.size();  
```

### 4.7 FileChannel的truncate方法 
可以使用`FileChannel.truncate()`方法截取一个文件。截取文件时，文件将中指定长度后面的部分将被删除。如： 

```java
channel.truncate(1024);  
```

这个例子截取文件的前1024个字节。 

### 4.8 FileChannel的force方法 
`FileChannel.force()`方法将通道里尚未写入磁盘的数据强制写到磁盘上。出于性能方面的考虑，操作系统会将数据缓存在内存中，所以无法保证写入到`FileChannel`里的数据一定会即时写到磁盘上。要保证这一点，需要调用`force()`方法。 `force()`方法有一个`boolean`类型的参数，指明是否同时将文件元数据（权限信息等）写到磁盘上。 

下面的例子同时将文件数据和元数据强制写到磁盘上： 

```java
channel.force(true);  
```

## 5 Socket通道-SocketChannel
`Java NIO`中的`SocketChannel`是一个连接到TCP网络套接字的通道。可以通过以下2种方式创建`SocketChannel`： 

- 打开一个SocketChannel并连接到互联网上的某台服务器。
- 一个新连接到达ServerSocketChannel时，会创建一个SocketChannel。

### 5.1 打开SocketChannel 
下面是`SocketChannel`的打开方式： 

```java
SocketChannel socketChannel = SocketChannel.open();  
socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));  
```

### 5.2 关闭SocketChannel 
当用完`SocketChannel`之后调用`SocketChannel.close()`关闭`SocketChannel`： 

```java
socketChannel.close();  
```

### 5.3 从SocketChannel读取数据 
要从`SocketChannel`中读取数据，调用一个`read()`的方法之一。以下是例子： 

```java
ByteBuffer buf = ByteBuffer.allocate(48);  
int bytesRead = socketChannel.read(buf);  
```

首先，分配一个`Buffer`。从`SocketChannel`读取到的数据将会放到这个`Buffer`中。 然后，调用`SocketChannel.read()`。该方法将数据从`SocketChannel` 读到`Buffer`中。`read()`方法返回的`int`值表示读了多少字节进`Buffer`里。如果返回的是`-1`，表示已经读到了流的末尾（连接关闭了）。 

### 5.4 写入SocketChannel 
写数据到`SocketChannel`用的是`SocketChannel.write()`方法，该方法以一个`Buffer`作为参数。示例如下： 

```java
String newData = "New String to write to file..." + System.currentTimeMillis();  
  
ByteBuffer buf = ByteBuffer.allocate(48);  
buf.clear();  
buf.put(newData.getBytes());  
  
buf.flip();  
  
while(buf.hasRemaining()) {  
    channel.write(buf);  
}  
```

注意`SocketChannel.write()`方法的调用是在一个`while`循环中的。`Write()`方法无法保证能写多少字节到`SocketChannel`。所以，我们重复调用`write()`直到`Buffer`没有要写的字节为止。 

### 5.5 非阻塞模式 
可以设置`SocketChannel`为 **非阻塞模式（non-blocking mode）**。设置之后，就可以在异步模式下调用`connect(), read() 和write()`。 

#### 5.5.1 connect() 
如果`SocketChannel`在非阻塞模式下，此时调用`connect()`，该方法可能在连接建立之前就返回了。为了确定连接是否建立，可以调用`finishConnect()`的方法。像这样： 

```java
socketChannel.configureBlocking(false);  
socketChannel.connect(new InetSocketAddress("www.google.com", 80));  
  
while(! socketChannel.finishConnect() ){  
    //wait, or do something else...  
}  
```

#### 5.5.2 write() 
非阻塞模式下，`write()`方法在尚未写出任何内容时可能就返回了。所以需要在循环中调用`write()`。前面已经有例子了，这里就不赘述了。 

#### 5.5.3 read() 
非阻塞模式下,`read()`方法在尚未读取到任何数据时可能就返回了。所以需要关注它的`int`返回值，它会告诉你读取了多少字节。 

### 5.6 非阻塞模式与选择器 
非阻塞模式与选择器搭配会工作的更好，通过将一或多个`SocketChannel`注册到`Selector`，可以询问选择器哪个通道已经准备好了读取，写入等。`Selector`与`SocketChannel`的搭配使用会在后面详讲。 


## 6 ServerSocket通道-ServerSocketChannel
`Java NIO`中的`ServerSocketChannel`是一个可以监听新进来的TCP连接的通道，就像标准IO中的`ServerSocket`一样。`ServerSocketChannel`类在`java.nio.channels`包中。 

这里有个例子： 

```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();  
  
serverSocketChannel.socket().bind(new InetSocketAddress(9999));  
  
while(true){  
    SocketChannel socketChannel =  
            serverSocketChannel.accept();  
  
    //do something with socketChannel...  
}  
```

### 6.1 打开ServerSocketChannel 
通过调用`ServerSocketChannel.open()`方法来打开`ServerSocketChannel`。如： 

```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();  
```

### 6.2 关闭ServerSocketChannel 
通过调用`ServerSocketChannel.close()`方法来关闭`ServerSocketChannel`。 如： 

```java
serverSocketChannel.close();  
```

### 6.3 监听新进来的连接 
通过`ServerSocketChannel.accept()`方法监听新进来的连接。当`accept()`方法返回的时候，它返回一个包含新进来的连接的 `SocketChannel`。因此，`accept()`方法会一直阻塞到有新连接到达。 

通常不会仅仅只监听一个连接，在`while`循环中调用`accept()`方法。 如下面的例子： 

```java
while(true){  
    SocketChannel socketChannel =  
            serverSocketChannel.accept();  
  
    //do something with socketChannel...  
}  
```

当然，也可以在`while`循环中使用除了`true`以外的其它退出准则。 

### 6.4 非阻塞模式 
`ServerSocketChannel`可以设置成非阻塞模式。在非阻塞模式下，`accept()`方法会立刻返回，如果还没有新进来的连接，返回的将是`null`。 因此，需要检查返回的`SocketChannel`是否是`null`。如： 

```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();  
  
serverSocketChannel.socket().bind(new InetSocketAddress(9999));  
serverSocketChannel.configureBlocking(false);  
  
while(true){  
    SocketChannel socketChannel =  
            serverSocketChannel.accept();  
  
    if(socketChannel != null){  
        //do something with socketChannel...  
    }  
}  
```

## 7 Datagram通道-DatagramChannel
`Java NIO`中的`DatagramChannel`是一个能收发UDP包的通道。因为UDP是无连接的网络协议，所以不能像其它通道那样读取和写入。它发送和接收的是数据包。 

### 7.1 打开DatagramChannel 

下面是`DatagramChannel`的打开方式： 

```java
DatagramChannel channel = DatagramChannel.open();  
channel.socket().bind(new InetSocketAddress(9999));  
```

这个例子打开的`DatagramChannel`可以在UDP端口9999上接收数据包。 

### 7.2 接收数据 
通过`receive()`方法从`DatagramChannel`接收数据，如： 

```java
ByteBuffer buf = ByteBuffer.allocate(48);  
buf.clear();  
channel.receive(buf);  
```
receive()方法会将接收到的数据包内容复制到指定的Buffer. 如果Buffer容不下收到的数据，多出的数据将被丢弃。 

### 7.3 发送数据 
通过`send()`方法从`DatagramChannel`发送数据，如: 
```java
String newData = "New String to write to file..." + System.currentTimeMillis();  
  
ByteBuffer buf = ByteBuffer.allocate(48);  
buf.clear();  
buf.put(newData.getBytes());  
buf.flip();  
  
int bytesSent = channel.send(buf, new InetSocketAddress("www.google.com", 80));  
```

这个例子发送一串字符到`www.google.com`服务器的UDP端口80。 因为服务端并没有监控这个端口，所以什么也不会发生。也不会通知你发出的数据包是否已收到，因为UDP在数据传送方面没有任何保证。 

### 7.4 连接到特定的地址 
可以将`DatagramChannel`“连接”到网络中的特定地址的。由于UDP是无连接的，连接到特定地址并不会像TCP通道那样创建一个真正的连接。而是锁住`DatagramChannel` ，让其只能从特定地址收发数据。 

这里有个例子: 

```java
channel.connect(new InetSocketAddress("www.google.com", 80));  
```

当连接后，也可以使用`read()`和`write()`方法，就像在用传统的通道一样。只是在数据传送方面没有任何保证。这里有几个例子： 

```java
int bytesRead = channel.read(buf);  
int bytesWritten = channel.write(but);  
```