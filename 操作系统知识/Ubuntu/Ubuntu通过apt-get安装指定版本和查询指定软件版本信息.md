# Ubuntu通过apt-get安装指定版本和查询指定软件版本信息
说明：在Linux用这个查询并不能完全的把所有版本都列举出来，因为每个版本都与系统版本和CPU架构有关，比如一个软件支持Ubuntu系统的16.04的CPU架构为amd64的版本只有1.0和1.2，其余都不支持，所以列举时就只有两款。

1. 通过apt-get安装制定版本软件
```shell
$ apt-get install \<\<package-name\>\>-\<\<version\>\>
```
2. 通过网站搜索：`https://packages.ubuntu.com/`
3. 列出所有来源的版本 `apt-cache madison <<package name>>` madison是一个apt-cache子命令，可以通过man apt-cache查询更多用法。
4. 列出所有来源的版本。信息会比`madison`详细. `apt-cache policy <<package name>>`  policy是一个apt-cache子命令，可以通过man apt-cache查询更多用法。
5. `apt-cache showpkg <<package name>>` 
6. `apt-get install -s <<package-name>>` 这个命令只是模拟安装时会安装哪些软件列表，但不会例举出每个软件有多少个版本 
7. `aptitude versions <<package name>>` 
8. `apt-show-versions -a <<package name>>` 列举出所有版本，且能查看是否已经安装。还可以通过apt-show-versions -u <<package name>>来查询是否有升级版本。
9. `whohas -d Debian,Ubuntu <<package name>> | tr -s ' ' '\t' | cut -f 1-3 | column -t` 
10. `apt-cache show <<package name>>` 查询指定包的详情，不管是否已经安装。
11. `dpkg -l <<package name>>` 效果和上面基本一致，但是结果是列表详情展示，会提示是否已经删除了之后还有依赖包没有删除等。
12. `dpkg -s <<package name>>` 必须是安装的包才能显示详情。
13. `dpkg-query -s <<package name>>` 同上，效果一致。