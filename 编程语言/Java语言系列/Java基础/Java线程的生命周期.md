# Java线程的生命周期
本文通过代码示例的方式介绍了Java线程的生命周期。

* [概述](#概述)
* [线程的生命周期](#线程的生命周期)
* [更细的划分](#更细的划分)

## 概述

Java中的线程被划分为六种状态，每个线程都可以在任意一个时间点都能处于其中的任意一个状态。
- **New**
- **RUNNABLE**
- **BLOCKED**
- **WAITING**
- **TIMED_WAITING**
- **TERMINATED**

下图展示线程的生命周期中状态及其关系：

！[threadLifeCycle](./images/threadLifeCycle.jpg)

图片来源：[Core Java Vol 1, 9th Edition, Horstmann, Cay S. & Cornell, Gary_2013](https://www.google.co.in/search?q=Core+Java+Vol+1%2C+9th+Edition%2C+Horstmann%2C+Cay+S.+%26+Cornell%2C+Gary_2013&oq=Core+Java+Vol+1%2C+9th+Edition%2C+Horstmann%2C+Cay+S.+%26+Cornell%2C+Gary_2013&aqs=chrome..69i57.383j0j7&sourceid=chrome&ie=UTF-8)

## 线程的生命周期
In Java, to get the current state of the thread, use Thread.getState() method to get the current state of the thread. Java provides java.lang.Thread.State class that defines the ENUM constants for the state of a thread, as summary of which is given below:

1. **Constant type** : `New`
- Declaration: public static final Thread.State NEW
- Description: Thread state for a thread which has not yet started.

2. **Constant type** : `Runnable`
- Declaration: public static final Thread.State RUNNABLE
- Description: Thread state for a runnable thread. A thread in the runnable state is executing in the Java virtual machine but it may be waiting for other resources from the operating system such as processor.

3. **Constant type**: `Blocked`
- Declaration: public static final Thread.State BLOCKED
- Description: Thread state for a thread blocked waiting for a monitor lock. A thread in the blocked state is waiting for a monitor lock to enter a synchronized block/method or reenter a synchronized block/method after calling Object.wait().

4. **Constant type**: `Waiting`
- Declaration: public static final Thread.State WAITING
- Description: Thread state for a waiting thread. Thread state for a waiting thread. A thread is in the waiting state due to calling one of the following methods:
	- Object.wait with no timeout
	- Thread.join with no timeout
	- LockSupport.park

A thread in the waiting state is waiting for another thread to perform a particular action.

5. **Constant type**: `Timed Waiting`
- Declaration: public static final Thread.State TIMED_WAITING
- Description: Thread state for a waiting thread with a specified waiting time. A thread is in the timed waiting state due to calling one of the following methods with a specified positive waiting time:
	- Thread.sleep
	- Object.wait with timeout
	- Thread.join with timeout
	- LockSupport.parkNanos
	- LockSupport.parkUntil

6. **Constant type**: `Terminated`
- Declaration: public static final Thread.State TERMINATED
- Description: Thread state for a terminated thread. The thread has completed execution.


```java
package thread;

//Java Program to demonstrates thread states
class DThread implements Runnable {
	@Override 
	public void run() {
		//moving thread2 to timed waiting state
		try{
			Thread.sleep(1500);
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		try{
			Thread.sleep(1500);
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("State of thread1 while it called join() method on thread2 - " + Test.thread1.getState());
		try{
			Thread.sleep(200);
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}


public class Test implements Runnable{
	public static Thread thread1;
	public static Test obj;
	
	public static void main(String[] args) {
		obj = new Test();
		thread1 = new Thread(obj);
		
		//thread1 created and is currently in the NEW state
		System.out.println("State of thread1 after creating it - " + thread1.getState());
		thread1.start();
		
		//thread1 moved to Runnable state
		System.out.println("State of thread1 after calling start() method on it - " + thread1.getState());
	}
	
	@Override 
	public void run() {
		DThread thread = new DThread();
		Thread thread2 = new Thread(thread);
		
		//thread1 created and is currently in the New State
		System.out.println("State of thread2 after creating it - " + thread2.getState());
		thread2.start();
		// thread2 moved to Runnable state
        System.out.println("State of thread2 after calling .start() method on it - " + 
            thread2.getState());
		//moving thread1 to timed waiting state
		try{
			//moving thread2 to timed waitin state
			Thread.sleep(200);
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("State of thread2 after calling sleep() method on it - " + thread2.getState());
		
		try{
			//waiting for thread2 to die
			thread2.join();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		System.out.println("State of thread2 when it has finished it's execution - " + thread2.getState());
	}
}
```
It's output

```java
State of thread1 after creating it - NEW
State of thread1 after calling start() method on it - RUNNABLE
State of thread2 after creating it - NEW
State of thread2 after calling .start() method on it - RUNNABLE
State of thread2 after calling sleep() method on it - TIMED_WAITING
State of thread1 while it called join() method on thread2 - WAITING
State of thread2 when it has finished it ss execution - TERMINATED
```

Explanation: When a new thread is created, the thread is in the NEW state. When .start() method is called on a thread, the thread scheduler moves it to Runnable state. Whenever join() method is called on a thread instance, the current thread executing that statement will wait for this thread to move to Terminated state. So, before the final statement is printed on the console, the program calls join() on thread2 making the thread1 wait while thread2 completes its execution and is moved to Terminated state. thread1 goes to Waiting state because it it waiting for thread2 to complete it’s execution as it has called join on thread2.

## 更细的划分

![lifecycle_of_a_thread](/images/lifecycle_of_a_thread.jpg)

### volatile和syncronized关键字

Java支持多个线程同时访问一个对象或者对象的成员变量，由于每个线程可以拥有这个变量的拷贝（虽然对象以及成员变量分配的内存是在共享内存中的，但是每个执行的线程还是可以拥有一份拷贝，这样做的目的是加速程序的执行，这是现代多核处理器的一个显著特性），所以程序在执行过程中，一个线程看到的变量并不一定是最新的。

- 关键字volatile可以用来修饰字段（成员变量），就是告知程序任何对该变量的访问均需要从共享内存中获取，而对它的改变必须同步刷新回共享内存，它能保证所有线程对变量访问的可见性。
- 关键字synchronized可以修饰方法或者以同步块的形式来进行使用，它主要确保多个线程在同一个时刻，只能有一个线程处于方法或者同步块中，它保证了线程对变量访问的可见性和排他性。

同步的实现本质是对一个对象的监视器（monitor）进行获取，而这个获取过程是排他的，也就是同一时刻只能有一个线程获取到由synchronized所保护对象的监视器。任意一个对象都拥有自己的监视器，当这个对象由同步块或者这个对象的同步方法调用时，执行方法的线程必须先获取到该对象的监视器才能进入同步块或者同步方法，而没有获取到监视器（执行该方法）的线程将会被阻塞在同步块和同步方法的入口处，进入BLOCKED态。

![monitor](./images/monitor.jpg)

可以看到，任意线程对Object（Object由synchronized保护）的访问，首先要获得Object的监视器。如果获取失败，线程进入同步队列，线程状态变为BLOCKED。当访问Object的前驱（获得了锁的线程）释放了锁，则该释放操作唤醒阻塞在同步队列中的线程，使其重新尝试对监视器的获取。

### 等待/通知机制
一个线程修改了一个对象的值，而另一个线程感知到了变化，然后进行相应的操作，整个过程开始于一个线程，而最终执行又是另一个线程。

等待/通知机制，是指一个线程A调用了对象O的wait()方法进入等待状态，而另一个线程B调用了对象O的notify()或者notifyAll()方法，线程A收到通知后从对象O的wait()方法返回，进而执行后续操作。上述两个线程通过对象O来完成交互，而对象上的wait()和notify/notifyAll()的关系就如同开关信号一样，用来完成等待方和通知方之间的交互工作。

1. 使用wait()、notify()和notifyAll()时需要先对调用对象加锁。
2. 调用wait()方法后，线程状态由RUNNING变为WAITING，并将当前线程放置到对象的等待队列。
3. notify()或notifyAll()方法调用后，等待线程依旧不会从wait()返回，需要调用notify()或notifAll()的线程释放锁之后，等待线程才有机会从wait()返回。
4. notify()方法将等待队列中的一个等待线程从等待队列中移到同步队列中，而notifyAll()方法则是将等待队列中所有的线程全部移到同步队列，被移动的线程状态由WAITING变为BLOCKED。
5. 从wait()方法返回的前提是获得了调用对象的锁。

等待/通知机制依托于同步机制，其目的就是确保等待线程从wait()方法返回时能够感知到通知线程对变量做出的修改。