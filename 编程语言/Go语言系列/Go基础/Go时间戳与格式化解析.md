# Go时间戳与格式化解析
本文主要讲了Go语言对于时间处理，时间戳格式化解析等的支持。
## 时间

```go
package main

import (
    "fmt"
    "time"
)

func main() {
    p := fmt.Println

    //获取当前时间
    now := time.Now()

    p(now)

    //提供年，月，日来创建时间，此时时间应当与
    //地区联系在一起，也就是时区
    then := time.Date(2017, 11, 24, 11, 11, 11, 651387237, time.UTC)
    p(then)

    p(then.Year())
    p(then.Month())
    p(then.Day())
    p(then.Hour())
    p(then.Minute())
    p(then.Second())
    p(then.Nanosecond())
    p(then.Location())

    // 输出当天是周几，Monday-Sunday中的一个
    p(then.Weekday())

    // 下面的几个方法判断两个时间的顺序，精确到秒
    p(then.Before(now))
    p(then.After(now))
    p(then.Equal(now))

    // Sub方法返回两个时间的间隔(Duration)
    diff := now.Sub(then)
    p(diff)

    // 可以以不同的单位来计算间隔的大小
    p(diff.Hours())
    p(diff.Minutes())
    p(diff.Seconds())
    p(diff.Nanoseconds())

    // 你可以使用Add方法来为时间增加一个间隔
    // 使用负号表示时间向前推移一个时间间隔
    p(then.Add(diff))
    p(then.Add(-diff))
}

```

## 时间戳
程序的一个通常需求是计算从Unix起始时间开始到某个时刻的秒数，毫秒数，微秒数等

```go
package main

import (
    "fmt"
    "time"
)

func main() {
    //使用Unix和UnixNano分别获取从Unix起时间
    //到现在所经过的秒数和微秒数
    now := time.Now()
    secs := now.Unix()
    nanos := now.UnixNano()
    fmt.Println(now)

    // 注意这里没有UnixMillis方法，所以我们需要将
    // 微秒手动除以一个数值来获取毫秒
    millis := nanos / 1000000
    fmt.Println(secs)
    fmt.Println(millis)
    fmt.Println(nanos)

    // 反过来，你也可以将一个整数秒数或者微秒数转换
    // 为对应的时间
    fmt.Println(time.Unix(secs, 0))
    fmt.Println(time.Unix(0, nanos))
}
```

## 时间格式化和解析
Go使用模式匹配的方式来支持日期格式化和解析。

```go
package main

import (
    "fmt"
    "time"
)

func main() {
    p := fmt.Println

    // 这里有一个根据RFC3339来格式化日期的例子
    t := time.Now()
    p(t.Format("2006-01-02T15:04:05Z07:00"))

    // Format 函数使用一种基于示例的模式匹配方式，
    // 它使用已经格式化的时间模式来决定所给定参数
    // 的输出格式
    p(t.Format("3:04PM"))
    p(t.Format("Mon Jan _2 15:04:05 2006"))
    p(t.Format("2006-01-02T15:04:05.999999-07:00"))

    // 对于纯数字表示的时间来讲，你也可以使用标准
    // 的格式化字符串的方式来格式化时间
    fmt.Printf("%d-%02d-%02dT%02d:%02d:%02d-00:00\n",
        t.Year(), t.Month(), t.Day(),
        t.Hour(), t.Minute(), t.Second())

    // 时间解析也是采用一样的基于示例的方式
    withNanos := "2006-01-02T15:04:05.999999999-07:00"
    t1, e := time.Parse(
        withNanos,
        "2012-11-01T22:08:41.117442+00:00")
    p(t1)
    kitchen := "3:04PM"
    t2, e := time.Parse(kitchen, "8:41PM")
    p(t2)

    // Parse将返回一个错误，如果所输入的时间格式不对的话
    ansic := "Mon Jan _2 15:04:05 2006"
    _, e = time.Parse(ansic, "8:41PM")
    p(e)

    // 你可以使用一些预定义的格式来格式化或解析时间
    p(t.Format(time.Kitchen))
}
```

