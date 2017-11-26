# Go通道方向
当使用通道作为函数参数时，可以指定该通道的读写性质：可读，可写。这种设置可以提高程序参数类型的安全。

```go
package main

import "fmt"


// ping函数只接收能够发送数据的通道作为参数，试图从这个通道接收数据
// 会导致编译错误，这里只写的定义方式为`chan<- string`表示这个类型为
// 字符串的通道为只写通道
func ping(pings chan<- string, msg string) {
    pings <- msg
}

//pong函数接受两个通道，一个只读通道pings , 一个只写通道pongs
func pong(pings <-chan string, pongs chan<- string){
    msg := <- pings
    pongs <- msg
}
func main() {
    pings := make(chan<- string, 1)
    pongs := make(<-chan string, 1)

    ping(pings, "Password Message")
    pong(pints, pongs)
    fmt.Println(<-pongs)
}
```
