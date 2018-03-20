# Go非阻塞通道
默认情况下，通道发送和接收数据是`阻塞的`。但是，我们可以使用`select的一个default的选项来实现无阻塞发送或接收数据`,甚至可以将多个select的case选项结合起来使用。

```go
package main

import (
    "fmt"
)

func main() {
    messages := make(chan string)
    singals := make(chan bool)

    // 这是一个非阻塞接收。如果通道`messages`中有值，那么`select`语句将会执行
    // `<- messages`分支并获取值；否则，就会立刻执行`default`分支
    select {
        case msg := <-messages :
            fmt.Println("Received message", msg)
        default: 
            fmt.Println("no message receieved")
    }

    // 非阻塞写入
    msg := "hi"
    select{
        case messages <- msg :
            fmt.Println("send Message", msg)
        default:
            fmt.Println("no message sent")
    }

    //在`default`分支上运用多个`case`分支，以此来实现多路非阻塞选择`select`
    //这里我们尝试利用非阻塞的方式从`messages`和'signals'通道中获取值
    select {
        case msg := <- messages :
            fmt.Println("Received message", msg)
        case sig := <- singals:
            fmt.Println("Received singal", sig)
        default: 
            fmt.Println("no activity")
    }
}

```

