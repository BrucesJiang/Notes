# 在Ubuntu上安装Win上的字体

1. 拷贝已有Windows系统字体
2. 在Linux创建一个目录用来存放Windows字体,其中Windows系统的字体位于目录`C:/Windows/Fonts`目录，将字体拷贝到`/usr/share/fonts/WindowsFonts`目录下
```shell
$ sudo mkdir /usr/share/fonts/WindowsFonts
```
3. 更改字体权限
```shell
$ sudo chmod 755 /usr/share/fonts/WindowsFonts/*
```
4. 更新字体缓存

```shell
$ sudo fc-cache -fv
```

## 安装字体查看器
图形化界面管理字体

```shell
# 安装
$ sudo apt-get install font-manager

# 卸载
$ sudo apt-get install remove font-manager

```

