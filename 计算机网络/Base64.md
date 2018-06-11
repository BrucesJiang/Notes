# Base64
Base64是网络上最常见的用于传输`8Bit`字节码的编码方式之一，事实上，它就是一种基于64个可打印字符来表示二进制数据的方法。起源于MIME协议规范。

我们说，Base64编码是从二进制到字符的过程，可以用于在HTTP环境下传递较长的标识信息。 例如我们可以将其用在HTTP表单和HTTP GET URL中的参数。 通常我们可能需要将二进制数据编码为适合放在URL（包括隐藏表单域）中的形式。此时，采用Base64编码具有不可读性。

## 原理
所谓Base64，就是选出64个字符——小写字母`a-z`、大写字母`A-Z`、数字`0-9`、符号`+`和`/`（再奖赏作为垫字的 `=`，实际上是65个字符）作为一个基本字符集。在数据编码过程中，其他所有符号都转换成这个字符集中的字符。

具体来说分为四步：

1. 将每三个字节作为一组，一共是24个二进制位
2. 将这24个二进制位分为四组，每个组有6个二进制位
3. 在每个组前面添加两个前导`0`，每个分组被扩展为32个二进制位，也就是四个字节
4. 根据下表，得到扩展后的每个字节的对应符号，这就是Base64的编码值。

| 索引 | 字符 | 索引 | 字符 | 索引 | 字符 | 
|:---:|:----:|:----:|:---:|:----:|:----:|
| 0   | A    | 1    | B   | 3    |  C   |
| 4   | D    | 5    | E   | 6    |  F   |
| 7   | G    | 8    | H   | 9    |  I   |
| 10  | J    | 11   | K   | 12   |  L   |
| 13  | M    | 14   | N   | 15   |  O   |
| 16  | P    | 17   | Q   | 18   |  R   |
| 19  | S    | 20   | T   | 21   |  U   |
| 22  | V    | W    | X   | 24   |  Y   |
| 25  | Z    | 26   | a   | 27   |  b   |
| 28  | c    | 29   | d   | 30   |  e   |
| 31  | f    | 32   | g   | 33   |  h   |
| 34  | i    | 35   | j   | 36   |  k   |
| 37  | l    | 38   | m   | 39   |  n   |
| 40  | o    | 41   | p   | 42   |  q   |
| 43  | r    | 44   | s   | 45   |  t   |
| 46  | u    | 47   | v   | 48   |  w   |
| 49  | x    | 50   | y   | 51   |  z   |
| 52  | 0    | 53   | 1   | 54   |  2   |
| 55  | 3    | 56   | 4   | 57   |  5   |
| 58  | 6    | 59   | 7   | 60   |  8   |
| 61  | 9    | 62   | +   | 63   |  /   |


因为，Base64将三个字节转化成四个字节，因此Base64编码后的文本，会比原文本大出三分之一左右。

英语单词Man如何转成Base64编码。

| 标题  | C1 | C2 | C3 |
|:----:|:---:|:---:|:--:|
| Text content | M | a | n |
| ASCII | 77 | 97 | 110 |
| Bit pattern| 01001101 | 01100001 | 01101110 |
| Index | 19 | 22 | 5 | 46 |
| Base64-Encoded | T | W | F | u |

1. "M"、"a"、"n"的ASCII值分别是77、97、110，对应的二进制值是01001101、01100001、01101110，将它们连成一个24位的二进制字符串010011010110000101101110。
2. 将这个24位的二进制字符串分成4组，每组6个二进制位：010011、010110、000101、101110。
3. 在每组前面加两个00，扩展成32个二进制位，即四个字节：00010011、00010110、00000101、00101110。它们的十进制值分别是19、22、5、46。
4. 根据上表，得到每个值对应Base64编码，即T、W、F、u。 

因此，`Man`的Base64编码就是`TWFu`。


如果字节数不足三，则这样处理:

1. 二个字节的情况：将这二个字节的一共16个二进制位，按照上面的规则，转成三组，最后一组除了前面加两个0以外，后面也要加两个0。这样得到一个三位的Base64编码，再在末尾补上一个"="号。
比如，"Ma"这个字符串是两个字节，可以转化成三组00010011、00010110、00010000以后，对应Base64值分别为T、W、E，再补上一个"="号，因此"Ma"的Base64编码就是TWE=。
2. 一个字节的情况：将这一个字节的8个二进制位，按照上面的规则转成二组，最后一组除了前面加二个0以外，后面再加4个0。这样得到一个二位的Base64编码，再在末尾补上两个"="号。
比如，"M"这个字母是一个字节，可以转化为二组00010011、00010000，对应的Base64值分别为T、Q，再补上二个"="号，因此"M"的Base64编码就是TQ==。

