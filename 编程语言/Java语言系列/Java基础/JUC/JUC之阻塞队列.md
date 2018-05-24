# JUC之阻塞队列
主要介绍了Java 8中的7种阻塞队列，最后分析了一种阻塞队列的具体实现原理。

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
`ArrayBlockingQueue`是一个用`数组`实现的有序阻塞队列。该队列按照先进先出（FIFO）的原则对元素进行排序。

默认情况下不保证线程公平的访问队列，所谓公平的访问队列是指阻塞的线程可以按照阻塞的先后顺序访问队列，即先阻塞线程先访问队列。非公平性是指对先等待的线程是非公平的，当队列可用时，阻塞的线程都可以争夺访问队列的资格，有可能先阻塞的线程最后才访问队列。为了保证公平性，通常会降低`ArrayBlockingQueue`的吞吐量。例如，下列创建一个的阻塞队列：

```java
ArrayBlockingQueue fiarQueue = new ArrayBlockingQueue(1000, true);
```

访问者的公平性是使用可重入锁实现的，代码如下：

```java
public ArrayBlockQueue(int capacity, boolean fair) {
    if(capacity < 0){
        throw new IllegalArgumentException();
    }
    
    this.items = new Object[capacity];
    lock = new ReentrantLock(fair);
    notEmpty = lock.newCondition();
    notFull = lock.newCondition();
}
```

## LinkedBlockingQueue
`LinkedBlockingQueue`是一个用链表实现的有界阻塞队列。此队列的默认和最大长度为`Integer.MAX_VALUE`。该队列按照先进先出的原则对元素进行排序。

## PriorityBlockingQueue
`PriorityBlockingQueue`是一个支持优先级的无界阻塞队列。默认情况下元素采取自然顺序升序排列。也可以自定义类实现`compareTo()`方法来指定排序规则，或者初始化`PriorityBlockingQueue`时，指定一个排序器`Comparator`对元素进行排序。需要注意的是`PriorityBlockingQueue`不能保证同优先级元素的顺序。

## DelayQueue
`DelayQueue`是一个支持延时获取元素的无界阻塞队列。队列使用`PriorityQueue`实现。队列中的元素必须实现`Delayed`接口，在创建元素时可以指定多久才能从队列中获取当前元素。只有在延迟期满时才能从队列中提取元素。

`DelayQueue`可是适用以下场景：
- 缓存系统的设计： 可以使用`DelayQueue`保存缓存元素的有效期，使用一个线程循环查询`DelayQueue`，一旦能从`DelayQueue`中获取元素时，表示缓存有效期到了。
- 定时任务调度： 使用`DelayQueue`保存当天将会执行的任务和执行时间，一旦从`DelayQueue`中获取到任务就开始执行，比如`TimerQueue`就是使用`DelayQueue`实现的。

1. 如何实现`DelayQueue`接口

`DelayQueue`队列的元素必须实现`DelayQueue`接口。可以参考`ScheduledThreadPoolExecutor`里`ScheduledFutureTask`类的实现，一共有三步：

- 第一步： 在创建对象的时候，初始化基本数据。使用`time`记录当前对象延迟到什么时候可以使用，使用`sequenceNumber`来标识元素在队列中的先后顺序。代码如下：

```java
private static final AtomicLong sequencer = new AtomicLong(0);

ScheduledFutureTask(Runnable r, V result, long ns, long period){
   super(r, result);
   this.time = ns;
   this.period = period;
   this.sequenceNumber = sequencer.getAndIncrement();
}
```
- 第二步： 实现`getDelay`方法，该方法返回当前元素还需要延时多长时间，单位为纳秒，代码如下：

```java
public long getDelay(TimeUnit unit){
    return unit.convert(time - now(), TimeUnit.NANOSECONDS);
}
```

- 第三步：实现`compareTo`方法来指定元素的顺序。例如，将延时时间最长的放在队列的末尾：

```java
public int compareTo(Delayed other) {
    if(other == this) //compare zero ONLY if same object
        return 0;
    if(other instanceof ScheduledFutureTask){
        ScheduledFutureTask<?> x = (ScheduledFutureTask<?>other;
        long diff = time - x.time;
        if(diff < 0){
            return -1;
        }else if(diff > 0){
            return 1;
        }else if(sequenceNumber < x.sequenceNumber) {
            return -1;
        }else {
            return 1;
        }
    }
    long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
    return (d == 0) ? 0 ((d < 0) ? -1 : 1);
}
```

2. 如何实现延时阻塞队列
延时阻塞队列的实现很简单， 当消费者从队列里获取元素时，如果元素没有达到延时时间，就阻塞当前线程。

```java
long delay = first.getDelay(TimeUnit.NANOSECONDS);
if(delay <= 0) {
    return q.poll();
}else if(leader != null) {
    available.await();
}else{
    Thread thisThread = Thread.currentThread();

    leader = thisThread;

    try {
        available.awaitNanos(delay);
    }finally{
        if(leader == thisThread){
            leader = null;
        }
    }
}
```
代码中的变量`leader`是一个等待获取队列头部元素的线程。如果`lader`不等于空，表示已经有线程在等待获取队列的头元素。所以使用`await()`方法让当前线程等待信号。如果`leader`等于空，则把当前线程设置成`leader`,并使用`awaitNanos()`方法让当前线程等待接收信号或等待`delay`时间。


## SynchronousQueue
`SynchronousQueue`是一个不存储元素的阻塞队列。每个`put`操作必须等待一个`take`操作，否则不能继续添加元素。

它支持公平访问队列。默认情况下线程采用非公平性策略访问队列。使用下列构造方法可以创建公平访问的`SynchronousQueue`,如果设置为`true`，则等待的线程会采用先进先出的顺序访问。

