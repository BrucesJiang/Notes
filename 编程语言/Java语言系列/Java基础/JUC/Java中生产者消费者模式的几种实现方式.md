# Java中生产者消费者模式的几种实现方式 

本文主要关注与多线程情况下生产者和消费者模式的实现。主要遵从以下逻辑：

1. 生产者生产数据到缓冲区， 消费者从缓冲区中取数据
2. 如果缓冲区满，则生产者线程等待
3. 如果缓冲区空，则消费者线程等待

根本上讲，我们仍旧遵从 **等待/通知**机制，只是使用了不同的方式实现这种机制。 这里总结了以下实现方式：

1. `synchronized+Object的wait和notify`
2. `lock+condition的await和signalAll`
3. `BlockingQueue`


## synchronzed+Object的wait和notify

代码实现如下：

```java
//Wait & Notify 机制
public class ProducerConsumerWithWaitNofity {
	public static void main(String[] args) {
		Resource resource = new Resource();
		//生产者线程
		ProducerThread ph1 = new ProducerThread(resource);
		ProducerThread ph2 = new ProducerThread(resource);
		ProducerThread ph3 = new ProducerThread(resource);
		//消费者线程
		ConsumerThread ch1 = new ConsumerThread(resource);
		//ConsumerThread ch2 = new ConsumerThread(resource);
		//ConsumerThread ch3 = new ConsumerThread(resource);
		
		ph1.start();
		ph2.start();
		ph3.start();
		ch1.start();
		//ch2.start();
		//ch3.start();
		
	}
	//资源类
	static class Resource {
		
		//当前资源数量
		private int num = 0;
		
		//资源池中允许存放的资源数目
		private int size = 10;
		
		/**
		 * 从资源池中取走资源
		 */
		public synchronized void remove() {
			if(num > 0) {
				num --;
				System.out.println("消费者 " + Thread.currentThread().getName()
						+ " 消费资源 ， " + " 当前线程池有  " + num + " 个");
				notifyAll();
			}else {
				try{
					//如果没有资源，则消费者进入等待状态
					wait();
					System.out.println("消费者 " + Thread.currentThread().getName() + " 线程进入等待状态");
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public synchronized void add() {
			if(num < size) {
				num ++;
				System.out.println(Thread.currentThread().getName() + " 生产一个资源， 当前资源池有 " + num + " 个");
				//通知等待的消费者
				notifyAll();
			}else {
				//如果当前资源池满
				try{
					wait(); //生产者进入等待状态, 并释放锁
					System.out.println(Thread.currentThread().getName() + " 线程进入等待状态");
					
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//消费者线程
	static class ConsumerThread extends Thread {
		private Resource resource;
		public ConsumerThread(Resource _resource) {
			this.resource = _resource;
		}
		
		@Override
		public void run() {
			while(true) {
				try{
					Thread.sleep(1000);
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
				resource.remove();
			}
		}
	}

	//生产者线程
	static class ProducerThread extends Thread {
		private Resource resource;
		public ProducerThread(Resource _resource) { 
			this.resource = _resource;
		}
		
		@Override
		public void run() {
			//不断生产
			while(true) {
				try{
					Thread.sleep(1000);
				}catch(InterruptedException e) {
					e.printStackTrace();
				}
				resource.add();
			}
		}
	}
}
```

## lock+condition的await和signalAll
代码实现如下：

