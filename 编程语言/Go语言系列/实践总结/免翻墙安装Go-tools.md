# 免翻墙安装Go tools
```shell
#!/usr/bin/env bash
# 免翻墙安装Go tools
branch="release-branch.go1.9"
mkdir -p $GOPATH/src/golang.org/x

git clone -b $branch git@github.com:golang/tools.git $GOPATH/src/golang.org/x/tools
git clone git@github.com:golang/net.git $GOPATH/src/golang.org/x/net

cd $GOPATH
go install golang.org/x/tools/cmd/goimports
go install golang.org/x/tools/cmd/vet
go install golang.org/x/tools/cmd/oracle
go install golang.org/x/tools/cmd/godoc
```

注意有没有这个包