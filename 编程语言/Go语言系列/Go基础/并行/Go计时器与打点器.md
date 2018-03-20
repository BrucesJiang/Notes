# Go计时器Timer与打点器Ticker
Go内置的Timer和Ticker可以使得代码在某个时刻执行或者以一定的时间间隔重复执行。

## Timer

```go

package main

import (
    "fmt"
    "time"
)

func main() {

    //Timer代表了未来的某一个事件，计时器需要持续多长时间，然后计时器提供
    //一个通道，这个通道将在等待的时间结束后得到通知
    tiemer1 := time.NewTimer(time.Second * 2)
    
    // <-timer1.C  表示在`timer`的通道`C`上阻塞等待，直到有个值发送给该通道，
    // 通知通道计时器已经等待完成
    // timer.NewTiemr方法获取的timer1的结构体定义为
    // type Ticket struct {
    //      C <- chan Time
    // }
    <- timer1.C

    fmt.Println("Timer 1 expired")

    //如果仅仅需要等待，可以使用time.Sleep, tiemer可以在timer等待完成之间取消等待
    //
    tiemr2 := time.NewTimer(time.Second)

    go func(){
        <- timer2.C
        fmt.Println("Time 2 expired")        
    }()

    stop2 := timer2.Stop()

    if stop2 {
        fmt.Println("Timer 2 stopped")
    }
}
```
第一个timer1将在2秒后因从等待完成而结束，第二个timer2将在等待完成之前因被取消而结束。

## Ticker

Timer是让程序等待一段时间，这种等待仅仅做一次。Ticker是将一段代码按照一定的时间间隔重复执行

```go
package main

import (
    "time"
    "fmt"
)

func main() {
    //和Timer类似使用一个通道来发送数据
    //这里使用range来遍历通道数据，这些数据每隔500毫秒被发送一次
    ticker := time.NewTicker(time.Millisecond * 500)

    go func() {
        for t := range ticker.C {
            fmt.Println("Ticker at", t)
        }
    }()
    
    //这里在1500好秒后，停止Ticker
    time.Sleep(time.Millisecond * 15000)
    ticker.Stop()
    fmt.Println("Ticker Stopped")
}

```
