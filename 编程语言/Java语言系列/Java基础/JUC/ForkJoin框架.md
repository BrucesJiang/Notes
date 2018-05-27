# Fork/Join框架
本文将会介绍Fork/Join框架的基本原理，算法，设计方式，实现和应用。

## 基本原理
Fork/Join框架是Java 7提供的一个用于并行执行任务的框架，是一个把较大任务分割成若干个小任务，最终汇总每个小任务结果后得到大任务结果的框架。

其原理如下图所示：

![ForkJoin](./images/ForkJoin.png)

## 工作窃取算法
工作窃取算法(work-stealing)指的是某个线程从其他线程任务列表中窃取任务来执行。为什么要使用工作窃取算法？假如我们有一个比较大的任务，可以把这个任务分解为若干个互不依赖的子任务，为了减少线程之间的竞争，把这些子任务分别放在不同的队列里，并为每个队列创建一个单独的线程来执行队列中的任务，线程和队列一一对应。例如A线程负责处理队列QA中的任务。但是有些线程会将自己队列中的任务先做完，而其他线程对应队列中还有任务等待处理。那么处理完任务的线程就会从其他任务队列中窃取一个任务来处理。而在这时它们会访问同一个队列，所以为了减少窃取任务线程和被窃取任务线程之间的竞争，通常会使用双端队列，被窃取任务线程永远从双端队列的头部拿任务执行，而窃取任务的线程永远从双端队列的尾部拿任务执行。

工作窃取算法的优点是充分利用线程进行并行计算，并减少了线程间的竞争，其缺点是在某些情况下还是存在竞争，比如双端队列里只有一个任务时。并且消耗了更多的系统资源，比如创建多个线程和多个双端队列。


## Fork/Join框架设计
我们叙述Fork/Join框架原理时将其分为两个部分：分割和结果合并。

1. 分割任务 Fork类将大任务分割成子任务，有可能子任务还是很大，因此可能需要递归分割
2. 执行子任务并合并结果 分割的子任务分别放在双端队列中，然后启动多个线程分别从双端队列中获取任务执行。子任务执行结果统一放在一个结果队列中，启动一个线程从结果队列中获取数据并合并为最终结果。

Fork/Join框架提供两个基本类完成上述两件事情：

1. ForkJoinTask: 使用ForkJoin框架，必须首先创建一个ForkJoin任务。它提供在任务中执行fork()和join()操作的机制。通常情况下，我们不需要直接继承ForkJoinTask类，只需要继承它的子类，Fork/Join框架提供了以下两个子类：
    - RecursiveAction: 用于没有返回结果的任务
    - RecursiveTask: 用于有返回结果的任务
2. ForkJoinPool: ForkJoinTask需要通过ForkJoinPool来执行。

任务分解后产生的子任务被添加到当前工作线程所维护的双端队列中，进入队列的头部。当一个工作线程的队列里暂时没有任务时，它会随机从其他工作线程的队列尾部获取要给任务处理。

## 使用Fork/Join框架
例如，对超过1000万个元素的数组进行排序。

```java
class SumTask extends RecursiveTask<Long> {

    static final int THRESHOLD = 100;
    long[] array;
    int start;
    int end;

    SumTask(long[] array, int start, int end) {
    		this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            // 如果任务足够小,直接计算:
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            		e.printStackTrace();
            }
            System.out.println(String.format("compute %d~%d = %d", start, end, sum));
            return sum;
        }
        // 任务太大,一分为二:
        int middle = (end + start) / 2;
        System.out.println(String.format("split %d~%d ==> %d~%d, %d~%d", start, end, start, middle, middle, end));
        SumTask subtask1 = new SumTask(this.array, start, middle);
        SumTask subtask2 = new SumTask(this.array, middle, end);
        invokeAll(subtask1, subtask2);
        Long subresult1 = subtask1.join();
        Long subresult2 = subtask2.join();
        Long result = subresult1 + subresult2;
        System.out.println("result = " + subresult1 + " + " + subresult2 + " ==> " + result);
        return result;
    }
    
    
    public static void main(String[] args) throws Exception {
    	// 创建随机数组成的数组:
    	long[] array = new long[400];
    	fillRandom(array);
    	// fork/join task:
    	ForkJoinPool fjp = new ForkJoinPool(4); // 最大并发数4
    	ForkJoinTask<Long> task = new SumTask(array, 0, array.length);
    	long startTime = System.currentTimeMillis();
    	Long result = fjp.invoke(task);
    	long endTime = System.currentTimeMillis();
    	System.out.println("Fork/join sum: " + result + " in " + (endTime - startTime) + " ms.");
	}
}
```
关键代码是fjp.invoke(task)来提交一个Fork/Join任务并发执行，然后获得异步执行的结果。


