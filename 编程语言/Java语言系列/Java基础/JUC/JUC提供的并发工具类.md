# JUC提供的并发工具类
JDK并发包中提供了几个非常有用的并发工具类。 `CountDownLatch, CyclicBarrier`和`Semaphore`工具类提供了一种并发流程控制的手段，`Exchanger`工具类则提供了在线程间交换数据的一种首段。

## 等待多线程完成 CountDownLatch
`CountDownLatch`允许一个或多个线程等待其他线程完成操作。`CountDownLatch`的构造函数接收一个int类型的参数作为计数器，如果想让程序等待N个点完成，这里需要传入N。

当程序调用`CountDownLatch`的`countDown`方法时，N就会减1，`CountDownLatch`的`await`方法会阻塞当前线程，直到N变为0。由于`countDown`方法可以用在任何地方，所以这里说的N个点，可以是N个线程，也可以是一个线程里的N个执行步骤。用在N个线程里时，只需要把这个`CountDownLatch`的引用传递到线程里即可。

注意： 计数器必须大于0，只是等于0的时候，计数器就是零，调用await方法时不会阻塞当前线程。`CountDownLatch`不可能初始化或者修改`CountDownLatch`对象的内部计数器值。一个线程调用`countDown`方法`happen-before`，另一个线程调用`await`方法。

注意`CountLatchDown`提供的功能和join方法是极其相似的。`Thread`类的`join`方法用于让当前执行线程等待`join`线程执行结束。其实现原理是不停检查`join`线程是否存活，如果`join`线程存活则让当前线程永远等待。其中`wait(0)`表示永远等待下去。 直到`join`线程终止后，线程的`this.notifyAll()`方法才会被调用，调用`notifyAll()`方法在JVM中实现，在JDK中看不到。

## 同步屏障 CyclicBarrier
`CyclicBarrier`的字面意思是可循环使用`Cyclic`的屏障`Barrier`。它要做的事情是，让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会消失，所有被屏障拦截的线程才会继续运行。

`CyclicBarrier`默认的构造方法是`CyclicBarrier(int parties)`，其参数表示屏障拦截的线程数量，每个线程调用`await`方法告诉`CyclicBarrier`其已经到达了屏障，然后当前线程被阻塞。`CyclicBarrier`还提供了一个更高级的构造函数`CyclicBarrier(int parties, Runnable barrierAction)`，用于在线程到达屏障时，优先执行`barrierAction`,方便处理更复杂的业务场景。


## CyclicBarrier和CountDownLatch的区别
`CountDownLatch`的计数器只能使用一次，而`CyclicBarrier`的计数器可以使用`reset()`方法重置。所以`CyclicBarrier`能处理更为复杂的业务场景。例如,如果计算发生错误，可以重置计数器，并让线程重新执行一次。 `CyclicBarrier`还提供了其他有用的方法，比如`getNumberWaiting`方法可以获得`CyclicBarrier`阻塞的线程数量。`isBroken()`方法用来了解阻塞的线程是否被中断。


## 控制并发线程数的Semaphore
`Semaphore`（信号量）是用来控制同时访问特定资源的线程数量，它通过协调各个线程，以保证合理的使用公共资源。


## 线程间交换数据的Exchanger
`Exchanger`（交换者）是一个用于线程间协作的工具类。`Exchanger`用于进行线程间的数据交换。它提供一个同步点，在这个同步点，两个线程可以交换彼此的数据。这两个线程通过`exchange`方法交换数据，如果第一个线程先执行`exchange()`方法，它会一直等待第二个线程也执行`exchange`方法，当两个线程都达到同步点时，这两个线程就可以交换数据，将本线程生产出来的数据传递给对方。

**Exchanger可以用于遗传算法** 遗传算法里需要选出两个作为交配对象，这时候会交换两个人的数据，并使用交叉规则得出两个交配结果。 **Exchanger也可以用于校对工作**，比如我们需要将纸质银行流水通过人工的方式录入成电子银行流水，为了避免错误，采用AB岗两人进行录入，录入Excel之后，系统需要加载这两个Excel，并对两个Excel数据进行校对，看看录入结果是否一致。 

```java
package four;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExchangerTest {
	private static final Exchanger<String> exgr = new Exchanger<String>();
	
	private static ExecutorService threadPool = Executors.newFixedThreadPool(2);
	
	public static void main(String[] args) {
		threadPool.execute(new Runnable(){
			@Override public void run() {
				try{
					String A = "银行流水A";
					exgr.exchange(A);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		});
		
		threadPool.execute(new Runnable(){
			@Override public void run() {
				try{
					String B = "银行流水B";
					String A = exgr.exchange(B);
					System.out.println(A.equals(B));
					System.out.println("A = " + A);
					System.out.println("B = " + B);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		});
		threadPool.shutdown();
	}
}
```