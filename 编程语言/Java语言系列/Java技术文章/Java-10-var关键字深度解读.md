# Java-10-var关键字深度解读
本文翻译自:

英文原文：https://blog.codefx.org/java/java-10-var-type-inference/

北京时间2018年3月21日，Java 10如约而至。虽然这一版本带来的特性并不是非常多，但其中有一项仍然成为大家关注的热点，它就是局部变量类型推断（JEP 286）。JEP 286引入了var，用于声明局部变量，例如：

```java
var users = new ArrayList<User>();
```

事情就是这么简单。不过，这篇文章将会讨论更多有关var的内容，比如什么时候可以用var、什么时候不能用var、var对可读性的影响，以及为什么没有使用val。

## 使用var代替类型声明
作为Java开发者，在声明一个变量时，我们总是习惯了敲打两次变量类型，第一次用于声明变量类型，第二次用于构造函数，比如：
```java
URL codefx = new URL("http://codefx.org")
```

我们也经常声明一种变量，它只会被使用一次，而且是用在下一行代码中，比如：
```java
URL codefx = new URL("http://codefx.org")
URLConnection connection = codefx.openConnection();
Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
```
这样也不算太糟糕，就是有点啰嗦。尽管IDE可以帮我们自动完成这些代码，但当变量总是跳来跳去的时候，可读性还是会受到影响，因为变量类型的名称由各种不同长度的字符组成。而且，有时候开发人员会尽力避免声明中间变量，因为太多的类型声明只会分散注意力，不会带来额外的好处。

从Java 10开始，开发人员可以使用var让编译器自己去推断类型：
```java
var codefx = new URL("http://codefx.org");
var connection = codefx.openConnection();
var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
```
在处理var时，编译器先是查看表达式右边部分，也就是所谓的构造器，并将它作为变量的类型，然后将该类型写入字节码当中。

这样可以少敲几个字，但更重要的是，它避免了信息冗余，而且对齐了变量名，更容易阅读。当然，这也需要付出一点代价：有些变量，比如例子当中的connection，就无法立即知道它是什么类型的。虽说IDE可以辅助显示出这些变量的类型，但在其他场景下可能就不行了，比如在代码评审的时候。

另外，你不需要担心变量名或方法名会与var发生冲突，因为var实际上并不是一个关键字，而是一个类型名，只有在编译器需要知道类型的地方才需要用到它。除此之外，它就是一个普通合法的标识符。也就是说，除了不能用它作为类名，其他的都可以，但极少人会用它作为类名。

局部变量类型推断是一个非常直观的特性，不过你可能会想：

- 这到底是Java还是JavaScript？
- 可以在哪些地方使用var？
- var会影响可读性吗？
- 为什么没有val或let？

## 不，这不是JavaScript
首先我要说明的是，var并不会改变Java是一门静态类型语言的事实。编译器负责推断出类型，并把结果写入字节码文件，就好像是开发人员自己敲入类型一样。

下面是使用IntelliJ（实际上是Fernflower的反编译器）反编译器反编译出的代码：
```java
URL codefx = new URL("http://codefx.org");
URLConnection connection = codefx.openConnection();
BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
```
从代码来看，就好像之前已经声明了这些类型一样。事实上，这一特性只发生在编译阶段，与运行时无关，所以对运行时的性能不会产生任何影响。所以请放心，这不是JavaScript。

如果你仍然担心不显式声明类型会让代码变得更糟糕，那么我倒要问你了，你在使用lambda表达式的时候会声明参数的类型吗？
```java
rhetoricalQuestion.answer(yes -> "see my point?");
```
哪些地方可以使用var（或哪些地方不能使用var）
JEP 286的标题“局部变量类型推断”就已经暗示了哪些地方可以使用var：局部变量。更准确地说，是那些带有构造器的局部变量声明。但像这样的就不行了：
```java
// 不行
var foo;
foo = "Foo";
```

必须写成：
```java
var foo = "Foo";
```
除此之外，var也不能用在“多元表达式”中，如lambda和方法引用：

```java
// 这些都不行
var ints = {0, 1, 2};
var appendSpace = a -> a + " ";
var compareString = String::compareTo
```
除了局部变量，for循环是唯一可以使用var的地方：

```java
var numbers = List.of("a", "b", "c");
for (var nr : numbers)
  System.out.print(nr + " ");
for (var i = 0; i < numbers.size(); i++)
  System.out.print(numbers.get(i) + " ");
```
也就是说，字段、方法签名和catch代码块仍然需要显式声明类型。
```java
// 这样也是不行的
private var getFoo() {
  return "foo";
}
```
## 避免“Action At A Distance”错误
将var限定在局部变量上并非技术方面的局限，而是设计上的决定。确实，如果能够像下面这样岂不更好？
```java
// 编译器推断出类型List<User>
var users = new ArrayList<User>();
// 这样就不行了，会出现编译错误
users = new LinkedList<>();
```
按照预期，编译器应该能够找出最具体的那个类型，但实际上它不会。JDK团队想要避免“Action At A Distance”错误（AAD），也就是说，他们希望在某处修改了代码不会影响到其他很“远”的地方。比如：

