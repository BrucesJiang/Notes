# Java开发必须掌握的一些线上排查命令

本文摘自[http://www.hollischuang.com/archives/1561](http://www.hollischuang.com/archives/1561)

作为一个合格的开发人员，不仅需要能够写得一手好代码，而且还需要能够排查问题。这里提到的排查问题不仅仅指在coding过程中的debug，还包括线上问题的排查。由于在生产环境中一般没有办法debug,所以我们需要借助一些常用命令来查看运行时的具体状况，这些运行时信息不仅限于运行日志、异常堆栈、堆使用情况，GC情况、JVM参数情况、线程情况等等。

给一个系统定位问题的时候，知识经验是关键，数据是依据，工具是运用只知识处理问题的手段。为了便于我们排查和解决问题，Sun公司为我们提供了一些常用命令。这些命令一般都是`jdk/lib/tools.jar`中类库的一层封装，随着JVM被安装到机器中。默认存在于`bin`目录中。

本文仅仅是对常用命令的概述，对每个命令的详细了解，参看各个分章节。

## jps
- **功能** -- 显示当前所有java进程PID
- **常用指令** 
  - jps -v 3331 : 显示虚拟机参数
  - jps -m 3331 : 现实传递给main()函数的参数
  - jps -l 3331 : 现实主类的全路径

## jinfo
- **功能** -- 实时查看虚拟机和调整虚拟机参数，可以显示未被显式指定的参数的默认值(`jps -v`则不能)。注意： Java 8已经不支持该指令
- **常用命令** 
  - jinfo -flag CMSIniniatingOccupancyFration 1444: 查询CMSIniniatingOccuancyFration参数值

## jstat
- **功能** -- 显示进程中的类装载、内存、垃圾收集、JIT编译等运行数据
- **常用指令**
  - jstat -gc 3331 250 20 : 查询进程3331（Java进程PID=3331）的垃圾收集状况，每250毫秒查询一次，一共查询20次。
  - jstat -gccause 3331 250 20 : 额外输出上次GC原因
  - jstat -class 3331: Java进程3331事件类装载、类卸载、总空间以及消耗的时间

## jmap
- **功能** -- 生成堆转储快照（heapdump）
- **常用命令** 
  - jmap -heap 3331 : 查看进程3331(通常是虚拟机进程PID)Java堆(heap)使用情况
  - jmap -histo 3331 : 查看堆内存(histogram)中的对象数量及大小
  - jmap -histo:live 3331 : JVM会先触发gc，然后再统计信息
  - jmap -dump:format=b,file=heapDump 3331 : 将内存使用的详细情况输出到文件，一般使用其他工具进行分析。

## jhat
- **功能** -- 一般与jmap配合使用，用来分析jmap生成的堆转储文件。由于有很多替代工具，例如，Eclipse Memory Analyzer,IBM HeapAnalyzer，所以很少使用。
- **常用命令**
  - jmap -dump:format=b,file=headpDump 3331 + jhat headDump: 解析Java堆转储文件，并启动一个Web Server 

## jstack
- **功能** -- 生成当前时刻的线程快照
- **常用指令** 
  - jstack 3331 : 查看线程情况
  - jstack -F 3331 : 正常输出不被响应时，使用该指令
  - jstack -l 3331 : 除堆栈外，显示关于锁的附件信息

## 常见问题的定位过程
### 频繁GC问题或内存溢出问题
1. 使用`jps`查看线程PID
2. 使用`jstat -gc PID 250 20`查看gc情况，一般比较关注PERM区的情况，查看GC的增长情况
3. 使用`jstat -gccause PID 250 20` 额外输出上次gc的原因
4. 使用`jmap -dump:format=b,file=heapDump 3331`生成堆转储文件
5. 使用`jhat`或可视化工具分析堆情况
6. 结合代码解决内存溢出或泄露问题

### 死锁问题
1. 使用`jps`查看线程ID
2. 使用`jstack PID`查看线程情况
