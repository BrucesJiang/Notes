超时机制对于连接外部资源的程序很重要，如果不使用超时机制，就需要限制程序的执行时间。我们利用channel和select很容易实现超时。

```go
package main

import (
    "time"
    "fmt"
)

func main() {

    //假设执行一个外部调用，2秒后将结果写入c1
   c1 := make(chan string, 1)

   go func() {
        time.Sleep(time.Second * 2)
        c <- "result 1"
   }()

   //使用select实现超时， `res := <-c1` 等待通道结果，
   // `<- Time.After`则在等待1秒后返回一个值，因为select
   //首先执行那些不再阻塞的case，这里会执行超时程序，如果
   // `res := <- c1` 超时1秒没有执行
   select {
   case res := <-c1:
        fmt.Println(res)
   case <-time.After(time.Second * 1):
        fmt.Println("timeout 1")
   }

   //如果我们将超时时间设为3秒，这个时候 `res := <- c2`将在
   //超时case之前执行，而且能够输出写入通道的c2的值
   c2 := make(chan string, 1)
   go func() {
        time.Sleep(time.Second * 2)
        c2 <- "result 2"
   }()
   
   select {
   case res := <-c2:
        fmt.Println(res)
   case <- time.After(time.Second * 3):
        fmt.Println(timeout 2")
   }
}


