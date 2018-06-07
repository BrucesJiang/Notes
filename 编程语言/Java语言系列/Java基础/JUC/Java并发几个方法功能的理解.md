# Java并发几个方法功能的理解
本文主要介绍了Java并发编程过程中常用的一些功能类似的方法和它们的区别： `sleep()`, `join()`, `yield()`, `wait()`,`notify()`,`notifyAll()`, `run()/start()`

## sleep和yield
`sleep`方法是`Thread`类的静态方法，作用是使当前线程（也就是调用该方法的线程）暂停一段时间。这样其它线程可以获取CPU控制权。但是被暂停的线程并不会释放对象锁，也就是说如果多个线程同时竞争锁，当拥有锁控制权的线程使用`sleep()`暂停时，其他线程也无法继续执行，因为该线程在暂停期间不释放锁。另外，调用`sleep()`方法的线程不需要持有任何形式的锁，也不需要包裹在`Synchronized`同步块中。

`yield`方法是`Thread`类的实例方法，该方法与`sleep`方法类似，只是不能由用户指定暂停多长时间，并且`yield()`方法只能让同优先级的线程有执行的机会。


sleep 方法使当前运行中的线程睡眠一段时间，进入不可以运行状态，这段时间的长短是由程序设定的，yield方法使当前线程让出CPU占有权，但让出的时间是不可设定的。

yield()也不会释放锁标志。实际上，yield()方法对应了如下操作；先检测当前是否有相同优先级的线程处于同可运行状态，如有，则把CPU的占有权交给次线程，否则继续运行原来的线程，所以yield()方法称为“退让”，它把运行机会让给了同等级的其他线程。


sleep 方法允许较低优先级的线程获得运行机会，但yield（）方法执行时，当前线程仍处在可运行状态，所以不可能让出较低优先级的线程此时获取CPU占有权。在一个运行系统中，如果较高优先级的线程没有调用sleep方法，也没有受到I/O阻塞，那么较低优先级线程只能等待所有较高优先级的线程运行结束，方可有机会运行。

yield()只是使当前线程重新回到可执行状态，所有执行yield()的线程有可能在进入到可执行状态后马上又被执行，所以yield()方法只能使同优先级的线程有执行的机会.


## join
`Thread`类的实例方法，JDK的API里对`join()`方法的注释：

`
public final void join() throws InterruptedException Waits for this thread to die. Throws: InterruptedException  - if any thread has interrupted the current thread. The interrupted status of the current thread is cleared when this exception is thrown.
`

主要的含义是**等待该线程的终止**，这里需要解释的是该线程指的是主线程等待子线程的终止。也就是主线程中位于子线程调用`join()`方法后面的代码，只有等到子线程结束后才能执行。

## wait，notify和notifyAll
这三个方法都是`Object`类的实例方法。它们必须是由同步监视器（也就是`syncrynized`包围的那个对象)来调用, 用于协调多个线程对共享数据的存取。`synchronized`关键字用于保护共享数据，阻止其他线程对共享数据的存取，但是这样程序的流程就很不灵活了，如何才能在当前线程还没退出`synchronized`数据块时让其他线程也有机会访问共享数据呢？此时就用这三个方法来灵活控制。

当调用`wait()`方法时线程会放弃对象锁，进入等待此对象的等待锁定池，只有针对此对象调用`notify()`方法后本线程才进入对象锁定池准备`Object`的方法：
- `void notify()`: 唤醒一个正在等待该对象的线程。
- `void notifyAll()`: 唤醒所有正在等待该对象的线程。

`notifyAll`使所有原来在该对象上等待被`notify`的线程统统退出`wait`状态，变成等待该对象上的锁，一旦该对象被解锁，它们会去竞争。`notify`只是选择一个`wait`状态线程进行通知，并使它获得该对象上的锁，但不惊动其它同样在等待被该对象`notify`的线程，当第一个线程运行完毕以后释放对象上的锁，此时如果该对象没有再次使用`notify`语句，即便该对象已经空闲，其他`wait`状态等待的线程由于没有得到该对象的通知，继续处在`wait`状态，直到这个对象发出一个`notify`或`notifyAll`，它们等待的是被`notify`或`notifyAll`，而不是锁。


```java
public class TraditionalThreadCommunication
{
	public static void main(String[] args)
	{
		final Business business = new Business();
		new Thread(new Runnable() {
			public void run()
			{
				for (int i = 1; i <= 50; i++)
				{
					business.sub(i);
				}
			}
		}).start();
		for (int i = 1; i <= 50; i++)
		{
			business.main(i);
		}
	}
}

class Business
{
	private boolean bShouldSub= true;

	public synchronized void sub(int i)
	{
		while (!bShouldSub)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		for (int j = 1; j <= 10; j++)
		{
			System.out.println("sub thread sequence of " + j + ",loop of " + i);
		}
		bShouldSub = false;
		this.notify();
	}

	public synchronizedvoid main(int i)
	{
		while (bShouldSub)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		for (int j = 1; j <= 100; j++)
		{
			System.out.println("main thread sequence of " + j + ",loop of " + i);
		}
		bShouldSub = true;
		this.notify();
	}
}

```


## Condition

如果程序不使用synchronized关键字来保证同步，而是使用Lock对象来保证同步，则就不能用wait,notify, notifyAll了.这时候,JAVA提供了使用Condition类来协调同步.

condition实例被绑定到一个Lock对象上,通过newCondition()来达到这个目的. condition提供三个方法来达到同步

- await(),类似wait()
- sginal() 类似notify
- signalAll()类似notifyAll

```java
public class Condition1Test
{
	public static void main(String[] args)
	{
		final BusinessLock business = new BusinessLock();
		new Thread(new Runnable() {
			public void run()
			{
				for (int i = 1; i <= 50; i++)
				{
					business.sub(i);
				}
			}
		}).start();
		for (int i = 1; i <= 50; i++)
		{
			business.main(i);
		}
	}
}

class BusinessLock
{
	private boolean bShouldSub = true;

	ReentrantLock lock = new ReentrantLock();
	Condition condition = lock.newCondition();

	public void sub(int i)
	{
		lock.lock();
		try
		{
			while (!bShouldSub)
			{
				try
				{
					condition.await();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (int j = 1; j <= 10; j++)
			{
				System.out.println("sub thread sequence of " + j + ",loop of " + i);
			}
			bShouldSub = false;
			condition.signal();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void main(int i)
	{
		lock.lock();
		try
		{
			while (bShouldSub)
			{
				try
				{
					condition.await();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			for (int j = 1; j <= 100; j++)
			{
				System.out.println("main thread sequence of " + j + ",loop of " + i);
			}
			bShouldSub = true;
			condition.signal();
		}
		finally
		{
			lock.unlock();
		}
	}
}

```