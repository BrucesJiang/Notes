# Java之ThreadLocal类的理解
本文是一篇介绍`java.lang.ThreadLocal`类的文章，包括了以下内容：

* [ThreadLocal的功能](#一ThreadLocal的功能)
* [ThreadLocal的实现原理](#二ThreadLocal的实现原理)
* [ThreadLocal的应用场景](#三ThreadLocal的应用场景)


# （一）ThreadLocal的功能
ThreadLocal实现了什么功能？ `ThreadLocal`类为每个线程提供了非线程共享且仅本线程可见的本地变量。 当线程执行结束时，该实例被GC。 我们说，`ThreadLocal`类实际上相当于提供了一种线程隔离机制，将变量同线程绑定。 更具体的，我们看看Java库API提供的注释：

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

1. `ThreadLocal`提供了线程独享的本地实例。 它与普通变量的区别在于，每个使用该`ThreadLocal`变量的线程都有其独享的数据空间。
2. 使用时，`ThreadLocal`变量通常为`private static`。



# （二）ThreadLocal的实现原理

理解`ThreadLocal`的原理，关键在于理解以下几点： 
1. 如何实现数据独享
2. 保证不发生内存泄露的。 

下面我们将从源代码执行的角度分析ThreadLocal的实现原理，下面示例是对上述Java API提供的示例代码的测试：

```java
import java.util.concurrent.CountDownLatch;

public class ThreadIdTest {
	public static void main(String[] args) throws Exception{
		CountDownLatch countDownLatch = new CountDownLatch(2);
		
		for (int i = 0; i < 30; i++) {
			new Thread(() -> {
					System.out.println(Thread.currentThread().getName() + " : "
							+ ThreadId.get());
			}, "Thread - " + i).start();
		}
	}
}
```
执行结果：

```java
Thread - 4 : 0
Thread - 0 : 12
Thread - 12 : 11
Thread - 11 : 10
Thread - 1 : 3
Thread - 9 : 9
Thread - 8 : 8
Thread - 13 : 4
Thread - 2 : 2
Thread - 6 : 7
Thread - 3 : 1
Thread - 7 : 6
Thread - 5 : 5
Thread - 16 : 15
Thread - 18 : 14
Thread - 10 : 13
Thread - 17 : 16
Thread - 20 : 17
Thread - 19 : 18
Thread - 22 : 19
Thread - 14 : 20
Thread - 15 : 25
Thread - 25 : 24
Thread - 28 : 23
Thread - 26 : 22
Thread - 27 : 21
Thread - 29 : 26
Thread - 21 : 27
Thread - 24 : 28
Thread - 23 : 29
```
在上述代码中，我们通过30个线程获取线程ID，并查看结果。 我们调用了`ThreadId`类的`get()`方法，本质上是`ThreadLocal`实例对象的`get()`方法。那么我们就从这里入手。

首先看一下, `ThreadLocal#get()`方法的源代码

```java
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}
```
我们发现，首先通过`Thread.currentThread()`方法获取当前线程的引用，并作为实参传递给`getMap()`方法以获取一个`ThreadLocalMap`实例。 按照调用栈继续跟进`getMap()`方法：

```java
ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}
```
从源码我们可以看到，该方法直接返回一个`Thread`实例的成员变量`threadLocals`。 我们看一下`Thread`类内部有关该类变量的定义:
```java
public class Thread implements Runnable {
	...

	/* ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
    ThreadLocal.ThreadLocalMap threadLocals = null;
	
	...
}
```
我们可以看到，`Thread`类定义中`threadLocals`为`ThreadLocal.ThreadLocalMap`类型。我们可以得出结论： **ThreadLocal类的内部类ThreadLocalMap保证了每个线程变量的本地隔离特性**，其原理如下图所示：

![threadlocal](./images/threadlocal.png)

确切的讲，内部类`ThreadLocal#ThreadLocalMap`通过一个`Map`结构维系了特定线程与数据之间的对应关系，而`ThreadLocal`类是这种关系的维护者和`ThreadLocalMap`的对外接口。 本质上说，通过`ThreadLocal#ThreadLocalMap`为每个线程创建了其独享的数据空间，这也就保证了`ThreadLocalMap`维护的数据是线程独享的。 有关内部类`ThreadLocalMap`的具体功能和实现，我将在下文详述。

下面，我们接着看`ThreadLocal#get()`方法。 我们直接叙述`map`为空的情况，它将调用`ThreadLocal#setInitialValue`方法。 另一个`Entry`为`TreadLocalMap`的内部类，我们下文和`ThreadLocalMap`一起叙述。 

```java
private T setInitialValue() {
    T value = initialValue();
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
    return value;
}
```
首先，我们发现了`ThreadId#initialValue`方法，这里会对整个初始值做初始化。 这里会不会发生线程不安全问题？ 回答是当然会，因此我们在`ThreadId`实现时操作的变量是原子类型。 我们发现这里本质上有两个过程： 1）当`map`不空时的`map.set(this, value)`和 2）当`map`空时的`createMap(t, value)`过程。 我们在这里着重讲述`ThreadLocal#createMap(Thread, T)`

```java
void createMap(Thread t, T firstValue) {
    t.threadLocals = new ThreadLocalMap(this, firstValue);
}
```

到此，我们发现整个`ThreadLocal#get()`方法就已经执行完毕。我们同样也获得了我们想要的结果，线程数据独享的原理本质是一个`map`连接的双方是`Thread`和`ThreadLocal#ThreadLocalMap`。

### ThreadLocalMap
嵌套类`ThreadLocalMap`的基本结构如下：

```java

private static final int INITIAL_CAPACITY = 16;

private Entry[] table;

private int size = 0;

private int threshold; // Default to 0
```
其中`INITIAL_CAPACITY`代表这个`Map`的初始容量；`table`是一个`Entry`类型的数组，用于存储数据；`size`代表表中的存储数目；`threshold`代表需要扩容时对应`size`的阈值。

`Entry`类是`ThreadLocalMap`的静态内部类，用于存储数据。 它的源码如下：

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

`Entry`类继承了`WeakReference<ThreadLocal<?>>`，即每个`Entry`对象都有一个`ThreadLocal`的弱引用（作为`key`），这是为了防止内存泄露。明白弱引用的特点就知道为什么有这个结论了。 弱引用关联的对象只能生存到下一次GC工作之前。当GC工作时，无论当前内存是否足够，都会回收掉只被弱引用关联的对象。

`ThreadLocalMap`类有两个构造函数，其中常用的是`ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue)`：

```java
ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
    table = new Entry[INITIAL_CAPACITY];
    int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
    table[i] = new Entry(firstKey, firstValue);
    size = 1;
    setThreshold(INITIAL_CAPACITY);
}
```
构造函数的第一个参数就是代表本`ThreadLocal`的实例对象`this`，第二个参数就是要保存的线程本地变量。构造函数首先创建一个长度为`16`的`Entry`数组，然后计算出`firstKey`对应的哈希值，然后存储到`table`中，并设置`size`和`threshold`。

这里需要注意一个细节，计算`hash`的时候里面采用了`hashCode & (size - 1)`的算法，这相当于取模运算`hashCode % size`的一个更高效的实现（这和和`HashMap`中的思路相同，都是为了提高效率）。正是因为这种算法，我们要求`size`必须是`2`的指数，因为这可以使得`hash`发生冲突的次数减小。

下面我们将具体分析`ThreadLocal#ThreadLocalMap`类是如何管理具体数据存储`Entry`数组，并且如何保证不发生内存泄露的。

我们首先看一下`ThreadLocalMap#set()`方法：

```java
private void set(ThreadLocal<?> key, Object value) {
    // We don't use a fast path as with get() because it is at
    // least as common to use set() to create new entries as
    // it is to replace existing ones, in which case, a fast
    // path would fail more often than not.
    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);
    for (Entry e = tab[i];
         e != null;
         e = tab[i = nextIndex(i, len)]) {
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
以及`ThreadLocalMap#nextIndex()`方法：

```java
private static int nextIndex(int i, int len) {
    return ((i + 1 < len) ? i + 1 : 0);
}
```
我们看到`ThreadLocalMap`解决冲突的方法是`线性探测法`（不断加1），而不是`HashMap`的`链地址法`，这一点从`Entry`的结构上也能推断出来。

如果`entry`里对应的`key`为`null`，表明此`entry`为`staled entry`，就将其替换为当前的`key`和`value`：

```java
private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                               int staleSlot) {
    Entry[] tab = table;
    int len = tab.length;
    Entry e;
    // Back up to check for prior stale entry in current run.
    // We clean out whole runs at a time to avoid continual
    // incremental rehashing due to garbage collector freeing
    // up refs in bunches (i.e., whenever the collector runs).
    int slotToExpunge = staleSlot;
    for (int i = prevIndex(staleSlot, len);
         (e = tab[i]) != null;
         i = prevIndex(i, len))
        if (e.get() == null)
            slotToExpunge = i;
    // Find either the key or trailing null slot of run, whichever
    // occurs first
    for (int i = nextIndex(staleSlot, len);
         (e = tab[i]) != null;
         i = nextIndex(i, len)) {
        ThreadLocal<?> k = e.get();
        // If we find key, then we need to swap it
        // with the stale entry to maintain hash table order.
        // The newly stale slot, or any other stale slot
        // encountered above it, can then be sent to expungeStaleEntry
        // to remove or rehash all of the other entries in run.
        if (k == key) {
            e.value = value;
            tab[i] = tab[staleSlot];
            tab[staleSlot] = e;
            // Start expunge at preceding stale entry if it exists
            if (slotToExpunge == staleSlot)
                slotToExpunge = i;
            cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
            return;
        }
        // If we didn't find stale entry on backward scan, the
        // first stale entry seen while scanning for key is the
        // first still present in the run.
        if (k == null && slotToExpunge == staleSlot)
            slotToExpunge = i;
    }
    // If key not found, put new entry in stale slot
    tab[staleSlot].value = null;
    tab[staleSlot] = new Entry(key, value);
    // If there are any other stale entries in run, expunge them
    if (slotToExpunge != staleSlot)
        cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
}
```
这替换过程里面也进行了不少的垃圾清理动作以防止引用关系存在而导致的内存泄露。

若是经历了上面步骤没有命中hash，也没有发现无用的Entry，set方法就会创建一个新的Entry，并会进行启发式的垃圾清理，用于清理无用的Entry。主要通过cleanSomeSlots方法进行清理。

```jav
private boolean cleanSomeSlots(int i, int n) {
    boolean removed = false;
    Entry[] tab = table;
    int len = tab.length;
    do {
        i = nextIndex(i, len);
        Entry e = tab[i];
        if (e != null && e.get() == null) {
            n = len;
            removed = true;
            i = expungeStaleEntry(i);
        }
    } while ( (n >>>= 1) != 0);
    return removed;
}
```
一旦发现一个位置对应的`Entry`所持有的`ThreadLocal`弱引用为`null`，就会把此位置当做`staleSlot`并调用`expungeStaleEntry`方法进行整理(`rehashing`)的操作：


```java
private int expungeStaleEntry(int staleSlot) {
    Entry[] tab = table;
    int len = tab.length;
    // expunge entry at staleSlot
    tab[staleSlot].value = null;
    tab[staleSlot] = null;
    size--;
    // Rehash until we encounter null
    Entry e;
    int i;
    for (i = nextIndex(staleSlot, len);
         (e = tab[i]) != null;
         i = nextIndex(i, len)) {
        ThreadLocal<?> k = e.get();
        if (k == null) {
            e.value = null;
            tab[i] = null;
            size--;
        } else {
            int h = k.threadLocalHashCode & (len - 1);
            if (h != i) {
                tab[i] = null;
                // Unlike Knuth 6.4 Algorithm R, we must scan until
                // null because multiple entries could have been stale.
                while (tab[h] != null)
                    h = nextIndex(h, len);
                tab[h] = e;
            }
        }
    }
    return i;
}
```
只要没有清理任何的`stale entries`并且`size`达到阈值的时候（即`table`已满，所有元素都可用），都会触发`rehashing`

```java
private void rehash() {
    expungeStaleEntries();
    // Use lower threshold for doubling to avoid hysteresis
    if (size >= threshold - threshold / 4)
        resize();
}


private void expungeStaleEntries() {
    Entry[] tab = table;
    int len = tab.length;
    for (int j = 0; j < len; j++) {
        Entry e = tab[j];
        if (e != null && e.get() == null)
            expungeStaleEntry(j);
    }
}
```
rehash操作会执行一次全表的扫描清理工作，并在size大于等于threshold的四分之三时进行resize，即扩容一倍。因此ThreadLocalMap的加载因子一样为0.75。

下面我们看到`ThreadLocal#get()`方法中的`getEntry()`方法：

```java
private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
    Entry[] tab = table;
    int len = tab.length;
    while (e != null) {
        ThreadLocal<?> k = e.get();
        if (k == key)
            return e;
        if (k == null)
            expungeStaleEntry(i);
        else
            i = nextIndex(i, len);
        e = tab[i];
    }
    return null;
}
```
逻辑很简单，`hash`以后如果是`ThreadLocal`对应的`Entry`就返回，否则调用`getEntryAfterMiss`方法，根据线性探测法继续查找，直到找到或对应`entry`为`null`，并返回。


最后，我们`ThreadLocal#set()`方法的执行逻辑与`get()`方法主体部分基本类似：

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}
```

# （三）小结

- ThreadLocal 并不解决线程间共享数据的问题
- ThreadLocal 通过隐式的在不同线程内创建独立实例副本避免了实例线程安全的问题
- 每个线程持有一个 Map 并维护了 ThreadLocal 对象与具体实例的映射，该 Map 由于只被持有它的线程访问，故不存在线程安全以及锁的问题
- ThreadLocalMap 的 Entry 对 ThreadLocal 的引用为弱引用，避免了 ThreadLocal 对象无法被回收的问题
- ThreadLocalMap 的 set 方法通过调用 replaceStaleEntry 方法回收键为 null 的 Entry 对象的值（即为具体实例）以及 Entry 对象本身从而防止内存泄漏
- ThreadLocal 适用于变量在线程间隔离且在方法间共享的场景

每个Thread里都含有一个`ThreadLocalMap`的成员变量，这种机制将`ThreadLocal`和`线程`巧妙地绑定在了一起，即可以保证无用的`ThreadLocal`被及时回收，不会造成内存泄露，又可以提升性能。假如我们把`ThreadLocalMap`做成一个`Map<t extends Thread, ?>`类型的`Map`，那么它存储的东西将会非常多（相当于一张全局线程本地变量表），这样的情况下用线性探测法解决哈希冲突的问题效率会非常差。而JDK里的这种利用`ThreadLocal`作为`key`，再将`ThreadLocalMap`与线程相绑定的实现，完美地解决了这个问题。

总结一下什么时候无用的`Entry`会被清理：

- Thread结束的时候
- 插入元素时，发现staled entry，则会进行替换并清理
- 插入元素时，ThreadLocalMap的size达到threshold，并且没有任何staled entries的时候，会调用rehash方法清理并扩容
- 调用ThreadLocalMap的remove方法或set(null)时

尽管不会造成内存泄露，但是可以看到无用的Entry只会在以上四种情况下才会被清理，这就可能导致一些Entry虽然无用但还占内存的情况。因此，我们在使用完ThreadLocal后一定要remove一下，保证及时回收掉无用的Entry。

# （四）ThreadLocal的应用场景

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



【场景】当应用线程池的时候，由于线程池的线程一般会复用，Thread不结束，这时候用完更需要remove了。

总的来说，对于多线程资源共享的问题，同步机制采用了“以时间换空间”的方式，而ThreadLocal采用了“以空间换时间”的方式。前者仅提供一份变量，让不同的线程排队访问；而后者为每一个线程都提供了一份变量，因此可以同时访问而互不影响。

在框架上的应用， 比如Spring。Spring对一些Bean中的成员变量采用ThreadLocal进行处理，让它们可以成为线程安全的。举个例子：

```java
package org.springframework.web.context.request;
public abstract class RequestContextHolder  {
	private static final boolean jsfPresent =
			ClassUtils.isPresent("javax.faces.context.FacesContext", RequestContextHolder.class.getClassLoader());
	private static final ThreadLocal<RequestAttributes> requestAttributesHolder =
			new NamedThreadLocal<RequestAttributes>("Request attributes");
	private static final ThreadLocal<RequestAttributes> inheritableRequestAttributesHolder =
			new NamedInheritableThreadLocal<RequestAttributes>("Request context");
            //......下面省略
}
```

再比如`Spring MVC`中的`Controller`默认是`singleton`的，因此如果`Controller`或其对应的`Service`里存在非静态成员变量的话，并发访问就会出现`race condition`问题，这也可以通过`ThreadLocal`解决。

# 一些文章

- [Java Doc](https://docs.oracle.com/javase/7/docs/api/java/lang/ThreadLocal.html)
- [ThreadLocal理解](https://www.cnblogs.com/jasongj/p/8079718.html)
- 