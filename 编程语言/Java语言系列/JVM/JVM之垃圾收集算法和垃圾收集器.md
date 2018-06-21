# JVM之垃圾收集算法和垃圾收集器
本文主要介绍了JVM在发展过程中出现的垃圾收集算法和各代垃圾收集器。 


# 垃圾搜集算法

## 标记-清除算法

最基础的搜集算法是 **标记收集算法**（Mark Sweep），被分为两个阶段 **标记** 和 **清除** ：在标记阶段首先通过根节点（GC Roots）标记所有从根节点开始的对象，未被标记的对象就是未被引用的垃圾对象。在清除阶段，清除所有未被标记的对象。 之所以称之为最基础的收集算法，是因为后序的收集算法都是基于这种思路并对其不足之处进行改进得到的。它主要的不足有两个： 一个是效率问题，标记和清除两个过程效率都不高；另外一个是空间问题，标记清除之后会产生大量不连续的内存碎片，空间碎片太多可能会导致以后在程序运行过程中需要分配较大的对象时，无法找到足够的连续内存而不得不提前触发另一次垃圾收集动作。

## 复制算法

复制算法的出现是为了解决“标记清除算法”中的效率问题。 该算法将内存划分为容量相等的两块，每次只使用一块。当一块内存使用完了，就将还存活的对象复制到另外一块上面，然后将已使用过的内存一次清理掉。这样使得每次都是对整个半区进行内存回收，内存分配时不用考虑内存碎片等复杂情况。

现在的商业虚拟机都采用这种收集算法来回收新生代，IBM研究表明新生代中的对象98%是朝夕生死的，所以并不需要按照1:1的比例划分内存空间，而是将内存分为一块较大的Eden空间和两块较小的Survivor空间，每次使用Eden和其中的一块Survivor。当回收时，将Eden和Survivor中还存活着的对象一次性地拷贝到另外一个Survivor空间上，最后清理掉Eden和刚才用过的Survivor的空间。HotSpot虚拟机默认Eden和Survivor的大小比例是8:1(可以通过-SurvivorRattio来配置)，也就是每次新生代中可用内存空间为整个新生代容量的90%，只有10%的内存会被“浪费”。当然，98%的对象可回收只是一般场景下的数据，我们没有办法保证回收都只有不多于10%的对象存活，当Survivor空间不够用时，需要依赖其他内存（这里指老年代）进行分配担保。

## 标记整理算法

复制收集法在对象存活率较高时就要进行较多的复制操作，效率将会降低。更关键的是如果不想浪费50%的空间就需要额外的空间进行分配担保，以应对被使用的内存中所有对象都100%存活的极端情况，所以在老年代一般不能直接选用这种算法。

我们说标记整理算法（Mark Compact）是一种老年代对象的回收算法。与标记清除算法相比，它做了一些优化。 第一步，同样是从GC Roots根节点开始对所有可达对象一次标记，然后将所有的存活对象都向内存的一端移动，然后直接清理掉端边界以外的内存。 这种方法既避免了碎片的产生，同样也不需要两块相同的内存空间。

## 分代搜集算法

“分代收集算法”（Generational Collection）根据对象存活周期的不同将内存划分为几块。一般是将Java堆划分为新生代和老年代，这样就可以根据各个年代的特点采用最合适的收集算法。在新生代中，每次垃圾收集时都发现有大批对象死去，只有少量存活，就可以选用复制算法，只需要付出少量存活对象的复制成本就可以完成收集。而老年代中因为对象存活率高、没有额外空间对它进行担保，这就必须使用“标记清理”或者“标记整理”算法进行回收。

# 垃圾收集器

## Serial收集器

Serial收集器是最古老的收集器，它的缺点是当Serial收集器想进行垃圾回收的时候，必须暂停用户的所有进程，即stop the world。到现在为止，它依然是虚拟机运行在client模式下的默认新生代收集器，与其他收集器相比，对于限定在单个CPU的运行环境来说，Serial收集器由于没有线程交互的开销，专心做垃圾回收自然可以获得最高的单线程收集效率。

