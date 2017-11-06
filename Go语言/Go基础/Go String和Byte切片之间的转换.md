# Go String和Byte切片之间的转换
***
> String转换到Byte数组时，每个byte(byte本质就是uint8)保存字符串对应字节的数值
> 
> Go的字符串是UTF-8编码的，每个字符长度是不确定的，可能是1,2,3或者4个字节结尾
> 例如：
>
```Go
package main

import "fmt"

func main() {

	str1    := "abcd"
	bytes1  := []byte(str1)
	fmt.Println(bytes1) 
	//Output:
	//[97 98 99 100]

	str2    := "中文"
	bytes2  := []byte(str2)
    fmt.Println(bytes2)
    //Output:
    //[228 184 173 230 150 135]
    //unicode，每个中文字符会由三个byte组成

    r1 := []rune(s1)
	fmt.Println(r1) 
	//Output:
	//[97 98 99 100]
	//每个字一个数值

	r2 := []rune(s2)
	fmt.Println(r2) 
	//Output:
	// [20013 25991]
	// 每个字一个数值
}
```