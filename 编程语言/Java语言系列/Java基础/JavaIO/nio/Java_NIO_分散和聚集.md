# Java NIO之分散/聚集
`Java NIO`开始支持`scatter/gather`，`scatter/gather`用于描述从`Channel`中读取或者写入到`Channel`的操作。

- **分散（scatter）** 从`Channel`中读取是指在读操作时将读取的数据写入多个`Buffer`中。因此，`Channel`将从`Channel`中读取的数据“分散（scatter）”到多个`Buffer`中。 
- **聚集（gather）** 写入`Channel`是指在写操作时将多个`Buffer`的数据写入同一个`Channel`，因此，`Channel`将多个`Buffer`中的数据“聚集（gather）”后发送到`Channel`。 

`scatter/gather`经常用于需要将传输的数据分开处理的场合，例如传输一个由消息头和消息体组成的消息，你可能会将消息体和消息头分散到不同的`Buffer`中，这样你可以方便的处理消息头和消息体。

## Scattering Reads
`Scattering Reads`是指数据从一个`Channel`读取到多个`Buffer`中。如下图描述： 

![scatter](./images/scatter.png)

下面是一个展示`scattering read`如何工作的代码示例：

```java
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBuffer.allocate(1024);

ByteBuffer[] bufferArray = {header, body};

channel.read(bufferArray);
```

注意`Buffer`首先被插入到数组，然后再将数组作为`channel.read()`的输入参数。`read()`方法按照`Buffer`在数组中的顺序将从`Channel`中读取的数据写入到`Buffer`，当一个`Buffer`被写满后，`Channel`紧接着向另一个`Buffer`中写。 

`Scattering Reads`在移动下一个`Buffer`前，必须填满当前的`Buffer`，这也意味着它不适用于动态消息(消息大小不固定)。换句话说，如果存在消息头和消息体，消息头必须完成填充（例如 128byte），`Scattering Reads`才能正常工作。 

## Gathering Writes
`Gathering Writes`是指数据从多个`Buffer`写入到同一个`channel`。如下图描述： 

![gather](./images/gather.png)

下面是一个展示`gathering write`如何工作的代码示例：

```java
ByteBuffer header = ByteBuffer.allocate(128);
ByteBuffer body   = ByteBUffer.allocate(1024);

//write data into buffers
ByteBuffer[] bufferArray = {header, body};

channel.write(bufferArray);
```

`bufferArray`是`write()`方法的入参，`write()`方法会按照`Buffer`在数组中的顺序，将数据写入到`channel`，注意只有`position`和`limit`之间的数据才会被写入。因此，如果一个`Buffer`的容量为`128byte`，但是仅仅包含`58byte`的数据，那么这`58byte`的数据将被写入到`channel`中。因此与`Scattering Reads`相反，`Gathering Writes`能较好的处理动态消息。 

