# Printf和反射

## 类型判断常用结构
类型判断—— **type-switch** 结构： 一个类型分类函数，它有一个可变长度参数，可以是任意类型的数组，它会根据数组元素的实际类型执行不同的动作：

```go
func classifier(items ...interface{}) {
    for i, x := range items {
        switch x.(type) {
        case bool:
            fmt.Printf("Param #%d is a bool\n", i)
        case float64:
            fmt.Printf("Param #%d is a float64\n", i)
        case int, int64:
            fmt.Printf("Param #%d is a int\n", i)
        case nil:
            fmt.Printf("Param #%d is a nil\n", i)
        case string:
            fmt.Printf("Param #%d is a string\n", i)
        default:
            fmt.Printf("Param #%d is unknown\n", i)
        }
    }
}

```

在处理来自于外部的、类型未知的数据时，比如解析诸如 JSON 或 XML 编码的数据，类型测试和转换会非常有用。

## 反射
通过分析`fmt`包中的`Printf`来具体理解反射。Printf以及其他格式化输出函数都会使用反射来分析它的 `...` 参数。

Printf的函数声明为：

```go
func Printf(format string, args ...interface{}) (n int, err error)
```

`Printf`函数中的`...`参数为空接口类型。Printf使用反射包来解析这个参数列表。因此格式化字符串中只有%d而没有 %u 和 %ld，因为它知道这个参数是 unsigned 还是 long。这也是为什么 Print 和 Println在没有格式字符串的情况下还能如此漂亮地输出。

构造一个通用的输出函数，来具体更加直观的体现反射。

```go
package main

import (
    "os"
    "strconv"
)

type Stringer interface {
    String() string
}


type Celsius float64

func (c celsius) String() string {
    return strconv.FormatFloat(float64(c), 'f', 1, 64) + " °C"
}

type Day int

var dayName = []string{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}

func (day Day) String() string {
    return dayName[day]
}

func print(args ...interface{}) {
    for i, arg := range args {
        if i > 0 {os.Stdout.WriteString(" ")}
        switch a := arg.(type) { // type switch
            case Stringer:  os.Stdout.WriteString(a.String())
            case int:       os.Stdout.WriteString(strconv.Itoa(a))
            case string:    os.Stdout.WriteString(a)
            // more types
            default:        os.Stdout.WriteString("???")
        }
    }
}

func main() {
    print(Day(1), "was", Celsius(18.36))  // Tuesday was 18.4 °C
}

```
