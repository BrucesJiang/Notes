# Java NIO之选择器(Selector)
`Selector`（选择器）是Java NIO中能够检测一到多个NIO通道，并能够知晓通道是否为诸如写事件做好准备的组件。这样，一个单独的线程可以管理多个`Channel`，从而管理多个网络连接。 其原理类似于Linux系统中多路IO复用原理中的`select()`函数，同样使用的是`Reactor`模式。

## 1 为什么使用Selector
禁用单线程处理多个`Channel`的好处是，能够使用更少的线程处理通道。事实上，可以只用一个线程处理所有的通道。对于操作系统来说，线程之间的上下文切换开销很大，而且每个线程都要占用系统的一些资源（如内存）。因此，使用的线程越少越好。

但是，需要紧记，现代操作系统和CPU在多任务方面的表现越来越好，所以多线程的开销随着时间的推移，变得越来越小了。实际上，如果一个CPU有多个内核，不使用多个任务可能是在浪费CPU能力。不管怎么说，关于这种设计的讨论应该放在另外一篇文章中。在这里，我们只需要知道使用`Selector`能够处理多个通道就好了。

下面是单线程使用一个`Selector`处理三个`Channel`的示意图：

![overview-selectors](./images/overview-selectors.png)

## 2 创建一个Selector
我们可以通过调用`Selector.open()`方法来创建一个`Selector`, 就像这样：
```java
Selector selector = Selector.open();
```

## 3 向选择器(Selector)注册通道(Channel)
为了将`Channel`和`Selector`配合使用，我们需要向`Selector`注册`Channel`。这通过使用`SelectableChannel.register()`方法来完成，就像这样：

```java
channel.configureBlocking(false);

SelectorKey key = channel.register(selector, SelectorKey.OP_READ);
```

与`Selector`配合使用时，`Channel`必须处于 **非阻塞模式(Non-blocking mode)**。 这也就是说，我们不能将`FileChannel`与`Selector`配合使用，因为`FileChannel`能切换到 **非阻塞模式**。 但是套接字通道都可以。

注意`register()`方法的第二个参数。这是一个“interest集合”，意思是在通过`Selector`监听`Channel`时对什么事件感兴趣。可以监听四种不同类型的事件：
- `Connect`
- `Accept`
- `Read`
- `Write`


通道触发了一个事件意思是该事件已经就绪。所以，某个`channel`成功连接到另一个服务器称为“连接就绪”。一个`server socket channel`准备好接收新进入的连接称为“接收就绪”。一个有数据可读的通道可以说是“读就绪”。等待写数据的通道可以说是“写就绪”。

这四种事件用`SelectionKey`的四个常量来表示：
- `SelectionKey.OP_CONNECT`
- `SelectionKey.OP_ACCEPT`
- `SelectionKey.OP_READ`
- `SelectionKey.OP_WRITE`

如果你对不止一种事件感兴趣，那么可以用“位或”操作符将常量连接起来，如下：

```java
int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
```

在下面还会继续提到interest集合。

### 3.1 SelectionKey
我们说，当向`Selector`注册`Channel`时，`register()`方法会返回一个`SelectionKey`对象。这个对象包含了一些你感兴趣的属性： 

- interest集合
- ready集合
- Channel
- Selector
- 附加的对象（可选）

下面我们会描述这些属性。

#### 3.1.1 interest集合 
我们说，`interest`集合是我们所选择的感兴趣的事件集合。可以通过`SelectionKey`读写`interest`集合，像这样： 

```java
int interestSet = selectionKey.interestOps();  
  
boolean isInterestedInAccept  = (interestSet & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT；  
boolean isInterestedInConnect = interestSet & SelectionKey.OP_CONNECT;  
boolean isInterestedInRead    = interestSet & SelectionKey.OP_READ;  
boolean isInterestedInWrite   = interestSet & SelectionKey.OP_WRITE;  
```

可以看到，用“位与”操作`interest`集合和给定的`SelectionKey`常量，可以确定某个确定的事件是否在`interest`集合中。 

#### 3.1.2 ready集合 
`ready集合`是通道已经准备就绪的操作的集合。在一次选择(`Selection`)之后，我们会首先访问这个`ready set`。 `Selection`将在下一小节进行解释。可以这样访问`ready集合`： 

```java
int readySet = selectionKey.readyOps(); 
```
可以用像检测`interest集合`那样的方法，来检测`channel`中什么事件或操作已经就绪。但是，也可以使用以下四个方法，它们都会返回一个布尔类型： 

```java
selectionKey.isAcceptable();  
selectionKey.isConnectable();  
selectionKey.isReadable();  
selectionKey.isWritable();  
```

#### 3.1.3 Channel + Selector 
从`SelectionKey`访问`Channel`和`Selector`很简单。如下： 

```java
Channel  channel  = selectionKey.channel();  
Selector selector = selectionKey.selector();  
```

#### 3.1.4 附加的对象 
可以将一个对象或者更多信息附着到`SelectionKey`上，这样就能方便的识别某个给定的通道。例如，可以附加 与通道一起使用的`Buffer`，或是包含聚集数据的某个对象。使用方法如下： 

