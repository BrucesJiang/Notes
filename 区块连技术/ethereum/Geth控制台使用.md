# Geth 控制台使用使用
在开发以太坊去中心化应用，免不了和以太坊进行交互，那就离不开Web3。
Geth 控制台（REPL）实现了所有的web3 API及Admin API，

## geth控制台 - 启动、退出
最简单启动方式如下：
```shell
$ geth console
```
geth控制台启动成功之后，可以看到`>`提示符。
退出输入`exit`

## geth 日志控制
- 重定向日志到文件
使用geth console启动时，在当前的交互界面下时不时出现日志。
可以使用以下方式把日志输出到文件。
```shell
$ geth console 2>>geth.log
```
可以新开一个命令行终端输入以下命令查看日志：
```
$ tail -f geth.log
```
- 重定向另一个终端
也可以把日志重定向到另一个终端，先在想要看日志的终端输入：
```shell
$ tty
```

就可以获取到终端编号，如：`/dev/ttys003`
然后另一个终端使用：
```shell
$ geth console 2>> /dev/ttys003
```
启动geth, 这是日志就输出到另一个终端。
如果不想看到日志还可以重定向到空终端：
```shell
$ geth console 2>> /dev/null
```
- 日志级别控制
使用`–verbosity`可以控制日志级别，如不想看到日志还可以使用：
```shell
$ geth --verbosity 0 console
```
启动一个开发模式测试节点
```shell
geth --datadir /home/xxx/testNet --dev console
```
技巧：如果我们经常使用一个方式来启动，可以把命令存为一个bash脚本。
~/bin你可以放一些常用的脚本，并把~/bin加入到环境变量PATH里。

## 连接geth节点
另外一个启动geth的方法是连接到一个geth节点：
```shell
$ geth attach ipc:/some/custom/path
$ geth attach http://191.168.1.1:8545
$ geth attach ws://191.168.1.1:8546
```
如连接刚刚打开的开发模式节点使用：

```shell
$ geth attach ipc:testNet/geth.ipc
```
