# Go通道同步

可以使用通道来同步例程之间的执行。实例中通过获取同步通道数据来阻塞程序的执行来等待另外一个例程运行结束，也即是说main函数所在的例程在运行到<-done语句的时候会一直等待Worker函数所在的例程执行完成，向通道中写入数据才会继续执行(main从通道中获取到数据)。

```go
package main

import (
    "fmt"
    "time"
)


//该函数以例程的方式运行
//通道done被用于通知另外一个例程这个例程已经执行完成
func Worker(done chan bool) {
    fmt.Println("working ...")
    time.Sleep(time.Second);
    fmt.Println("done")
    
    //向通道发送一个数据，表示worker函数已经执行完成
    done <- true
}

func main() }{
    //使用例程来调用Worker函数，同时将通道`done`传递给例程
    //通道`done`被用来通知另外一个例程Worker函数已经执行完成
    done := make(chan bool, 1)

    go Worker(done)
    
    //阻塞，直到从Worker所在的例程中获得一个Worker执行完成的数据
    <- done
}
```

如果从main函数中删除<- done 语句，main函数可能在Worker例程开始运行前就已经结束。