Serial Old是Serial收集器的老年代版本，它同样是一个单线程收集器，使用”标记－整理“算法。这个收集器的主要意义也是被Client模式下的虚拟机使用。在Server模式下，它主要还有两大用途：一个是在JDK1.5及以前的版本中与Parallel Scanvenge收集器搭配使用，另外一个就是作为CMS收集器的后备预案，在并发收集发生Concurrent Mode Failure的时候使用。

通过指定-UseSerialGC参数，使用Serial + Serial Old的串行收集器组合进行内存回收。

## ParNew收集器

ParNew收集器是Serial收集器新生代的多线程实现，注意在进行垃圾回收的时候依然会stop the world，只是相比较Serial收集器而言它会运行多条进程进行垃圾回收。

ParNew收集器在单CPU的环境中绝对不会有比Serial收集器更好的效果，甚至由于存在线程交互的开销，该收集器在通过超线程技术实现的两个CPU的环境中都不能百分之百的保证能超越Serial收集器。当然，随着可以使用的CPU的数量增加，它对于GC时系统资源的利用还是很有好处的。它默认开启的收集线程数与CPU的数量相同，在CPU非常多（譬如32个，现在CPU动辄4核加超线程，服务器超过32个逻辑CPU的情况越来越多了）的环境下，可以使用-XX:ParallelGCThreads参数来限制垃圾收集的线程数。

-UseParNewGC: 打开此开关后，使用ParNew + Serial Old的收集器组合进行内存回收，这样新生代使用并行收集器，老年代使用串行收集器。

## Parallel Scavenge收集器

Parallel是采用复制算法的多线程新生代垃圾回收器，似乎和ParNew收集器有很多的相似的地方。但是Parallel Scanvenge收集器的一个特点是它所关注的目标是吞吐量(Throughput)。所谓吞吐量就是CPU用于运行用户代码的时间与CPU总消耗时间的比值，即吞吐量=运行用户代码时间 / (运行用户代码时间 + 垃圾收集时间)。停顿时间越短就越适合需要与用户交互的程序，良好的响应速度能够提升用户的体验；而高吞吐量则可以最高效率地利用CPU时间，尽快地完成程序的运算任务，主要适合在后台运算而不需要太多交互的任务。

Parallel Old收集器是Parallel Scavenge收集器的老年代版本，采用多线程和”标记－整理”算法。这个收集器是在jdk1.6中才开始提供的，在此之前，新生代的Parallel Scavenge收集器一直处于比较尴尬的状态。原因是如果新生代Parallel Scavenge收集器，那么老年代除了Serial Old(PS MarkSweep)收集器外别无选择。由于单线程的老年代Serial Old收集器在服务端应用性能上的”拖累“，即使使用了Parallel Scavenge收集器也未必能在整体应用上获得吞吐量最大化的效果，又因为老年代收集中无法充分利用服务器多CPU的处理能力，在老年代很大而且硬件比较高级的环境中，这种组合的吞吐量甚至还不一定有ParNew加CMS的组合”给力“。直到Parallel Old收集器出现后，”吞吐量优先“收集器终于有了比较名副其实的应用祝贺，在注重吞吐量及CPU资源敏感的场合，都可以优先考虑Parallel Scavenge加Parallel Old收集器。

-UseParallelGC: 虚拟机运行在Server模式下的默认值，打开此开关后，使用Parallel Scavenge + Serial Old的收集器组合进行内存回收。-UseParallelOldGC: 打开此开关后，使用Parallel Scavenge + Parallel Old的收集器组合进行垃圾回收

## CMS收集器

CMS(Concurrent Mark Swep)收集器是一个比较重要的回收器，现在应用非常广泛，我们重点来看一下，CMS一种获取最短回收停顿时间为目标的收集器，这使得它很适合用于和用户交互的业务。从名字(Mark Swep)就可以看出，CMS收集器是基于标记清除算法实现的。它的收集过程分为四个步骤：

- 初始标记(initial mark)
- 并发标记(concurrent mark)
- 重新标记(remark)
- 并发清除(concurrent sweep)

注意初始标记和重新标记还是会stop the world，但是在耗费时间更长的并发标记和并发清除两个阶段都可以和用户进程同时工作。

