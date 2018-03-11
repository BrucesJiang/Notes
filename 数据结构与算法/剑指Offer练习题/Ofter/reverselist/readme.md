#反向输出文件
>words.txt 是需要反向输出的英文文章。
>ReverseList.java 文件中的 reversed() 方法用于实现反向迭代，其中的部分代码已经给出。
>FileOperate.java 文件中包含两个方法： readFile(String pathName) 和 outFile(String pathName,String result)，readFile(String pathName) 的作用是读取 words.txt 文件中的内容； outFile(String pathName,String result) 的作用是将反向迭代的结果输出到文件中。

##目标
>完善 reversed() 方法使之实现反向迭代
>完成 readFile(String pathName) 和 outFile(String pathName,String result) 两个方法
>单词中的分隔符为空格 " "

##提示语
>words.txt 中的内容有换行，拼接字符串时注意分隔符，换行用空格 " "分隔符处理
>写入文件时不要换行
>文件路径已经在测试类中传入

##知识点
>文件操作
>迭代器
>foreach
>内部类
