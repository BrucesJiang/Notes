# 字典(Map)
字典是GO语言内置的关联数据类型。

以实例说明其用法：

```go
package main 

import "fmt"

func main() {
    //创建Map 
    // make(map[Type]Type)
    m := make(map[string]int)

    // map[key]=value 设置元素的值
    m["k1"] = 7
    m["k2"] = 13

    //输出所有key-value
    fmt.Println("Map:", m)

    //通过key获取value
    v1 := m["k1"]
    fmt.Println("V1 :", v1)

    //内置函数返回字典元素的个数
    fmt.Println("Len:", len(m))

    //内置函数delete从字典中删除key-value
    delete(m, "k1")
    
    // 根据键来获取值有一个可选的返回值，这个返回值表示字典中是否
    // 存在该键，如果存在为true，返回对应值，否则为false，返回零值
    // 有的时候需要根据这个返回值来区分返回结果到底是存在的值还是零值
    // 比如字典不存在键x对应的整型值，返回零值就是0，但是恰好字典中有
    // 键y对应的值为0，这个时候需要那个可选返回值来判断是否零值。
    _, ok := m["k2"]
    fmt.Println("ok:", ok)

    // 可以用 ":=" 同时定义和初始化一个字典
    n := map[string]int{"foo": 1, "bar": 2}
    fmt.Println("map:", n)

}
```