# Java线程池
合理使用线程池的优势：
1. 降低资源消耗。 通过重复利用已创建的线程降低线程创建和销毁造成的消耗。
2. 提高响应速度。 当任务到达时，任务可以不需要等到线程创建就能立即执行。
3. 提供线程的可管理性。 线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一分配、调优和监控。

## 线程池的实现原理
当向线程池提交一个任务后，线程池的处理流程如下：
1. 线程池判断核心线程池里的线程是否都在执行任务。如果不是，则创建一个新的工作做线程来执行任务。如果核心线程池里的线程都在执行任务，则进入下一个流程。
2. 线程池判断工作队列是否已满。如果工作队列没有满，则将新提交的任务存储在这个工作队列里。如果工作队列已满，则进入下一个流程。
3. 线程池判断线程池的线程是否都处于工作状态。如果没有，则创建一个新的工作线程来执行任务。如果已经满了，则交给饱和策略来处理这个任务。

线程池的主要处理流程：

![executor_pool_workflow](./images/executor_pool_workflow.png)


`ThreadPoolExecutor`执行`execute()`方法的示意图：

![threadPoolExecutor_workflow](./images/threadPoolExecutor_workflow.png)

`ThreadPoolExecutor`执行`execute()`方法分为下面的四种情况：
1. 如果当前运行的线程少于`corePoolSize`，则创建新线程来执行任务（注意，执行这一步骤需要获取全局锁）
2. 如果运行的线程等于或多于`corePoolSize`，则将任务加入`BlockingQueue`
3. 如果无法将任务加入`BlockingQueue`（队列已满），则创建新的线程来处理任务（注意，执行这一步需要获取全局锁）
4. 如果创建新线程将使当前运行的线程超出`maximumPoolSize`,任务将会被拒绝，并调用`RejectedExceptionHandler.rejectedExecution()`方法。

`ThreadPoolExecutor`采取上述步骤的总体设计思路是为了执行`execute()`方法时，尽可能避免获取全局锁。在`ThreadPoolExecutor`完成预热之后（当前运行线程大于等于corePoolSize)，几乎所有的`execute()`方法调用都是执行步骤2，而步骤2不需要获取全局锁。

`execute`源码：

```java
public void execute(Runnable command) {
	if(comman == null) throw new NullPointerException();
	//如果线程数量少于基本线程数，则创建线程并执行当前任务
	if(poolSize >= corePoolSize || !addIfUnderCorePoolSize(command)) {
		//如果线程数大于基本线程数或线程创建失败，则将当前任务放到工作队列中
		if(runState == RUNNING && workQueue.offer(command)) {
			if(runState != RUNNING || poolSize == 0) {
				ensureQueuedTaskHandled(command);
			}
		}
		//如果线程池不处于运行中或任务无法放入队列，且当前线程数量小于最大允许的线程数量，则创建一个线程执行任务
		else if (!addIfUnderMaximumPoolSize(command)) {
			//抛出RejectedExecutionException异常
			reject(command); //is shutdown or saturated
		}
	}
}
```

**工作线程** 线程池创建线程时，会将线程封装成工作线程Worker，Worker在执行完任务后，还会循环获取工作队列里的任务来执行。

```java
public void run() {
	try{
		Runnable task = firstTask;
		firstTask = null;
		while(task != null || (task = getTask()) != null) {
			runTask(task);
			task = null;
		}
	}finally {
		workerDone(this);
	}
}

```
`ThreadPoolExecutor`中线程执行任务的示意图：

![threadPoolExecutor_workflow_1](./images/threadPoolExecutor_workflow_1.png)

线程池中的线程执行任务分两种情况；
1. 在`execute()`方法创建一个线程时，会让这个线程执行当前任务。
2. 这个线程执行完上任务后，会反复从`BlockingQueue`获取任务来执行。
