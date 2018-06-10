# Java NIO之缓冲区(Buffer)
`Java NIO`中的`Buffer`用于和`NIO`通道`Channel`进行交互。 据我们所知，数据是从通道读入缓冲区，或从缓冲区写入到通道中的。

缓冲区的本质是一块可以写入数据，并且可以从中读取数据的内存。这块内存被包装成`NIO Buffer`对象，并提供了一组方法用来方便的访问这块内存。

## 1 Buffer的基本用法
用`Buffer`读写数据基本上遵循以下四个步骤：
1. 将数据写入`Buffer`
2. 调用`buffer.flip()`
3. 从`Buffer`中读取数据
4. 调用`buffer.clear()`或`buffer.compact()`

当我们向`Buffer`中写入数据时，这个`Buffer`会纪录我们写入了多少数据。一旦我们需要从`Buffer`中读取数据，需要通过`flip()`方法将`Buffer`从写模式切换到读模式。 在读模式下，可以读取之前写入到`Buffer`的所有数据。

一旦读取了所有数据，我们需要清空这个`Buffer`, 以便它可以再次被写入。 有两种方式能清空缓冲区：调用`clear()`方法或调用`compact()`方法。`clear()`方法清空整个缓冲区。`compact()`方法仅仅清空已经读取数据的空间。任何未读的数据都被移到缓冲区的起始处，新写入的数据将放到缓冲区未读数据的后面。 

下面是一个`Buffer`使用的例子，展示了`write, flip, read, clear`等操作的用法：

```java
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();

//create buffer with capacity of 48 bytes
ByteBuffer buf = ByteBuffer.allocate(48);

int bytesRead = inChannel.read(buf); //read into buffer.
while (bytesRead != -1) {

  buf.flip();  //make buffer ready for read

  while(buf.hasRemaining()){
      System.out.print((char) buf.get()); // read 1 byte at a time
  }

  buf.clear(); //make buffer ready for writing
  bytesRead = inChannel.read(buf);
}
aFile.close();
```
### 1.1 Buffer的capacity,position和limit
`Buffer`本质上是一块可以写入数据，然后读取数据的内存。这块内存被包装成一个`NIO Buffer`对象，并提供了一组方法用来方便访问这块内存。

为了理解`Buffer`是如何工作的，它有三个我们需要熟悉的属性。它们是：
1. `capacity`
2. `position`
3. `limit`

`position`和`limit`的含义取决于`Buffer`处于读模式还是写模式。无论`Buffer`处于读模式还是写模式，`Capacity`都具有相同的含义。

这里有一个`capacity`,`position`和`limit`在读写模式中的说明。图例下面是详细说明：

![buffers-modes](./images/buffers-modes.png)

#### 1.1.1 capacity
作为一个内存块，`Buffer`有一个固定的大小，被称作 `capacity`。 我们只能向 `Buffer`中写入`capacity`个`byte,long,char`等等。一旦`Buffer`满了，在继续写入数据之前，我们能需要清空它（读取数据或清空）

#### 1.1.2 position
当你写数据到`Buffer`中时，`position`表示当前的位置。初始的`position`值为0.当一个`byte`、`long`等数据写到`Buffer`后，`position`会向前移动到下一个可插入数据的`Buffer`单元。`position`最大可为`capacity – 1`。 

当读取数据时，也是从某个特定位置读。当将`Buffer`从写模式切换到读模式，`position`会被重置为`0`。当从`Buffer`的`position`处读取数据时，`position`向前移动到下一个可读的位置。 

#### 1.1.3 limit
在写模式下，`Buffer`的`limit`表示你最多能往`Buffer`里写多少数据。 写模式下，`limit`等于`Buffer`的`capacity`。 

当切换`Buffer`到读模式时， `limit`表示你最多能读到多少数据。因此，当切换`Buffer`到读模式时，`limit`会被设置成写模式下的`position`值。换句话说，你能读到之前写入的所有数据（`limit`被设置成已写数据的数量，这个值在写模式下就是`position`）

## 2. Buffer的类型
`Java NIO`有以下几种类型的 `Buffer`：
- ByteBuffer
- MappedByteBuffer
- CharBuffer
- DoubleBuffer
- FloatBuffer
- IntBuffer
- LongBuffer
- ShortBuffer

正如我们所见，这些`Buffer`类型代表了不同的数据类型。 换句话说，就是可以通过`char`，`short`，`int`，`long`，`float` 或 `double` 类型来操作缓冲区中的字节。 

MappedByteBuffer 有些特别，在涉及它的专门章节中再讲。 

### 2.1 Buffer的分配(Allocating a Buffer)
要想获得一个`Buffer`对象首先要进行分配。 每一个`Buffer`类都有一个`allocate`方法。

