# 内置函数之make和new
Go对于二者的定义：
```go
func new(Type) *Type
//内建函数new分配内存，其中第一个实参为类型，而非值。返回值为指向该类型的新分配的零值得指针

func make(Type, size IntegerType) Type
//内建函数make分配并初始化一个类型为切片、映射、或通道的对象。其第一个实参为类型，而非值。make的返回类型与其参数相同，而非指向它的指针。其具体结果取决于具体的类型：
//切片：size指定了其长度。该切片的容量等于其长度。切片支持第二个整数实参可用来指定不同的容量：它必须不小于其长度，因此 make([]int, 0, 10)会分配一个长度为0，容量为10的切片。
//映射：初始分配的创建取决于size，但产生的映射长度为0。size可以省略，这种情况下就会分配一个小的起始大小。
//通道：通道的缓存根据指定的缓存容量初始化。若size为零或被省略，该信道即为无缓存的。
```
 - new(T)为每个新的类型T分配一片内存，初始化为0并且返回类型为*T的内存地址。这种方式 `返回一个执行类型为T，值为0的地址的指针`，它适用于值类型，如数组和结构体
 - make(T) 返回一个类型为T的初始值，仅仅适用于3中内建的引用类型：slice, map和channel

```go
package main

type Foo map[string]string
type Bar struct {
    thingOne string
    thingTwo int
}

func main() {
    // OK
    y := new(Bar)
    (*y).thingOne = "hello"
    (*y).thingTwo = 1

    // NOT OK
    z := make(Bar) // 编译错误：cannot make type Bar
    (*z).thingOne = "hello"
    (*z).thingTwo = 1

    // OK
    x := make(Foo)
    x["x"] = "goodbye"
    x["y"] = "world"

    // NOT OK
    u := new(Foo)
    (*u)["x"] = "goodbye" // 运行时错误!! panic: assignment to entry in nil map
    (*u)["y"] = "world"
}
```

上述程序说明了在map上使用new和make的区别以及可能发生的错误。试图 make() 一个结构体变量，会引发一个编译错误，这还不是太糟糕，但是 new() 一个映射并试图使用数据填充它，将会引发运行时错误！ 因为 new(Foo) 返回的是一个指向 nil 的指针，它尚未被分配内存。所以在使用 map 时要特别谨慎。