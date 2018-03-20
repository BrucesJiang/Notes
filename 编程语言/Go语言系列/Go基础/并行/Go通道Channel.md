Go通道Channel
通道(Channel):用于连接两个并行例程(goroutime),用于两个例程之间通信。例如，在一个例程中写入数据，在另外一个例程中读取数据。

```go
package main

import "fmt"

func main() {
    //创建一个用于传递string类型的channel
    msg := make(chan string)

    // channel <- 向通道中写入数据
    // 开启一个新的例程
    go func() {msg <- "o"}

    //在本例程中接收通道数据
    // <- chan
    m := <- msg
    fmt.Println(m)
}

```

在运行程序时，从一个例程传递到另外一个例程，默认情况下，例程之间的通信同步的，也就是说数据的发送端和接收端必须配对使用。Channel的这种`阻塞`例程性质，使得程序为尾部不用添加额外等待代码，也可以接收到数据。上述示例中，主例程被阻塞到能够获取到发送端发来的信息时才可以继续执行。