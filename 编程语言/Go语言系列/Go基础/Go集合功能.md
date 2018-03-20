# Go集合功能
我们经常需要处理一些集合数据，例如选出所有符合条件的数据或者使用一个自定义的函数将一个集合元素拷贝到另外一个集合。

在一些语言中，例如Java，通常是使用泛化数据结构或算法。但是Go不支持泛化类型，如果程序或数据类型需要操作集合，通常是为集合提供一些操作函数

下列代码演示了操作strings切片的集合函数。在有些情况下，使用内联集合操作代码回更清晰，而不是去创建新的函数。


```go
package main

import "strings"
import "fmt"

//返回t在vs中第一次出现的索引，如果没有找到t,返回 -1
func Index(vs []string, t string) int {
    for i, v := range vs {
        if v == t {
            return i
        }
    }
    return -1
}


//如果t存在于vs中，那么返回true,否则返回false
func Include(vs []string, t string) bool {
    for _, v := range vs {
        if v == t {
            return true
        }
    }
    return false
}

//如果分别使用vs中所有字符串任何f的参数都能让f返回true
//那么返回true, 否则返回false
func Any(vs []string, f func(string) bool) bool {
    for _, v := range vs {
        if f(v) {
            return true
        }
    }
    return false
}


//如果分别使用vs中所有字符串所有f的参数都能让f返回true
//那么返回true, 否则返回false
func All(vs []string, f func(string) bool) bool {
    for _, v := range vs {
        if !f(v) {
            return false
        }
    }
    return true
}

//返回一个新的字符串切片，切片的元素为vs能够让函数f返回true的元素
func Filter(vs []string, f func(string) bool) []string {
    vsf := make([]string, 0)
    for _, v := range vs {
        if f(v) {
            vsf = append(vsf, v)
        }
    }
    return vsf 
}

//返回一个string类型的切片，切片的元素为vs中所有字符串作为f函数
//参数所返回的结果
func Map(vs []string, f func(string) string) []string {
    vsm := make([]string, len(vs))
    for _, v := range vs {
        vsm[i] = f(v)
    }
    return vsm
}

func main() {
    var strs = []string{"peach", "apple", "pear", "plum"}

    fmt.Println(Index(strs, "pear")) 
    
    fmt.Println(Include(strs, "grape"))   

    fmr.Println(Any(strs, func(v string) bool {
        return strings.HasPrefix(v, "p")
    }))

    fmt.Println(All(strs, func(v string) bool {
        return strings.HasPrefix(v, "p")
    }))

    fmt.Println(Filter(strs, func(v string) bool {
        return strings.Contains(v, "e")
    }))

    fmt.Println(Map(strs, strings.ToUpper))
}




```