```java
// id被推推为`int`
var id = 123;
if (id < 100) {
  // 此处省略了很长的代码
  // 调用了其他类的方法
} else {
  // 此处也省略了很长的代码
}
```
现在，我们加入一行：
```java
id = "124"
```
这样会发生什么？if代码块会抛出一个错误，因为id变成了字符串类型，所有不能使用小于号进行比较操作。这个错误距离代码修改的地方很“远”，而其根源就是因为对一个变量重新赋值。

这么看来，将类型推断限定在带有构造器的局部变量声明上是有它的道理的。

## 为什么不推断字段和方法的类型？
字段和方法的作用域比局部变量大得多，所以更有可能出现AAD错误。在最糟糕的情况下，修改一个方法的参数类型可能导致二进制文件的不兼容和运行时错误。

因为非private的字段和方法是类契约的一部分，它们的类型不能通过推断来获得。不过，private的字段和方法似乎可以使用类型推断，但问题是这样会让这个特性看起来非常奇怪。

局部变量属于实现细节，通常不会在很“远”的地方引用这些变量，所以就没有必要严格、显式和啰嗦地给它们声明类型了。

## 为什么要使用var？
相比其他年轻的编程语言，Java代码的啰嗦是开发人员最大的痛点之一，也是饱受Java开发人员诟病的一个地方。为此，Amber项目开发了var，旨在“开发出一些小的Java语言特性，以便提高效率”，其目标是降低Java代码编写和阅读的繁琐程度。

局部变量类型推断正好迎合了这一目标。在编写代码时，声明变量的方式更简单了，尽管这类代码有大半可以使用IDE生成，或者使用IDE的重构功能进行修改。

除了让变量声明变得更简单，修改起来也很容易。这话怎么说？有些变量的类型真的很难看，比如那些带有泛型的企业级类名：
```java
InternationalCustomerOrderProcessor<AnonymousCustomer, SimpleOrder<Book>> orderProcessor = createInternationalOrderProcessor(customer, order);
```
因为类型名称太长了，结果把变量名推到了代码的右边。如果限定了每行只能容纳150个字符，那么变量名还有可能被推到下一行显示。这些对于可读性来说都是一种伤害。
```java
var orderProcessor = createInternationalOrderProcessor(customer, order);
```
使用var就显得不那么累赘了，一眼就能看到头。

总之，使用var的意义不在于减少字符数量，而是为了不那么啰嗦和累赘。

## 对可读性的影响
现在让我们来讲讲可读性。确实，类型的缺失会让事情变得更糟糕，不是吗？一般来说，确实是的。在阅读代码时，类型是很重要的一个因素。尽管IDE可以帮助显示出推断的类型，但如果这些类型直接显示在代码中看起来不是更方便吗？

这是var在可读性方面的一个不足，不过，它却带来了另一个优势，那就是变量名对齐：
```java
// 显式类型
No no = new No();
AmountIncrease<BigDecimal> more = new BigDecimalAmountIncrease();
HorizontalConnection<LinePosition, LinePosition> jumping =
  new HorizontalLinePositionConnection();
Variable variable = new Constant(5);
List<String> names = List.of("Max", "Maria");
 
// 推断类型
var no = new No();
var more = new BigDecimalAmountIncrease();
var jumping = new HorizontalLinePositionConnection();
var variable = new Constant(5);
var names = List.of("Max", "Maria");
```
虽说类型名称很重要，但好的变量名也是有过之而无不及。类型用于描述Java生态系统（JDK的类）、一般场景（类库或框架）或业务领域（应用程序）的一般性概念，所以类型一般会有通用的名字。而变量名处在很小的上下文中，它们的名字应该要更精确一些。

这种可读性方面的改进可能会导致出现更多带有构造器的局部变量声明，因为这样在编写代码和阅读代码时会更加方便。

## 为什么没有使用val/const/let？
其他很多使用了var的编程语言也会为不可变变量提供一个额外的关键字，比如val、const或let。但Java 10没有使用这些关键字，所以我们必须使用final var来声明不可变变量。究其原因，可能是因为：

1. 虽说不变性很重要，但对于局部变量来说，这种重要性程度没有那么高。
2. Java 8引入了“隐式”final的概念，所以在我们看来，局部变量就已经是不可变的。
3. 大部分人同意使用var（74%的人强烈同意，12%的人基本同意），而对var/val和var/let的反馈则显得有点含糊不清。
我同意前面两点，至于第三点只能勉强接受，但对结果还是感到有点失望。或许等到了有一天，我们不得不使用final var的时候，是不是可以考虑使用val或let？

## 总结
在声明局部变量时，可以使用var代替具体的类名或接口名，让编译器自己去推断变量的类型。当然，只有在声明并且立即初始化变量的情况下才能使用var。for循环中的下标也可以使用var来声明。编译器会把推断出来的类型写入字节码，不影响运行时。Java仍然是一门静态类型的语言。

除了局部变量，var不能被用于字段或方法上，这样做是为了避免AAD错误。

虽说var有可能让代码变得更糟，但作为Java开发者，应该尝试在变量声明和嵌套表达式或链式表达式之间做出权衡，写出可读性更高的代码。