```java
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用Lock和Condition解决消费者和生产者问题
 * 
 * @author Bruce Jiang
 *
 */
public class LockCondition {
	public static void main(String[] args) {
		Lock lock = new ReentrantLock();
		Condition producerCondition = lock.newCondition();
		Condition consumerCondition = lock.newCondition();
		Resource resource = new Resource(lock, producerCondition,
				consumerCondition);

		// 生产者线程
		ProducerThread ph1 = new ProducerThread(resource);

		// 消费者线程
		List<ConsumerThread> list = Arrays.asList(new ConsumerThread(resource),
				new ConsumerThread(resource), new ConsumerThread(resource));
		ph1.start();
		for (int i = 0; i < list.size(); i++) {
			list.get(i).start();
		}
	}

	// 资源类
	static class Resource {
		private int num = 0; // 当前资源数量
		private int size = 10; // 资源池中允许存放的资源数量
		private Lock lock;
		private Condition producerCondition;
		private Condition consumerCondition;

		public Resource(Lock lock, Condition producerCondition,
				Condition consumerCondition) {
			this.lock = lock;
			this.producerCondition = producerCondition;
			this.consumerCondition = consumerCondition;
		}

		/**
		 * 向资源池中添加资源
		 */
		public void add() {
			lock.lock();
			try {
				if (num < size) {
					num++;
					System.out.println(Thread.currentThread().getName()
							+ " 生产一个资源， 当前资源池中有 " + num + " 个");
					// 唤醒等待的消费者
					consumerCondition.signalAll();
				} else {
					// 池满，生产者线程等待
					try {
						producerCondition.await();
						System.out.println(Thread.currentThread().getName()
								+ " 线程进入等待");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} finally {
				lock.unlock();
			}
		}

		/**
		 * 从资源池中取走资源
		 */
		public void remove() {
			lock.lock();
			try {
				if (num > 0) {
					num--;
					System.out.println(Thread.currentThread().getName()
							+ " 消费一个资源， 当前资源池中有 " + num + " 个");
					producerCondition.signalAll();// 唤醒等待的生产者
				} else {
					try {
						consumerCondition.await();
						System.out.println(Thread.currentThread().getName()
								+ " 线程进入等待");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			} finally {
				lock.unlock();
			}
		}

	}

	// 消费者线程

	static class ConsumerThread extends Thread {
		private Resource resource;

		public ConsumerThread(Resource resouce) {
			this.resource = resouce;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep((long) (1000 * Math.random()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resource.remove();
			}
		}
	}

	// 生产者线程
	static class ProducerThread extends Thread {
		private Resource resource;

		public ProducerThread(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep((long) (1000 * Math.random()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resource.add();
			}

		}
	}
}
```

## BlockingQueue

代码实现如下：

```java
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 使用阻塞队列BlockingQueue解决生产者消费者问题
 * 
 * @author Bruce Jiang
 *
 */
public class BlockingQueueConsumerProducer {
	public static void main(String[] args) {
		Resource resource = new Resource();
		// 生产者
		ProducerThread ph1 = new ProducerThread(resource);
		// 消费者
		List<ConsumerThread> list = Arrays.asList(new ConsumerThread(resource),
				new ConsumerThread(resource), new ConsumerThread(resource));
		
		ph1.start();
		for(int i = 0; i < list.size(); i ++) {
			list.get(i).start();
		}
	}

	/**
	 * 公共资源
	 */
	static class Resource {
		private BlockingQueue<Integer> resourceQueue = new LinkedBlockingQueue<Integer>(
				10);

		/**
		 * 向资源池中添加资源
		 */
		public void add() {
			try {
				resourceQueue.put(10 * (int) Math.random());
				System.out.println(Thread.currentThread().getName()
						+ " 生产一个资源， " + " 当前资源池有 " + resourceQueue.size()
						+ " 个资源");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 从资源池中移除资源
		 */
		public void remove() {
			try {
				resourceQueue.take();
				System.out.println(Thread.currentThread().getName()
						+ " 消费一个资源， 当前线程池有 " + resourceQueue.size() + " 个资源");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 生产者
	 */
	static class ProducerThread extends Thread {
		private Resource resource;

		public ProducerThread(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep((long) (1000 * Math.random()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resource.add();
			}
		}
	}

	/**
	 * 消费者
	 */
	static class ConsumerThread extends Thread {
		private Resource resource;

		public ConsumerThread(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep((long) (1000 * Math.random()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resource.remove();
			}
		}
	}
}
```