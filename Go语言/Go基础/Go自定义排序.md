# Go自定义排序
编程过程中对于排序的需求可能是多种多样的，可能是各种数据类型的排序，可能是根据不同的依据排序。这里介绍了一种尽可能通用的自定义排序：


按照字符串长度进行排序
```go
package main

import (
    "sort"
    "fmt"
)

//为了能够使用自定义函数排序，需要一个对应的排序类型
//这里使用内置的字符串数据定义一个别名
type ByLength []string

// 我们实现了sort接口的Len，Less和Swap方法
// 这样我们就可以使用sort包的通用方法Sort
// Len和Swap方法的实现在不同的类型之间大致
// 都是相同的，只有Less方法包含了自定义的排序
// 逻辑，这里我们希望以字符串长度升序排序
func (s ByLength) Len() int {
    return len(s)
}

func (s ByLength) Swap(i, j int) {
    s[i], s[j]= s[j], s[i]
}

func (s ByLength) Less(i, j int) bool {
    return len(s[i]) < len(s[j])
}

// 一切就绪之后，我们就可以把需要进行自定义排序
// 的字符串类型fruits转换为ByLength类型，然后使用
// sort包的Sort方法来排序
func main() {
    fruits := []string{"peach", "banana", "kiwi"}
    sort.Sort(ByLength(fruits))
    fmt.Println(fruits)
}
```
同样的，对于其他的类型，使用这种方法，我们可以为Go的切片提供任意的排序方法。归纳一下就是：

1.创建自定义排序类型
2.实现sort包的接口方法Len,Swap和Less
3.使用sort.Sort方法来排序