下面是一个分配`48`字节`capacity`的`ByteBuffer`的例子。 
```java
ByteBuffer buf = ByteBuffer.allocate(48);
```

下面是一个分配`1024`字符`capacity`的`CharBuffer`空间的例子：
```java
CharBuffer buf = CharBuffer.allocate(1024); // 2 << 10
```

### 2.3 向Buffer中写数据
可以使用下面两种方法向`Buffer`中写入数据：
1. 从通道`Channel`写到缓冲区`Buffer`中
2. 通过`Buffer`的`put()`方法写入

下面是一个将数据从`Channel`写到`Buffer`的例子：
```java
int bytesRead = inChannel.read(buf); //read into buffer
```

下面是一个通过`Buffer`的`put()`方法写入数据的例子：
```java
buf.put(27);
```

`Buffer`中有许多其他版本的`put()`方法，允许我们使用不同的方法向`Buffer`中写入数据。例如，写到一个指定的位置，或者把一个字节数组写入到Buffer。 更多Buffer实现的细节参考JavaDoc。

#### 2.3.1 flip()方法 
`flip()`方法将`Buffer`从写模式切换到读模式。 调用`flip()`方将`position`重新设置为`0`,并且将`limit`设置成为之前的`position`值。 换句话说，`position`现在用于标记读的位置，`limit`表示之前写入了多少个`byte`,`char`等——现在能够读取多少个`byte`,`char`等。

### 2.4 从Buffer中读取数据
有两种方式可以从`Buffer`中读取数据：
1. 从`Buffer`中读取数据到`Channel`
2. 使用`get()`方法从`Buffer`中读取数据

下面是从`Buffer`中读取数据到`Channel`的例子：
```java
//read data from buffer into channel
int bytesWrite = inChannel.write(buf);
```

下面是使用`get()`方法从`Buffer`中读取数据的例子：
```java
byte aByte = buf.get();
```

`get()`方法有很多版本，允许你以不同的方式从`Buffer`中读取数据。例如，从指定`position`读取，或者从`Buffer`中读取数据到字节数组。更多`Buffer`实现的细节参考JavaDoc。

#### 2.4.1 rewind()方法
`Buffer.rewind()`将`position`设回 `0`，所以我们可以重读`Buffer`中的所有数据。`limit`保持不变，仍然表示能从`Buffer`中读取多少个元素（`byte`、`char`等）。

#### 2.4.2 clear()与compact()方法
一旦读完`Buffer`中的数据，需要让`Buffer`准备好再次被写入。可以通过`clear()`或`compact()`方法来完成。 

如果调用的是`clear()`方法，`position`将被设回 `0`，`limit`被设置成`capacity`的值。换句话说，`Buffer`被清空了。`Buffer`中的数据并未清除，只是这些标记告诉我们可以从哪里开始往`Buffer`里写数据。 

如果`Buffer`中有一些未读的数据，调用`clear()`方法，数据将“被遗忘”，意味着不再有任何标记会告诉你哪些数据被读过，哪些还没有。 

如果`Buffer`中仍有未读的数据，且后续还需要这些数据，但是此时想要先先写些数据，那么使用`compact()`方法。

`compact()`方法将所有未读的数据拷贝到`Buffer`起始处。然后将`position`设到最后一个未读元素正后面。`limit`属性依然像`clear()`方法一样，设置成`capacity`。现在`Buffer`准备好写数据了，但是不会覆盖未读的数据。 

#### 2.4.3 mark()与reset()方法
通过调用`Buffer.mark()`方法，可以标记`Buffer`中的一个特定`position`。之后可以通过调用`Buffer.reset()`方法恢复到这个`position`。例如： 

```java
buffer.mark();  
  
//call buffer.get() a couple of times, e.g. during parsing.  
  
buffer.reset();  //set position back to mark.  
```
#### 2.4.4 equals()与compareTo()方法
可以使用`equals()`和`compareTo()`方法两个`Buffer`。 
##### 2.4.4.1 equals() 
当满足下列条件时，表示两个`Buffer`相等： 

- 有相同的类型（`byte、char、int`等）。
- `Buffer`中剩余的`byte、char`等的个数相等。
- `Buffer`中所有剩余的`byte、char`等都相同。

如我们所见，`equals`只是比较`Buffer`的一部分，不是每一个在它里面的元素都比较。实际上，它只比较`Buffer`中的剩余元素。 

##### 2.4.4.2 compareTo()方法
`compareTo()`方法比较两个`Buffer`的剩余元素(`byte、char`等)， 如果满足下列条件，则认为一个`Buffer`“小于”另一个`Buffer`： 

- 第一个不相等的元素小于另一个`Buffer`中对应的元素。
- 所有元素都相等，但第一个`Buffer`比另一个先耗尽(第一个`Buffer`的元素个数比另一个少)。