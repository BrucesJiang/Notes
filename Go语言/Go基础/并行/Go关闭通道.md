# Go关闭通道
关闭通道意味着将不会再有数据通过通道传递。这告诉接收端数据已经发送完毕。

```go
package main

import "fmt"

func main() {
    jobs := make(chan int, 5)
    done := make(chan bool)
    

    //接收端例程，无限接受来自发送端的数据，指导发送端
    //关闭
    go func() {
        for {
            j, more := <-jobs
            if more {
                fmt.Println("received job", j)
            } else {
                fmt.Println("received all jobs")
                done <- true
                return
            }
        }
    }()
    
    //发送端发送数据
    for j := 1; j <= 3; j++ {
        jobs <- j
        fmt.Println("sent job", j)
    }
    close(jobs)  //关闭通道
    fmt.Println("sent all jobs")
    <-done
}
```