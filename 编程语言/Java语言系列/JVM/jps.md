# jps
## 摘要
`jps`命令位于`jdk`的`bin`目录下，其作用是显示当前系统的Java进程运行状况以及其ID号。`jps`相当于Solaris进程工具`ps`。不像`pgrep java`或`ps -ef | grep java`, 	`jps`并不使用应用程序名称来查找JVM实例。它查找所有的Java应用程序，即使没有Java执行体的Java应用程序，例如定制的起动器。另外，`jps`仅查找当前用户的Java进程，而不是当前系统中的Java进程。

## 位置
Java命令都在JDK的`$JAVA_HOME/bin`目录下，`jps`也不例外是Java自带的命令。

## 功能
`jsp(Java Virtual Machine Process Status Tool)`是从JDK 1.5开始提供的一个显示当前所有Java进程PID的命令。

## 原理
Java程序起动后，会在`java.io.tmpdir`指定的目录下生成一个名称类似与`hsperfdata\_user`的临时文件夹，这个文件夹里（Linux系统中为`/tmp/hsperfdata_${userName}`）有几个文件，名称就是当前运行Java进程的PID，因此列出当前运行的Java进程，就是将这个目录里的文件名进行罗列。如果添加参数，则是将这几个文件进行相关的解析。

例如在本机上测试的结果：

```shell
$ cd /tmp/hsperfdata_jd/
$ ls
9282 9535
$ jps 
9282 9535
```

## 使用
```shell
$ jps -help
usage: jps [-help]
       jps [-q] [-mlvV] [<hostid>]

Definitions:
      <hostid>:  <hostname>[:<port>]

```

### 参数及其含义

1. **-q** - 只显示PID, 不显示class名称，jar文件名和传递给main方法的参数
2. **-m** - 输出应用程序main方法的参数,在嵌入式JVM上可能是null。
3. **-l** - 输出应用程序main class完整的package名 或者 应用程序jar文件完整路径名
4. **-v** - 输出传递给JVM的参数

## JPS失效处理
**现象**： 用`ps -ef|grep java`能看到启动的java进程，但是用jps查看却不存在该进程的id。在该情况下，jconsole、jvisualvm可能无法监控该进程，其他java自带工具也可能无法使用

**分析**： `jps、jconsole、jvisualvm`等工具的数据来源就是这个文件（`/tmp/hsperfdata_userName/pid`)。所以当该文件不存在或是无法读取时就会出现`jps`无法查看该进程号，`jconsole`无法监控等问题

**原因**：

- （1）、磁盘读写、目录权限问题 若该用户没有权限写/tmp目录或是磁盘已满，则无法创建/tmp/hsperfdata_userName/pid文件。或该文件已经生成，但用户没有读权限

- （2）、临时文件丢失，被删除或是定期清理 对于linux机器，一般都会存在定时任务对临时文件夹进行清理，导致/tmp目录被清空。这也是我第一次碰到该现象的原因。常用的可能定时删除临时目录的工具为crontab、redhat的tmpwatch、ubuntu的tmpreaper等等。导致的这种现象的原因可能是jconsole监控进程，发现在某一时段后进程仍然存在，但是却没有监控信息了。

- （3）、java进程信息文件存储地址被设置，不在/tmp目录下 上面我们在介绍时说默认会在/tmp/hsperfdata_userName目录保存进程信息，但由于以上1、2所述原因，可能导致该文件无法生成或是丢失，所以java启动时提供了参数(-Djava.io.tmpdir)，可以对这个文件的位置进行设置，而jps、jconsole都只会从/tmp目录读取，而无法从设置后的目录读物信息。