ForkJoin摘要包含以下四个类：

1. ForkJoinPool
    - 用于执行Task，或生成新的ForkJoinWorkerThread
    - 执行ForkJoinWorkerThread间的work-stealing逻辑
2. ForkJoinTask
    - 执行具体的叉分逻辑
    - 声明以同步/异步方式执行
3. ForkJoinWorkerThread
    - 是ForkJoinPool内的work thread, 执行ForkJoinTask
    - 内部有ForkJoinPoll.WorkQueue来保存要执行的ForkJoinTask
4. ForkJoinPool.WorkQueue
    - 保存要执行的ForkJoinTask

## Fork/Join框架的异常处理
`ForkJoinTask`在执行时可能会抛出异常，但是我们没有办法在主线程里直接捕获，所以`ForkJoinTask`提供了`isCompletedAbnormally()`方法来检测任务是否已经抛出异常或被取消了。我们可以通过`ForkJoinTask`的`getException()`来获取异常：

```java
if(task.isCompletedAbnormally()) {
		System.out.println(task.getException());
}

```
`getException()`方法返回`Throwable`对象，如果任务被取消了则返回`CancellationException`。如果任务没有完成或没有抛出异常，则返回`null`。

## Fork/Join框架的实现原理
作为一个轻量级的并发执行框架，Fork/Join的实现事实上是由3个角色构成： **任务队列（WorkQueue），工作者线程（ForkJoinWorkerThread），待执行任务（ForkJoinTask）**。任务队列负责存放程序提交给执行者（ForkJoinPool）的任务，工作者线程负责执行这些任务，然后该框架通过执行者的接口对外提供服务。

对于这些角色如何协调完成并发任务，建议参看Java 8源代码，逻辑更加清晰。这里重点说明Fork/Join框架的两个机制： **双端队列和抢断/闲置**，然后通过源码阐述 `任务提交（submit）`，`任务执行（fork）`和`任务连接（join）`。

### 双端队列
我们使用了双端队列,它允许元素从两端弹出,且限定了插入和删除操作必须在队列的两端进行。它是高效的,创建-push、发布-pop和弹出-take任务能够在程序调用中的开销更低廉,使得程序员能够构建更小粒度的任务,最终也能更好的利用并行所带来的益处。


双端队列的基本结构采用了很常规的一个结构——数组(尽管是可变长的)来表示每个队列,同时附带两个索引:top索引和base索引。top索引类似于数组中的栈指针,通过push和pop操作来改变;而base索引只能通过take操作来改变。把base索引定义为volitile变量可以保证当队列中元素不止一个时,take操作可以在不加锁的情况下进行。

因为执行者ForkJoinPool的操作都是无缝的绑定到双端队列的细节之中,我们把这个数据结构直接放在执行者ForkJoinPool类之中(静态内部类),而不是作为一个单独的组件。


由于每个push和pop操作都需要获取锁以保证同步前后的一致性,这将成为性能瓶颈。所以,对于双端队列而言,我们有以下的调整策略:

