# Go通道选择
Go的select关键字允许例程等待多个例程，将例程，通道和select结合。

```go
package main

import(
    "time"
    "fmt"
)

func main() {
    ch1 := make(chan string)
    ch2 := make(chan string)

    //模拟并行例程的阻塞操作
    go func() {
        time.Sleep(time.Second * 1)
        ch1 <- "C1"
    }()

    go func() {
        time.Sleep(time.Second * 2) //超时
        ch2 <- "C2"
    }

    for i:= 0; i < 2; i ++ {
        select {
            case m1 := <- c1 
                fmt.Println("Received ", m1)
            case m2 := <- c2
                fmt.Println("Received ", m2)
        }
    }
}

```