```java
public SynchronousQueue(boolean fait) {
    transfer = fair ? new TransferQueue() : new TransferStack();
}
```
`SynchronousQueue`可以看成传球手角色，负责把生产者线程处理的数据直接传递给消费者线程。队列本身并不存储任何元素，非常适合传递性场景。`SynchronousQueue`的吞吐量高于`LinkedBlockingQueue`和`ArrayBlockQueue`。

## LinkedTransferQueue
`LinkedTransferQueue`是一个由链表结构组成的无界阻塞`TransferQueue`队列。相对于其他阻塞队列，`LinkedTransferQueue`多了`tranfer`和`tryTranfer`方法。

1. transfer方法： 如果当前消费者正在等待接收元素（消费者使用`take()`方法或带时间限制的`poll()`方法时），`transfer()`方法可以把生产者传入的元素立刻transfer（传输）给消费者。如果没有消费者在等待接收元素，`transfer`方法会将元素存放在队列的`tail`节点，并等到该元素被消费者消费了才返回。`transfer`方法的关键代码如下：

```java
Node pred = tryAppend(s, haveData);
return awaitMatch(s, pred, e, (how == TIMED), nanos);
```
第一行代码试图将存放在当前元素的s节点作为tail节点。第二行代码让CPU自旋等带消费者消费元素。因为自旋会消耗CPU，所以自旋一定的次数后使用`Thread.yield()`方法来暂停当前正在执行的线程，并执行其他线程。

2. tryTransfer方法: 用来试探生产者传入的元素是否能直接传给消费者。如果没有消费者等待接收元素，则返回false。和transfer方法的区别是tryTransfer方法无论消费者是否接收，方法立即返回，而transfer方法是必须等到消费者消费了才返回。 对于带有时间限制的tryTransfer(E e, long timeout, TimeUnit unit)方法，试图把生产者传入的元素直接传给消费者，但是如果没有消费者消费该元素则等待指定的时间再返回。如果超时还没有消费元素，则返回false。如果在超时时间内消费了元素，则返回true。

## LinkedBlockingDeque
`LinkedBlockingDeque`是一个由链表结构组成的双向阻塞队列。所谓双向队列指的是可以从队列两端插入和移除元素。双向队列因为多了一个操作队列的入口，在多线程同时入队时，也就减少了一半的竞争。相比于其他的阻塞队列，`LinkedBlockingDeque`多了`addFirst`,`addLast`，`offerFirst`，`offerLast`,`peekLast`等方法，以`First`单词结尾的方法，表示插入、获取(peek)或移除双端队列的第一个元素。以`Last`单词结尾的方法，表示插入、获取或移除双端队列的最最后一个元素。另外插入方法`add`等同于`addLast`,移除方法`remove`等效于`removeFirst`。但是`take`方法却等同于`takeFirst`。在初始化`LinkedBlockingDeque`时可以设置容量防止其过度膨胀。另外，双向阻塞队列可以运用在“工作窃取”模式中。

## 阻塞队列的实现原理
这里主要阐述了生产者和消费者进行高效通信的机理—— **通知模式** 实现。所谓通知模式，就是当生产者想满队列中添加元素时，生产者会被阻塞；当消费者消费了一个元素后，会通知生产者当前队列可用。以下是JDK中`ArrayBlockingQueue`的源码实现。它具体使用了`Condition`实现。

```java
private final Condition notFull;
private final Condition notEmpty;

public ArrayBlockingQueue(int capacity, boolean fair) {
    //省略其他代码
    notEmpty = lock.newCondition();
    notFull = lock.newCondition();
}

public void put(E e) throws InterruptedException {
    checkNotNull(e);
    final ReentrantLock lock = this.lock();
    lock.lockInterruptibly();
    try{
	while(count == items.length) {
            notFull.await();//阻塞
        }
        insert(e); //执行插入
    }finally{
      lock.unlock();//释放锁
    }
     
}

private void insert(E x) {
    items[putIndex] = x;
    putIndex = inc(putIndex);
    ++count;
    notEmpty.signal();//通知
}

public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock(); //获取锁
    lock.lockInterruptibly();
    try{
	while(count == 0) {
           notEmpty.await(); //阻塞
        }
        reutrn extract(); //取数据
    }finally{
	lock.unlock(); //释放锁
    }
}

```
当向队列里插入一个元素时，如果队列不可用，那么阻塞生产者主要通过LockSupport.park(this)实现。

```java
public final void await() throws InterrruptedException {
    if(Thread.interrupted()){
        throw new InterruptedException();
    }
    Node node = addConditionWaiter();
    int saveState = fullyRelease(node);
    int interruptMode = 0;
    while(!isOnSyncQueue(node)) {
        LockSupport.park(this);
        if((interruptMode = checkInterruptWhileWaiting(node)) != 0)
            break;
    }
    if(acquireQueued(node, savedState) && interruptMode != THROW_IE)
         interruptMode = REINTERRUPT;
    if(node.nextWaiter != null) //clean up if canlled
         unlinkCancelledWaiters();
    if(interruptAfterWait(interruptMode)
         reportInterruptAfterWait(interruptMode);
}


public static void park(Object blocker) {
    Thread t= Thread.currentThread();
    setBlocker(t, blocker);
    unsafe.park(false, 0L);
    setBlocker(t, null);
}
```
首先调用 `setBlocker` 保存将要阻塞的线程，然后调用`unsafe.park()`阻塞当前线程。其中`unsafe.park()`是一个`native`方法：

```java
public native park(boolean isAbsolute, long time);
```

