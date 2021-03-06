# B树和B+树的基本概念

## B树的概念
1970年Bayer等人提出的一种多路平衡查找树，称为B树。Knuth给出的定义如下：

一棵m阶B树或者为空，或者为满足下列性质的m叉树:
- 树中每个节点至多有m棵子树
- 根节点至少有两棵子树
- 除根节点外，每个节点至少有ceil(m/2)棵子树
- 所有叶子节点都在同一层上
- 所有节点都包含如下形式的数据：$(n, A_{0}, K_{1}, A_{1}, K_{2}, ..., K_{n}, A_{n})$
