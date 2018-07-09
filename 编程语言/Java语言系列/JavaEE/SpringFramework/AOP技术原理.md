# AOP技术原理

AOP(Aspect-Oriented Programming， 面向方面编程)，可以说是OOP（Object-Oriented Programming，面向对象编程）的补充和完善。OOP引入封装、继承和多态等概念来建立一种对象层次结构，用以模拟公共行为的一个集合。当我们需要为分散的对象引入公共行为的时候，OOP则显得无能为力。也就是说，OOP允许我们定义从上到下的关系，但并不适合定义从左到右的关系。例如，日志功能。日志代码往往水平的散布在所有对象层次中，而与它散布到对象的核心功能毫无关系。对于其他类型的代码，如安全性、异常处理和透明的持续性也是如此。这种散布在各处的无关的代码被称为横切（cross-cutting）代码。在OOP的设计中，它导致了大量代码的重复，而不利于各个模块的复用。

而AOP技术则恰恰相反，它利用一种称为“横切”的技术，剖解封装对象的内部，并将那些影响了多个类的公共行为封装到一个可用模块，并将其命名为"Aspect",即“方面”。所谓的“方面”，简单说，就是将那些与业务无关，却为业务模块所公共调用的逻辑或责任封装起来，以便减少系统的代码重复，降低模块之间的耦合度。 

AOP技术将软件系统分为两个部分： **核心关注点和横切关注点**。业务处理的主要流程是核心关注点，与之关系不大的部分是横切关注点。横切关注点的一个特点是：经常出现在核心关注点的周边，并且各处都基本类似。例如权限认证、日志和事务处理。AOP的作用在于分离系统中的各种关注点，将核心关注点和横切关注点分离开来。

实现AOP技术主要分为两类: **动态代理技术**， 利用截取消息的方式，对该消息进行装饰，以取代原有对象执行的行为；**静态织入**方式，引入特定的语法创建“Aspect”,从而使得编译器可以在编译期间织入有关“Aspect”的代码。

## AOP用来封装横切关注点，具体可以在下面的场景中使用:

1. Authentication 权限
2. Caching 缓存
3. Context passing 内容传递
4. Error handling 错误处理
5. Lazy loading　懒加载
6. Debugging　　调试
7. logging, tracing, profiling and monitoring　记录跟踪　优化　校准
8. Performance optimization　性能优化
9. Persistence　　持久化
10. Resource pooling　资源池
11. Synchronization　同步
12. Transactions 事务

## AOP的相关概念

1. 方面（Aspect）: 一个关注点的模块化，这个关注点实现可能横切多个对象。事务管理JavaEE应用中一个很好的横切关注点的例子。 在Spring中，Aspect用Advisor或拦截器实现。
2. 连接点（Joinpoint）: 程序执行过程中明确的点，如方法调用或特定的异常被抛出
3. 通知（Advisor）：在特定的连接点，AOP框架执行的动作。各种类型的通知包括“around”，“before”和“throw”通知。通知类型将在下面讨论。许多AOP框架包括Spring都是以拦截器做通知模型，维护一个“围绕”连接点的拦截器链。Spring中定义了四个“Advice”： `BeforeAdvice, AfterAdvice, ThrowAdvice 和 DynamicIntroductionAdvice`。
4. 切入点（Pointcut）: 指定一个通知将被引发的一系列连接点的集合。AOP框架必须允许开发者指定切入点。例如，使用正则表达式。Spring定义了Pointcut接口，用来组合`Methodmatcher`和`ClassFilter`，可以通过名字很清楚的理解，`MethodMatcher`用来检测目标类的方法是否可以被应用此通知，`ClassFilter`用来检查`Pointcurt`是否应该应用到目标类上。
5. 引入（Introduction）：添加方法或字段到被通知的类。Spring允许引入新的接口到任何被通知的对象。例如，可以使用一个引入使任何对象实现`IsModified`接口来简化缓存。Spring中要使用`Introduction`,可以通过`DelegatingIntroductionInterceptor`来实现通知，通过`DefaultIntroductionAdvisor`来配置`Advice`和代理类要实现的接口。
6. 目标对象（Target Object）: 包括连接点的对象。也被称作被通知或被代理对象，POJO。
7. AOP代理（AOP Proxy）： AOP框架代理的对象，包含通知。在Spring中，AOP代理可以是JDK动态代理或CGLIB代理。
8. 织入（Weaving）： 组装方面来创建一个被通知对象。这可以在编译时完成（例如AspectJ编译器），也可以在运行时完成。Spring和其他纯Java AOP框架一样，在运行时织入。