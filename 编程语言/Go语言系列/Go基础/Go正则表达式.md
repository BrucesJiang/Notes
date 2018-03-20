# Go正则表达式
Go内置正则表达式，这里是一些常规用法

```go
package main

import (
    "bytes"
    "fmt"
    "regexp"
)

func main() {

    //测试模式是否匹配字符串
    //至少有一个a-z之间的字符存在
    match, _ := regexp.MatchString("p([a-z]+)ch", "peach")
    fmt.Println(match)

    
    //上面直接使用了字符串匹配的正则表达式，
    //对于其他的正则匹配任务，需要使用
    //`Compile`来使用一个优化过正则对象
    r, _ := regexp.Compile("p([a-z]+)ch")
   
    //
    fmt.Println(r.MatchString("peach"))

    // 这个方法检测字符串参数是否存在正则所约束的匹配
    fmt.Println(r.FindString("peach punch"))

    //该方法查找第一次匹配的索引，并返回匹配字符串的起始索引和结束索引
    fmt.Println(s,e := r.FindStringIndex("peach punch"))

    fmt.Println("Start : ", s, "End :", e)
    
    // 这个方法返回全局匹配的字符串和局部匹配的字符，比如
    // 这里会返回匹配`p([a-z]+)ch`的字符串
    // 和匹配`([a-z]+)`的字符串
    fmt.Println(r.FindStringSubmatch("peach punch"))

    // 和上面的方法一样，不同的是返回全局匹配和局部匹配的
    // 起始索引和结束索引
    fmt.Println(r.FindStringSubmatchIndex("peach punch"))

    // 这个方法返回所有正则匹配的字符，不仅仅是第一个
    fmt.Println(r.FindAllString("peach punch pinch", -1))

    // 这个方法返回所有全局匹配和局部匹配的字符串起始索引
    // 和结束索引
    fmt.Println(r.FindAllStringSubmatchIndex("peach punch pinch", -1))

    // 为这个方法提供一个正整数参数来限制匹配数量
    fmt.Println(r.FindAllString("peach punch pinch", 2))

    //上面我们都是用了诸如`MatchString`这样的方法，其实
    // 我们也可以使用`[]byte`作为参数，并且使用`Match`
    // 这样的方法名
    fmt.Println(r.Match([]byte("peach")))

    // 当使用正则表达式来创建常量的时候，你可以使用`MustCompile`
    // 因为`Compile`返回两个值
    r = regexp.MustCompile("p([a-z]+)ch")
    fmt.Println(r)

    // regexp包也可以用来将字符串的一部分替换为其他的值
    fmt.Println(r.ReplaceAllString("a peach", "<fruit>"))

    // `Func`变量可以让你将所有匹配的字符串都经过该函数处理
    // 转变为所需要的值
    in := []byte("a peach")
    out := r.ReplaceAllFunc(in, bytes.ToUpper)
    fmt.Println(string(out))
}

```