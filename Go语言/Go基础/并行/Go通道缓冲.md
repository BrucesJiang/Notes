# Go通道缓冲
默认情况下，通道是不带缓冲的，也就是说，发送端发送数据就会有一个接收端接收数据，如果在接收端(发送端)没有接收数据(发送数据)的情况下，连续发送数据(接收数据)，发送端(接收端)都会被阻塞。缓冲通道允许在没有接收端没有取走数据的情况下向通道中存放多个同种数据。

示例代码中说明这一情况：

```go
package main

import "fmt"

func main() {
    //使用make创建一个缓冲区大小为2的通道
    msg := make(chan string, 2)
    
    //
    msg <- "buffered"
    msg <- "channel"

    fmt.Println(<-msg)
    fmt.Println(<-msg)

}

```