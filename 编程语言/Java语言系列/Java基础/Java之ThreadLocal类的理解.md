# Java之ThreadLocal类的理解
本文是一篇介绍`java.lang.ThreadLocal`类的文章，包括了以下内容：

* [ThreadLocal的功能](#一ThreadLocal的功能)
* [ThreadLocal的实现原理](#二ThreadLocal的实现原理)
* [ThreadLocal的应用场景](#三ThreadLocal的应用场景)


# （一）ThreadLocal的功能
ThreadLocal实现了什么功能？我们说，`ThreadLocal`类为每个线程提供了非线程间共享且仅本线程可见的本地变量。 更具体的，我们看看Java库API提供的注释：

This class provides thread-local variables. These variables differ from their normal counterparts in that each thread that accesses one (via its get or set method) has its own, independently initialized copy of the variable. ThreadLocal instances are typically private static fields in classes that wish to associate state with a thread (e.g., a user ID or Transaction ID).

For example, the class below generates unique identifiers local to each thread. A thread's id is assigned the first time it invokes ThreadId.get() and remains unchanged on subsequent calls.
```
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadId {
     // Atomic integer containing the next thread ID to be assigned
     private static final AtomicInteger nextId = new AtomicInteger(0);

     // Thread local variable containing each thread's ID
     private static final ThreadLocal<Integer> threadId =
         new ThreadLocal<Integer>() {
             @Override protected Integer initialValue() {
                 return nextId.getAndIncrement();
         }
     };

     // Returns the current thread's unique ID, assigning it if necessary
     public static int get() {
         return threadId.get();
     }
 }
 

```
Each thread holds an implicit reference to its copy of a thread-local variable as long as the thread is alive and the ThreadLocal instance is accessible; after a thread goes away, all of its copies of thread-local instances are subject to garbage collection (unless other references to these copies exist).

From [Java API](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html)

它包括以下几个要点：

1. `ThreadLocal`为提供了特定线程独享的本地实例。 它与普通变量的区别在于，每个使用该`ThreadLocal`变量的线程都有其独享的数据空间。
2. 使用时，`ThreadLocal`变量通常为`private static`。



# （二）ThreadLocal的实现原理
从源代码的角度分析ThreadLocal的实现原理。  以下几点：

1. 如何保证ThreadLocal保存的数据线程独享

最朴素的想法，在每个为每个线程中`ThreadLocal`实例对象都重新分配一个数据空间。这个过程在该线程第一次调用`ThreadLocal`对象的`get()`方法时得到实现，`ThreadLocal`实例对象重新开辟数据空间。 我们说，对于每个线程而言，它保存的`ThreadLocal`实例对象的引用和另外一个线程是不同的。这种实现机制由以下几个函数实现。

![threadlocal](./images/threadlocal.png)

与上图匹配的小例子：

```java
import java.util.concurrent.CountDownLatch;

public class ThreadLocalTest {
	public static void main(String[] args) throws Exception{
		final int threads = 3;

		CountDownLatch countDownLatch = new CountDownLatch(threads);
		InnerClass innerClass = new InnerClass();
		for (int i = 0; i < threads; i++) {
			new Thread(() -> {
				for (int j = 0; j < 4; j++) {
					innerClass.add(String.valueOf(j));
					innerClass.print();
				}
				innerClass.set("Hello World");
				countDownLatch.countDown();
			}, "Thread-" + i).start();
		}
		countDownLatch.await();
	}

	private static class InnerClass {
		public void add(String str) {
			StringBuilder nStr = Counter.counter.get();
			Counter.counter.set(nStr.append(str));
		}
		
		public void print() {
			System.out
					.printf("Thread name: %s, ThreadLocal hashCode: %s, Instance hashcode: %s, Value: %s\n",
							Thread.currentThread().getName(), Counter.counter
									.hashCode(), Counter.counter.get().hashCode(),
							Counter.counter.get().toString());
		}

		public void set(String words) {
			Counter.counter.set(new StringBuilder(words));
			Counter.counter.set(new StringBuilder(words));
			System.out
					.printf("Set, Thread name:%s , ThreadLocal hashcode:%s,  Instance hashcode:%s, Value:%s\n",
							Thread.currentThread().getName(), Counter.counter
									.hashCode(), Counter.counter.get().hashCode(),
							Counter.counter.get().toString());
		}
	}

	private static class Counter {
		private static ThreadLocal<StringBuilder> counter = new ThreadLocal<StringBuilder>(){
			@Override
			protected StringBuilder initialValue() {
				return new StringBuilder();
			}
		};
	}
}


```

从源代码看：

```java
public T get() {
    Thread t = Thread.currentThread(); //获得当前线程
    ThreadLocalMap map = getMap(t); //TheadLocalMap是否存在，也就是是否已经存在数据空间
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue(); //如果没有，则重新初始化一个新的数据空间
}

//初始化函数
private T setInitialValue() {
    T value = initialValue(); //
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
    return value;
}

//初始化为空
protected T initialValue() {
    return null;
}
```
`setInitialValue` 方法为`private`方法，无法被重载。

首先，通过`initialValue()`方法获取初始值。该方法为 `protected` 方法，且默认返回`null`。所以典型用法中常常重载该方法。上例中即在内部匿名类中将其重载。然后拿到该线程对应的`ThreadLocalMap`对象，若该对象不为`null`，则直接将该`ThreadLocal`对象与对应实例初始值的映射添加进该线程的`ThreadLocalMap`中。若为`null`，则先创建该 `ThreadLocalMap`对象再将映射添加其中。

2. 如何避免内存泄露

依照上述实现方式，我们最直观的感觉是会造成内存泄露问题。 这个问题源自于当线程使用完毕，而线程没有结束时，`ThreadLocal`实例对象所在用的数据空间就不会被回收。 因此，在实现时设计者采用了弱引用的方式来保持`ThreadLocal`实例对象引用和其数据空间之间的连接关系。 我们知道相比于强引用，弱引用更加容易回收。 它的实现，如下代码所示：

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
  /** The value associated with this ThreadLocal. */
  Object value;

  Entry(ThreadLocal<?> k, Object v) {
    super(k);
    value = v;
  }
}
```

`Entry`为`ThreadLocalMap`的成员。这里并不需要考虑 ThreadLocalMap 的线程安全问题。因为每个线程有且只有一个 ThreadLocalMap 对象，并且只有该线程自己可以访问它，其它线程不会访问该 ThreadLocalMap，也即该对象不会在多个线程中共享，也就不存在线程安全的问题。

更确切的说，对于已经不再被使用且已被回收的`ThreadLocal`对象，它在每个线程内对应的实例由于被线程的`ThreadLocalMap`的`Entry`强引用，无法被回收，可能会造成内存泄漏。

针对该问题，`ThreadLocalMap`的`set`方法中，通过`replaceStaleEntry`方法将所有键为`null`的`Entry`的值设置为`null`，从而使得该值可被回收。另外，会在`rehash`方法中通过 `expungeStaleEntry`方法将键和值为`null`的`Entry`设置为`null`从而使得该`Entry`可被回收。通过这种方式,`ThreadLocal`可防止内存泄漏。

```java
private void set(ThreadLocal<?> key, Object value) {
  Entry[] tab = table;
  int len = tab.length;
  int i = key.threadLocalHashCode & (len-1);

  for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
    ThreadLocal<?> k = e.get();
    if (k == key) {
      e.value = value;
      return;
    }
    if (k == null) {
      replaceStaleEntry(key, value, i);
      return;
    }
  }
  tab[i] = new Entry(key, value);
  int sz = ++size;
  if (!cleanSomeSlots(i, sz) && sz >= threshold)
    rehash();
}
```
# （三）ThreadLocal的应用场景

对于Web应用而言，	`Session`保存了很多信息。很多时候需要通过`Session`获取信息，有些时候又需要修改`Session`的信息。一方面，需要保证每个线程有自己单独的`Session`实例。另一方面，由于很多地方都需要操作`Session`，存在多方法共享`Session`的需求。如果不使用`ThreadLocal`，可以在每个线程内构建一个 `Session`实例，并将该实例在多个方法间传递，如下所示。

```java
public class SessionHandler {

  public static class Session {
    private String id;
    private String user;
    private String status;
  }

  public Session createSession() {
    return new Session();
  }

  public String getUser(Session session) {
    return session.getUser();
  }

  public String getStatus(Session session) {
    return session.getStatus();
  }

  public void setStatus(Session session, String status) {
    session.setStatus(status);
  }

  public static void main(String[] args) {
    new Thread(() -> {
      SessionHandler handler = new SessionHandler();
      Session session = handler.createSession();
      handler.getStatus(session);
      handler.getUser(session);
      handler.setStatus(session, "close");
      handler.getStatus(session);
    }).start();
  }
}
```

该方法是可以实现需求的。但是每个需要使用 Session 的地方，都需要显式传递 Session 对象，方法间耦合度较高。 这里使用 ThreadLocal 重新实现该功能如下所示。

```java
public class SessionHandler {

  public static ThreadLocal<Session> session = new ThreadLocal<Session>();

  @Data
  public static class Session {
    private String id;
    private String user;
    private String status;
  }

  public void createSession() {
    session.set(new Session());
  }

  public String getUser() {
    return session.get().getUser();
  }

  public String getStatus() {
    return session.get().getStatus();
  }

  public void setStatus(String status) {
    session.get().setStatus(status);
  }

  public static void main(String[] args) {
    new Thread(() -> {
      SessionHandler handler = new SessionHandler();
      handler.getStatus();
      handler.getUser();
      handler.setStatus("close");
      handler.getStatus();
    }).start();
  }
}

```
使用 ThreadLocal 改造后的代码，不再需要在各个方法间传递 Session 对象，并且也非常轻松的保证了每个线程拥有自己独立的实例。

如果单看其中某一点，替代方法很多。比如可通过在线程内创建局部变量可实现每个线程有自己的实例，使用静态变量可实现变量在方法间的共享。但如果要同时满足变量在线程间的隔离与方法间的共享，ThreadLocal再合适不过。

# 小结

- ThreadLocal 并不解决线程间共享数据的问题
- ThreadLocal 通过隐式的在不同线程内创建独立实例副本避免了实例线程安全的问题
- 每个线程持有一个 Map 并维护了 ThreadLocal 对象与具体实例的映射，该 Map 由于只被持有它的线程访问，故不存在线程安全以及锁的问题
- ThreadLocalMap 的 Entry 对 ThreadLocal 的引用为弱引用，避免了 ThreadLocal 对象无法被回收的问题
- ThreadLocalMap 的 set 方法通过调用 replaceStaleEntry 方法回收键为 null 的 Entry 对象的值（即为具体实例）以及 Entry 对象本身从而防止内存泄漏
- ThreadLocal 适用于变量在线程间隔离且在方法间共享的场景

# 一些文章

- [Java Doc](https://docs.oracle.com/javase/7/docs/api/java/lang/ThreadLocal.html)
- [ThreadLocal理解](https://www.cnblogs.com/jasongj/p/8079718.html)
- 