不过由于CMS收集器是基于标记清除算法实现的，会导致有大量的空间碎片产生，在为大对象分配内存的时候，往往会出现老年代还有很大的空间剩余，但是无法找到足够大的连续空间来分配当前对象，不得不提前开启一次Full GC。为了解决这个问题，CMS收集器默认提供了一个-XX:+UseCMSCompactAtFullCollection收集开关参数（默认就是开启的)，用于在CMS收集器进行FullGC完开启内存碎片的合并整理过程，内存整理的过程是无法并发的，这样内存碎片问题倒是没有了，不过停顿时间不得不变长。虚拟机设计者还提供了另外一个参数-XX:CMSFullGCsBeforeCompaction参数用于设置执行多少次不压缩的FULL GC后跟着来一次带压缩的（默认值为0，表示每次进入Full GC时都进行碎片整理）。

不幸的是，它作为老年代的收集器，却无法与jdk1.4中已经存在的新生代收集器Parallel Scavenge配合工作，所以在jdk1.5中使用cms来收集老年代的时候，新生代只能选择ParNew或Serial收集器中的一个。ParNew收集器是使用-XX:+UseConcMarkSweepGC选项启用CMS收集器之后的默认新生代收集器，也可以使用-XX:+UseParNewGC选项来强制指定它。

由于CMS收集器现在比较常用，下面我们再额外了解一下CMS算法的几个常用参数：

- UseCMSInitatingOccupancyOnly：表示只在到达阈值的时候，才进行 CMS 回收。
- 为了减少第二次暂停的时间，通过-XX:+CMSParallelRemarkEnabled开启并行remark。如果ramark时间还是过长的话，可以开启-XX:+CMSScavengeBeforeRemark选项，强制remark之前开启一次minor gc，减少remark的暂停时间，但是在remark之后也立即开始一次minor gc。
- CMS默认启动的回收线程数目是(ParallelGCThreads + 3)/4，如果你需要明确设定，可以通过-XX:+ParallelCMSThreads来设定，其中-XX:+ParallelGCThreads代表的年轻代的并发收集线程数目。
- CMSClassUnloadingEnabled： 允许对类元数据进行回收。
- CMSInitatingPermOccupancyFraction：当永久区占用率达到这一百分比后，启动 CMS 回收 (前提是-XX:+CMSClassUnloadingEnabled 激活了)。
- CMSIncrementalMode：使用增量模式，比较适合单 CPU。
- UseCMSCompactAtFullCollection参数可以使 CMS 在垃圾收集完成后，进行一次内存碎片整理。内存碎片的整理并不是并发进行的。
- UseFullGCsBeforeCompaction：设定进行多少次 CMS 垃圾回收后，进行一次内存压缩。

一些建议对于Native Memory:

- 使用了NIO或者NIO框架（Mina/Netty）
- 使用了DirectByteBuffer分配字节缓冲区
- 使用了MappedByteBuffer做内存映射
- 由于Native Memory只能通过FullGC回收，所以除非你非常清楚这时真的有必要，否则不要轻易调用System.gc()。

另外为了防止某些框架中的System.gc调用（例如NIO框架、Java RMI），建议在启动参数中加上-XX:+DisableExplicitGC来禁用显式GC。这个参数有个巨大的坑，如果你禁用了System.gc()，那么上面的3种场景下的内存就无法回收，可能造成OOM，如果你使用了CMS GC，那么可以用这个参数替代：-XX:+ExplicitGCInvokesConcurrent。

此外除了CMS的GC，其实其他针对old gen的回收器都会在对old gen回收的同时回收young gen。

## G1收集器

G1(Garbage First)收集器是一款面向服务端应用的垃圾收集器。HotSpot团队赋予它的使命是在未来替换掉JDK1.5中发布的CMS收集器。与其他GC收集器相比，G1具备如下特点：

- 并行与并发：G1能更充分的利用CPU，多核环境下的硬件优势来缩短stop the world的停顿时间。
- 分代收集：和其他收集器一样，分代的概念在G1中依然存在，不过G1不需要其他的垃圾回收器的配合就可以独自管理整个GC堆。
- 空间整合：G1收集器有利于程序长时间运行，分配大对象时不会因无法得到连续的空间而提前触发一次GC。
- 可预测的非停顿：这是G1相对于CMS的另一大优势，降低停顿时间是G1和CMS共同的关注点，能让使用者明确指定在一个长度为M毫秒的时间片段内，消耗在垃圾收集上的时间不得超过N毫秒。


