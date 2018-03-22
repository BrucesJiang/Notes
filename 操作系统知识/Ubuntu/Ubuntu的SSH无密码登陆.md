# Ubuntu的SSH无密码登陆（14.04）

正常情况下，我们需要连上SSH的控制台输入用户名及其密码才行。如果两者全部正确，我们就可以访问，反之访问被服务端拒绝。不过相比而言还有一种比用密码更安全的登录方式，我们可以在登录SSH时通过加密密钥进行无密码登录。

如果你想启用这个安全的方式，我们只需简单的禁用密码登录并只允许加密密钥登录即可。使用这种方式时，客户端计算机上会产生一对私钥和公钥。接着客户端得把公钥上传到SSH服务端的authorized_key文件中去。在授予访问前，服务器及客户端电脑会校验这个密钥对。如果服务器上的公钥与客服端提交的私钥匹配则授予访问权限，否则访问被拒绝。

## 1. 安装Openssh服务端
```shell 
$ sudo apt-get update
$ sudo apt-get install openssh-server
```

## 2. 开启openssh服务
在OpenSSH已经成功安装在Ubuntu14.04操作系统上了之后，启动OpenSSH的服务。

```shell
$ sudo service ssh start
#或
$ sudo /etc/init.d/ssh start
```

## 3. 配置密钥对
安装并启动OpenSSH服务后，要配置公私密钥对。

```shell
# 生生成密钥对
$ ssh-keygen -t rsa
```
在运行完以上命令了以后，需要回答一系列的问题。首先选择保存密钥的路径，按回车将会选择默认路径即家目录的一个隐藏的.ssh文件夹。

在密钥对生成以后，需要将客户端上的公钥复制到SSH服务端或者主机，来创建对客户端的信任关系。运行以下命令复制客户端的公钥到服务端。

```shell
$ ssh-copy-id user@ip_address
```
在公钥上传之后，我们现在可以禁用通过密码登陆SSH的方式了。为此，我们需要通过以下命令用文本编辑器打开`/etc/ssh/ssh_config`。

```shell
$ sudo nano /etc/ssh/sshd_config
```

按照下图所示去掉几行注释并进行一些赋值
![openssh-server](./images/openssh-server.md)

## 4. 重启SSH服务
最后，在我们配置完SSH服务端后，为了使改动生效我们需要重启SSH服务。在终端或控制台运行以下命令重启。

```shell
$ sudo service ssh restart
#或
$ sudo /etc/init.d/ssh restart
```
这样就可以不用密码仅用密钥对的方式登录ssh服务端