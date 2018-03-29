# Byteball 简介
[Byteball白皮书](https://byteball.org/Byteball.pdf)
[Byteball官网](https://byteball.org/)

Byte与常见的虚拟货币不同，它没有采用区块链技术，而是采用DAG(Directed Acylic Graph)技术。信息分为Unit存储，各个Unit之间通过Hash连接在一起。

Byteball提供了一中新的保证数据不被篡改的方式，用户使用Byte来提交所需存储的内容。

通过白皮书可以了解到，Byteball克服了BTC的一些缺点：
- BTC存储信息量收到区块大小的限制，而Byteball不限制
- BTC受到信息传输速度的限制，需要等待至少六个区块才能确定交易是否有效，而Byteball可以很快确认一笔交易
- BTC的POW机制需要耗费大量资源，Byteball采用新的共识机制(通过对比存储单元unit之间序列顺序确定正确的链条)，大大减少消耗
- BTC中的交易确定性低，虽然不确定性指数逐级递减；Byteball中对交易是否成功有明确的定义
- BTC的价格不稳定，没有标准的资产；Byteball通过花费机制将Byte和现实货物的价格联系起来，形成一种负反馈的调节机制，Byte的价值将更为有效
- BTC可以被追踪，Byteball的隐私性更好
- Byteball可以更好的匹配现有金融资产