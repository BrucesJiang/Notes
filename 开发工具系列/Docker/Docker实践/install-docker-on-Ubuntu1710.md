# Installing Docker CE on Ubuntu 17.10 Artful Aardvark

a release file for Ubuntu 17.10 Artful Aardvark is not available on [Download Docker](https://download.docker.com/linux/ubuntu/dists/).

If you are used to installing Docker to your development machine with get-docker script, that won't work either. So the solution is to install Docker CE from the zesty package.

```shell
$ sudo apt-get update
$ sudo apt-get install apt-transport-https  ca-certificates curl software-properties-common 
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
$ sudo apt-key fingerprint 0EBFCD88

$ sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu zesty stable"

$ sudo apt-get update
$ sudo apt-get install docker-ce
```

## config for Certain User
# 创建一个docker组
docker后台进程是绑定的Unix的socket而不是TCP端口。默认情况下，Unix的socket属于用户root，其它用户要使用要通过sudo命令。由于这个原因，docker daemon通常使用root用户运行。

为了避免使用sudo当你使用docker命令的时候，创建一个Unix组名为docker并且添加用户。当docker daemon启动，它会分配Unix socket读写权限给所属的docker组。

`注意：`docker组不等价于用户root，如果想要知道的更多关于安全影响，查看docker daemon attack surface。

```shell
$ sudo groupadd docker

$ sudo usermod -aG docker $USER

```
退出再重进，确保该用户有正确的权限。

校验生效，通过运行docker命令不带sudo：
```shell
$ docker run hello-world
```
如果失败会有以下类似的信息：`Cannot connect to the Docker daemon. Is 'docker daemon' running on this host?`
确保DOCKER_HOST环境变量没有设置。如果有取消它。

## 调整内存和交换区计算
当用户运行docker时，他们可能在使用一个镜像时看见下面的信息：
`WARNING: Your kernel does not support cgroup swap limit. WARNING: Your kernel does not support swap limit capabilities. Limitation discarded.`

为了阻止这些信息，在你的系统中启用内存和交换区计算。这个操作会导致即便docker没有使用也有内存开销以及性能下降。内存开销大概是总内存的1%。性能降低了大约10%。

修改/etc/default/grub文件。vi或者vim命令都行，设置`GRUB_CMDLINE_LINUX`的值，如下：`GRUB_CMDLINE_LINUX="cgroup_enable=memory swapaccount=1"。`保存文件并关闭。`sudo update-grub` 更新启动项。重启你的系统。

## 启动UFW转发
当你运行docker时，在同一台主机上使用UFW(Uncomplicated Firewall) ，你需要额外的配置。docker使用桥接方式来管理容器的网络。默认情况下，UFW废弃所有的转发流量。因此，docker运行时UFW可以使用，你必须设置合适UFW的转发规则。

UFW默认配置规则拒绝了所有传入流量。如果你想要从另一个主机到达你的容器需要允许连接docker的端口。docker的默认端口是2376如果TLS启用，如果没有启动则是2375,会话是不加密的。默认情况，docker运行在没有TLS启动的情况下。

为了配置UFW并且允许进入的连接docker端口：

1.检查UFW是否安装并启用：`$ sudo ufw status`

2.打开/etc/default/ufw文件并编辑：`$ sudo nano /etc/default/ufw`

3.设置`DEFAULT_FORWARD_POLICY：DEFAULT_FORWARD_POLICY="ACCEPT"`

4.保存退出并重启使用新的设置：`$ sudo ufw reload`

5.允许所有的连接到docker端口：`$ sudo ufw allow 2375/tcp`

# 为使用docker配置DNS服务器

系统运行桌面的Ubuntu或者Ubuntu衍生产品通常使用127.0.0.1作为默认的nameserver文件/etc/resolv.conf文件中。NetworkManager也通常设置dnsmasq nameserver 127.0.0.1在/etc/resolv.conf。

当在桌面机器运行容器，使用这些配置时，docker的使用者会看见这些警告：
`WARNING: Local (127.0.0.1) DNS resolver found in resolv.conf and containers can't use it. Using default external servers : [8.8.8.8 8.8.4.4]`

这个警告发生是因为docker容器不能使用本地DNS命名服务器。此外docker默认使用一个额外的nameserver。

为了避免这个警告，你可以在使用docker容器的时候指定一个DNS服务器。或者你可以禁用dnsmasq在NetworkManager中。但是，禁用会导致DNS协议在某些网络中变慢。

下面的说明描述了如何在Ubuntu14.0或以下版本配置docker守护进程。Ubuntu15.04及之上的使用systemd用于启动项和服务管理。指导通过使用systemd来配置和控制一个守护进程。

设置指定的DNS服务：

打开/etc/default/docker文件并编辑：sudo nano /etc/default/docker，添加配置项：DOCKER_OPTS="--dns 8.8.8.8"。将8.8.8.8用一个本地的DNS服务例如192.168.1.1替换。你也可以配置多个DNS服务器。用空格隔开它们，如：--dns 8.8.8.8 --dns 192.168.1.1。警告：当你在笔记本连接了不同网络的情况时做这些操作，确保选择一个公用的DNS服务器。保存文件并退出，重启docker守护进程：sudo service docker restart。

或者另一个选择，禁用dnsmasq在网络管理器中，这可能导致你的网速变慢：

打开/etc/NetworkManager/NetworkManager.conf文件，编辑它：sudo nano /etc/NetworkManager/NetworkManager.conf。找到行dns=dnsmasq，注释掉。保存关闭文件，重启网络管理器和docker.sudo restart network-manager  sudo restart docker。

## 配置docker引导启动

Ubuntu15.04之后使用systemd作为引导启动和服务管理，14.10及以下版本是upstart。15.04以上，需要配置docker守护进程boot启动，运行命令：sudo systemctl enable docker

14.10及以下版本安装方法会自动配置upstart来启动docke daemon在boot。

## 升级卸载docker
```shell
#升级：
sudo apt-get upgrade docker-engine

##卸载：
$ sudo apt-get purge docker-engine

##卸载及依赖：
$ sudo apt-get autoremove --purge docker-engine

#上述命令不会卸载images，containers，volumes或者用户自己创建的配置文件。你如果想删除这些东西，执行下面的命令：

$ rm -rf /var/lib/docker

#安装最简单的方法是：

$ sudo apt-get update　　 
$ sudo apt-get install docker

```