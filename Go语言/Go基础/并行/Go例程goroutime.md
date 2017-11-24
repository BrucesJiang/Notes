# Go例程goroutime
goroutime是一个轻量级线程。

使用示例

```go
package main

import "fmt"

func f(from string) {
    for i := 0; i < 3; i ++ {
        fmt.Println(from, ":", i)
    }
}

func main() {
    //同步方式调用函数
    f("ok")

    // 让函数以例程(goroutine)方式
    // 运行使用go f(s)
    // 这个例程将和调用它的协程并行执行
    go f("goroutine")

    //匿名方式开启一个例程
    go func(msg string) {
        fmt.Println(msg)
    }("going")

    // 上面的协程在调用之后就异步执行了，所以程序不用等待它们执行完成
    // 就跳到这里来了，下面的Scanln用来从命令行获取一个输入，然后才
    // 让main函数结束
    // 如果没有下面的Scanln语句，程序到这里会直接退出，而上面的协程还
    // 没有来得及执行完，你将无法看到上面两个协程运行的结果
    var input string
    fmt.Scanln(&input)
    fmt.Println("done")
}
```