# Ethereum JSON-RPC
Ethereum RPC APIs分为两类分别是：
1. Standard APIs
2. Geth Specific APIs

## Ethereum RPC
1. HTTP-RPC: 手动启动, 相关配置和启动参数
    - rpc
    - rpcaddr
    - rpcport
    - rpcapi
    - rpccorsdomain
2. WS-RPC： 手动启动, 相关配置和启动参数
    - ws  
    - wsaddr  
    - wsport  
    - wsapi  
    - wsorigins   
3. IPC-RPC： 手动启动, 相关配置和启动参数
    - ipcdisable  
    - ipcapi  
    - ipcpath
注：出于安全考虑，接口默认开放API子集，通过相应参数可以进行配置。

## HTTP-RPC 调用实例
1. 通过docker运行以太坊私有链节点并配置参数
```shell
$ docker run -it -d --name eth-noderpc -p 30303:30303 -p 8545:8545 --net eth-network --ip  172.25.0.10 ethereum:3.0 --rpc --rpcaddr "0.0.0.0" --rpcapi "admin,debug,eth, miner,net,personal,shh,txpool,web3" --nodiscover --networkid 15
```
参数解释：
```shell
--rpc 启用HTTP-RPC服务

--rpcaddr 服务监听接口，默认“localhost”

--rpcapi 配置可使用的api接口，默认“eth, net, web3”
```


2.  调用RPC接口

    - 查看账户信息
```shell
$ curl -X POST -H "Content-Type":application/json --data '{"jsonrpc":"2.0", "method":"eth_accounts","params":[],"id":67}' ip:port
```
    -  查看账户余额
```shell
$ curl -X POST -H "Content-Type":application/json --data '{"jsonrpc":"2.0", "method":"eth_getBalance","params":["0xde1e758511a7c67e7db93d1c23c1060a21db4615","latest"],"id":67}' ip:port  
```
    - 查看区块高度
```shell
$ curl -X POST -H "Content-Type":application/json --data '{"jsonrpc":"2.0", "method":"eth_blockNumber","params":[],"id":67}' ip:port
```

    -  解锁账户
```shell
$ curl -X POST -H "Content-Type":application/json --data '{"jsonrpc":"2.0", "method":"personal_unlockAccount","params":["0xde1e758511a7c67e7db93d1c23c1060a21db4615","password",300],"id":67}' ip:port
```

    - 发送交易
```shell
$ curl -X POST -H "Content-Type":application/json --data '{"jsonrpc":"2.0","method":"eth_sendTransaction","params": [{                                                                                 
  "from": "0xde1e758511a7c67e7db93d1c23c1060a21db4615",  
  "to": "0xd64a66c28a6ae5150af5e7c34696502793b91ae7",  
  "value": "0x1"  
}],  
"id":67}' ip:port
```  
    - 查看交易状态
```shell
$ curl -X POST -H "Content-Type":application/json --data '{"jsonrpc":"2.0", "method":"miner_start","params":[],"id":67}' ip:port
```

    - 开启挖矿
```shell
$ curl -X POST -H "Content-Type":application/json --data '{"jsonrpc":"2.0", "method":"miner_start","params":[],"id":67}' ip:port
```  
    - 结束挖矿
```shell
$ curl -X POST -H "Content-Type":application/json --data '{"jsonrpc":"2.0", "method":"miner_stop","params":[],"id":67}' ip:port
```

注意命令与官网的区别，如果去掉会产生格式不可解析的错误。

参考资料：

[https://github.com/ethereum/wiki/wiki/JSON-RPC](https://github.com/ethereum/wiki/wiki/JSON-RPC)

[https://github.com/ethereum/go-ethereum/wiki/Management-APIs](https://github.com/ethereum/go-ethereum/wiki/Management-APIs)