- push和pop操作仅可以被工作线程的拥有者所调用; 
- 对task的操作会因为窃取任务线程在某一个时间点对take的操作而采取加锁限制。这样,控制冲突将被降低为两个部分(工作线程和窃取任务线程)同步的层次; 
- pop和take操作只有在双端队列为空的时候才会发生冲突,否则的话,双端队列会保证他们在不同的数组元素上面进行操作。

### 工作窃取 
这里主要的问题在于,当一个工作线程既没有本地任务也无法从别的工作线程中窃取任务时怎么办。经管在多核处理器上,可以依赖于硬件的忙等待、自旋循环的去尝试窃取一个任务,但是即使这样,尝试窃取任务还是会增加线程竞争,甚至会导致那些不是闲置的工作线程降低效率(锁协议的关系)。


Java中并没有十分健壮的机制来保证这种情况是少发生的,但是在实际场景中它往往也是可以让人接受的。一个窃取失败的线程在尝试另外的窃取之前会降低自己的优先级,在尝试窃取期间执行Thread#yeild()方法,将自己的状态在线程池中设置为不活跃的,他们会一直阻塞直到有新的任务进来(即线程强制睡眠)。其他情况下,在进行一定的自旋次数之后,线程将进入休眠阶段,他们会休眠而不是放弃抢断。强化的休眠机制会给人造成一种需要花费很长时间去划分任务的假象。但是这似乎是最好的也是通用的折中方案。框架的未来版本也许会支持额外的控制方法,以便于让程序员在感觉性能受到影响时可以重写默认的实现。

### 提交任务 
当你实例化一个执行者ForkJoinPool时,通常情况下它会创建一个线程数等于计算机处理器数的池(通过Runtime.availableProcessors()方法获得)①。当ForkJoinPool对象被创建时,这些线程被创建并且在池中等待,直到有任务到达让它们执行。

一旦ForkJoinTask被启动,就会启动其子任务并等待它们执行完成。执行者ForkJoinPool负责将任务赋予线程池中处于等待任务状态的另一线程。线程池中的活动线程会尝试执行其他任务所创建的子任务。ForkJoinPool尝试在任何时候都维持与可用的处理器数目一样数目的活动线程数。


你可以把 Fork/Join 模式看作并行版本的 Divide and Conquer 策略,仅仅关注如何划分任务和组合中间结果,将剩下的事情丢给 Fork/Join 框架。分割的子任务分别放在双端队列里,然后几个启动线程分别从双端队列里获取任务执行。子任务执行完的结果都统一放在一个队列里,启动一个线程从队列里拿数据,然后合并这些数据。


### 基本调度策略:

- 每一个工作线程维护自己的调度队列中的可运行任务。队列以双端队列的形式被维护(注:deques通常读作“decks”),不仅支持后进先出——LIFO的push和pop操作,还支持先进先出——FIFO的take操作。 
- 对于一个给定的工作线程来说,任务所产生的子任务将会被放入到工作者自己的双端队列中。工作线程使用后进先出——LIFO(最早的优先)的顺序,通过弹出任务来处理队列中的任务。 
- 当一个工作线程的本地没有任务去运行的时候,它将使用先进先出——FIFO的规则尝试随机的从别的工作线程中拿(“偷窃”)一个任务去运行。 
- 当一个工作线程触及了join操作,如果可能的话它将处理其他任务,直到目标任务被告知已经结束(通过isDone方法)。所有的任务都会无阻塞的完成。 
- 当一个工作线程无法再从其他线程中获取任务和失败处理的时候,它就会退出(通过yields, sleeps, 和/或者优先级调整,参考第3节)并经过一段时间之后再度尝试直到所有的工作线程都被告知他们都处于空闲的状态。在这种情况下,他们都会阻塞直到其他的任务再度被上层调用。

注: 对于工作线程数,通常情况下与平台所处的处理器数保持一致,但有的时候更少,用于处理其他相关的任务,或者有些情况下更多,来提升非计算密集型任务的性能。