在G1之前的其他收集器进行收集的范围都是新生代或者老年代，而G1不再是这样。使用G1收集器时，Java堆的内存布局就与其他收集器有很大差别，它将整个Java堆划分为多个大小相等的独立区域（Region），虽然还有新生代和老年代的概念，但是新生代和老年代不再是物理隔离。然们都是一部分`Region`的集合。

G1收集器之所以能建立可预测的停顿时间模型，是因为它可以有计划的避免在整个Java堆中进行全区域的垃圾收集。G1跟踪各个Region里面的垃圾堆积的价值大小（回收所获得空间大小以及回收所需时间的经验值），在后台维护一个有限列表，每次根据允许收集时间，优先选择回收价值最大的`Region`。这种使用Region划分内存空间以及优先级的区域回收方式，保证了G1收集器在有限时间内可以尽可能高的收集效率。 

Region的划分导致一个问题更加突出：对象引用跨Region时，如何保证可达性判定正确？

在G1收集器中，Region之间的对象引用以及其他收集器中的新生代与老年代之间的对象引用，虚拟机都是使用Remembered Set来避免全堆扫描的。G1中的每个Region都有一个与之对应的Remembered Set，虚拟机发现程序在对Reference类型的数据进行写操作时，会产生一个Write Barrier暂时中断写操作，检查Reference引用的对象是否处于不同的Region之中（在分代的例子中就是检查是否有老年代的对象引用了新生代中的对象）。如果是，便通过CardTable把相关引用信息纪录到被引用对象所属的Region的Remembered Set中。当进行内存回收时，在GC节点的枚举范围中加入Remembered Set即可保证不对全堆扫描也不会遗漏。

如果不计算维护Remember Set的操作，G1收集器的运作大致可划分为以下几个步骤：

- 初始标记（Initial Marking）
- 并发标记（Concurrent Marking）
- 最终标记（Final Marking）
- 筛选回收（Live Data Counting and Evacuation）

和CMS类似。虽然G1看起来有很多优点，实际上CMS还是主流。

# JVM常用参数

除了上面提及的一些参数，下面补充一些和GC相关的常用参数：

- Xmx: 设置堆内存的最大值。
- Xms: 设置堆内存的初始值。
- Xmn: 设置新生代的大小。
- Xss: 设置栈的大小。
- PretenureSizeThreshold: 直接晋升到老年代的对象大小，设置这个参数后，大于这个参数的对象将直接在老年代分配。
- MaxTenuringThrehold: 晋升到老年代的对象年龄。每个对象在坚持过一次Minor GC之后，年龄就会加1，当超过这个参数值时就进入老年代。
- UseAdaptiveSizePolicy: 在这种模式下，新生代的大小、eden 和 survivor 的比例、晋升老年代的对象年龄等参数会被自动调整，以达到在堆大小、吞吐量和停顿时间之间的平衡点。在手工调优比较困难的场合，可以直接使用这种自适应的方式，仅指定虚拟机的最大堆、目标的吞吐量 (GCTimeRatio) 和停顿时间 (MaxGCPauseMills)，让虚拟机自己完成调优工作。
- SurvivorRattio: 新生代Eden区域与Survivor区域的容量比值，默认为8，代表Eden: Suvivor= 8: 1。
- XX:ParallelGCThreads：设置用于垃圾回收的线程数。通常情况下可以和 CPU 数量相等。但在 CPU 数量比较多的情况下，设置相对较小的数值也是合理的。
- XX:MaxGCPauseMills：设置最大垃圾收集停顿时间。它的值是一个大于 0 的整数。收集器在工作时，会调整 Java 堆大小或者其他一些参数，尽可能地把停顿时间控制在 MaxGCPauseMills 以内。
- XX:GCTimeRatio:设置吞吐量大小，它的值是一个 0-100 之间的整数。假设 GCTimeRatio 的值为 n，那么系统将花费不超过 1/(1+n) 的时间用于垃圾收集。

# Reference

- 《深入Java虚拟机》