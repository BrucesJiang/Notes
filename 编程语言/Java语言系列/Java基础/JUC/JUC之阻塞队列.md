# JUC之阻塞队列
主要介绍了Java 8中的7种阻塞队列。

## JUC中的阻塞队列
Java 8中提供了7中阻塞队列，如下：
- `ArrayBlockingQueue` 一个由数组结构构成的有界阻塞队列
- `LinkedBlockingQueue` 一个由链表结构构成的有界阻塞队列
- `PriorityBlockingQueue` 一个支持优先级排序的无界阻塞队列
- `DelayQueue` 一个使用优先级队列实现的无界阻塞队列
- `SynchronousQueue` 一个不存储元素的阻塞队列
- `LinkedTransferQueue` 一个由链表结构构成的无界阻塞队列
- `LinkedBlockingQueue` 一个由链表结构构成的双向阻塞队列

## ArrayBlockingQueue