再举一个中文的例子，汉字"严"如何转化成Base64编码。

这里需要注意，汉字本身可以有多种编码，比如gb2312、utf-8、gbk等等，每一种编码的Base64对应值都不一样。下面的例子以utf-8为例。

首先，"严"的utf-8编码为E4B8A5，写成二进制就是三字节的"11100100 10111000 10100101"。将这个24位的二进制字符串，按照第3节中的规则，转换成四组一共32位的二进制值"00111001 00001011 00100010 00100101"，相应的十进制数为57、11、34、37，它们对应的Base64值就为5、L、i、l。

所以，汉字"严"（utf-8编码）的Base64值就是5Lil。

```javascript
/* Copyright (C) 1999 Masanao Izumo <iz@onicos.co.jp>
* Version: 1.0
* LastModified: Dec 25 1999
* This library is free. You can redistribute it and/or modify it.
*/

/*
* Interfaces:
* b64 = base64encode(data);
* data = base64decode(b64);
*/


var base64EncodeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
var base64DecodeChars = new Array(
-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
-1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1);

function base64encode(str) {
	var out, i, len;
	var c1, c2, c3;

	len = str.length;
	i = 0;
	out = "";
	while(i < len) {
		c1 = str.charCodeAt(i++) & 0xff;
		if(i == len){
			out += base64EncodeChars.charAt(c1 >> 2);
			out += base64EncodeChars.charAt((c1 & 0x3) << 4);
			out += "==";
			break;
		}
		c2 = str.charCodeAt(i++);
		if(i == len){
			out += base64EncodeChars.charAt(c1 >> 2);
			out += base64EncodeChars.charAt(((c1 & 0x3)<< 4) | ((c2 & 0xF0) >> 4));
			out += base64EncodeChars.charAt((c2 & 0xF) << 2);
			out += "=";
			break;
		}
		c3 = str.charCodeAt(i++);
		out += base64EncodeChars.charAt(c1 >> 2);
		out += base64EncodeChars.charAt(((c1 & 0x3)<< 4) | ((c2 & 0xF0) >> 4));
		out += base64EncodeChars.charAt(((c2 & 0xF) << 2) | ((c3 & 0xC0) >>6));
		out += base64EncodeChars.charAt(c3 & 0x3F);
	}
	return out;
}

function base64decode(str) {
	var c1, c2, c3, c4;
	var i, len, out;

	len = str.length;
	i = 0;
	out = "";
	while(i < len) {
		/* c1 */
		do {
			c1 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
		} while(i < len && c1 == -1);
		if(c1 == -1)
			break;

		/* c2 */
		do {
			c2 = base64DecodeChars[str.charCodeAt(i++) & 0xff];
		} while(i < len && c2 == -1);
		if(c2 == -1)
			break;

		out += String.fromCharCode((c1 << 2) | ((c2 & 0x30) >> 4));

		/* c3 */
		do {
			c3 = str.charCodeAt(i++) & 0xff;
			if(c3 == 61)
				return out;
			c3 = base64DecodeChars[c3];
		} while(i < len && c3 == -1);
		if(c3 == -1)
			break;

		out += String.fromCharCode(((c2 & 0XF) << 4) | ((c3 & 0x3C) >> 2));

		/* c4 */
		do {
			c4 = str.charCodeAt(i++) & 0xff;
			if(c4 == 61)
				return out;
			c4 = base64DecodeChars[c4];
		} while(i < len && c4 == -1);
		if(c4 == -1)
			break;
		out += String.fromCharCode(((c3 & 0x03) << 6) | c4);
	}
	return out;
}
```
上面代码中的base64encode()用于编码，base64decode()用于解码。

因此，对utf-8字符进行编码要这样写：

```javascript
sEncoded=base64encode(utf16to8(str));
```
然后，解码要这样写：
```javascript
sDecoded=utf8to16(base64decode(sEncoded));
```

[Stackoverflow有关Base64的讨论](https://stackoverflow.com/questions/5258057/should-i-embed-images-as-data-base64-in-css-or-html)