```java
selectionKey.attach(theObject);  
Object attachedObj = selectionKey.attachment();  
```

还可以在用`register()`方法向`Selector`注册`Channel`的时候附加对象。如： 

```java
SelectionKey key = channel.register(selector, SelectionKey.OP_READ, theObject);  
```

## 4 通过选择器(Selector)选择通道(Channel)
一旦向Selector注册了一或多个通道，就可以调用几个重载的select()方法。这些方法返回你所感兴趣的事件（如连接、接受、读或写）已经准备就绪的那些通道。换句话说，如果你对“读就绪”的通道感兴趣，select()方法会返回读事件已经就绪的那些通道。 

下面是这些`select()`方法：
- `int select()`
- `int select(long timeout)`
- `int selectNow()`

`select()`方法将会一直阻塞直到有一个通道在你注册的事件上就绪了

`select(long timeout)`方法和`select()`方法一样，除了会阻塞一个最大时间`timeout`(参数)

`selectNow()`方法不会阻塞，不管什么通道就绪都立刻返回（此方法执行非阻塞的选择操作。如果自从前一次选择操作后，没有通道变成可选择的，则此方法直接返回零。）

`select()`方法返回的`int`返回值表明有多少个`Channel`已经就绪。也就是说，从上次调用`select()`方法到目前有多少个`Channel`就绪。 如果调用`select()`方法，因为有一个通道变成就绪状态，返回了`1`，若再次调用`select()`方法，如果另一个通道就绪了，它会再次返回`1`。如果对第一个就绪的`channel`没有做任何操作，现在就有`2`个就绪的通道，但在每次`select()`方法调用之间，只有一个通道就绪了。

### 4.1 selectedKeys()
一旦调用了`select()`方法，并且返回值表明有一个或更多个通道就绪了，然后可以通过调用`selector`的`selectedKeys()`方法，访问“已选择键集（selected key set）”中的就绪通道。如下所示： 

```java
Set<SelectionKey> selectedKeys = selector.selectedKeys();
```

当向`Selector`注册`Channel`时，`Channel.register()`方法会返回一个`SelectionKey` 对象。这个对象代表了注册到该`Selector`的通道。可以通过`SelectionKey`的`selectedKeySet()`方法访问这些对象。 

可以遍历这个已选择的键集合来访问就绪的通道。如下： 

```java
Set<SelectionKey> selectedKeys = selector.selectedKeys();

Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

while(keyIterator.hasNext()) {

	SelectionKey key = keyIterator.next();

	if(key.isAcceptable()) {
		// a connection was accepted by a ServerSocketChannel
	}else if(key.isConnectable()) {
		// a connection was established with a remote server
	}else if(key.isReadable()) {
		// a channel is ready fo reading
	}else if(key.isWritable()) {
		// a channel is ready fo writing
	}

	keyIterator.remove();
}
```
这个循环遍历已选择键集中的每个键，并检测各个键所对应的通道的就绪事件。 

注意每次迭代末尾的`keyIterator.remove()`调用。`Selector`不会自己从已选择键集中移除`SelectionKey`实例。必须在处理完通道时自己移除。下次该通道变成就绪时，`Selector`会再次将其放入已选择键集中。 

`SelectionKey.channel()`方法返回的通道需要转型成你要处理的类型，如`ServerSocketChannel`或`SocketChannel`等。 

### 4.2 wakeup()
某个线程调用`select()`方法后阻塞了，即使没有通道已经就绪，也有办法让其从`select()`方法返回。只要让其它线程在第一个线程调用`select()`方法的那个对象上调用`Selector.wakeup()`方法即可。阻塞在`select()`方法上的线程会立马返回。 

如果有其它线程调用了`wakeup()`方法，但当前没有线程阻塞在`select()`方法上，下个调用`select()`方法的线程会立即“醒来（wake up）”。

### 4.3 close()
用完`Selector`后调用其`close()`方法会关闭该`Selector`，且使注册到该`Selector`上的所有`SelectionKey`实例无效。通道本身并不会关闭。


## 完整的Selector示例
这里有一个完整的示例，打开一个Selector，注册一个通道注册到这个Selector上(通道的初始化过程略去),然后持续监控这个Selector的四种事件（接受，连接，读，写）是否就绪。 

```java
Selector selector = Selector.open();

channel.configureBlocking(false);

SelectionKey key = channel.register(selector, SelectionKey.OP_READ);

while(true) {

	int readyChannels = selector.select();

	if(readyChannels == 0) continue;

	Set<SelectionKey> selectedKeys = selector.selectedKeys();

	Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

	while(keyIterator.hasNext()) {
		SelectionKey key = keyIterator.next();

		if(key.isAcceptable()) {
			// a connection was accepted by a ServerSocketChannel
		}else if(key.isConnectable()) {
			// a connection was established with a remote server
		}else if(key.isReadable()) {
			// a channel is ready fo reading
		}else if(key.isWritable()) {
			// a channel is ready fo writing
		}

		keyIterator.remove();
	}
}

```