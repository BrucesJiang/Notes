# SpringMVC配置和执行流程
本文主要介绍Spring MVC应用的开发和配置步骤以及总结相应的执行流程。

## 简明开发配置步骤
1. 在`web.xml`文件中定义前端控制器`DispatcherServlet`来拦截用户请求。由于Web应用是基于请求/响应框架的应用。所以所有的MVC Web框架都需要在`web.xml`中配置该框架的核心`Servlet`或`Filter`，这样才可以让框架介入Web应用中。

2. 如果以POST方法提交请求，则定义包含表单的数据的JSP页面，如果仅仅是GET请求，则无需定义。
3. 定义处理用户请求的`Handle`类，可以实现`Controller`接口或使用`@Controller`注解。前端控制器`DispatcherServlet`就是MVC中的C，该控制器负责接收请求，并将请求分发给对应的`Handle`,即实现`Controller`接口的Java类；该Java类负责调用后台业务逻辑代码来处理请求。
4. 配置`Handle`。 Java领域的绝大部分MVC框架使用`xml`文件来进行配置管理。也就是配置哪个请求用哪个`Controller`进行处理，从而让前端控制器根据该配置创建合适的`Controller`
实例，并调用该`Controller`的业务逻辑方法。例如`xml`配置
```xml
<!-- 配置Handler, 映射'/hello'请求 -->
<ben name="/hello" class="org.controller.HelloController"/>
```
或者使用注解配置
```java
@Controller
public class HelloController {
    @RequestMapping(value="/hello")
    public ModelAndView hello(){
        ...
    }
}
```
5. 编写视图资源。

**某些解释：**
MVC框架底层机制：前端`Servlet`接收到用户请求后，通常会对请求做简单处理，例如解析、参数封装等，然后通过反射机制创建`Controller`实例，并调用`Controller`指定的方法（实现`Controller`接口的是`handleRequest`方法，而且使用基于注解的控制器可以是任意方法）来处理用户请求。当`Servlet`拦截用户请求后，有两种方案匹配处理请求的`Controller`实例。
1. `xml配置文件` -- 在`xml`配置文件中描述`hell`请求对应使用的`HelloController`类。
2. `利用注解` -- 使用注解`@Controller`描述一个类，并使用注解`@RequestMapping(value="/hello")`描述`hello`请求对应的方法。

## Spring MVC的执行流程
1. 用户向服务器发送请求，请求被前端服务器`DispatcherServlet`截获
2. `DispatcherServlet`对请求URL（统一资源定位符）进行解析，得到URI（请求资源标识符）。然后根据URI调用`HandlerMapping`获得该`Handler`配置的所有相关对象，包括`Handler`对象以及`Handler`对象对应的拦截器，这些对象会被封装到一个`HandlerExecutionChain`对象中返回。
3. `DispatcherServlet`根据获得的 `Handler`选择一个合适的`HandlerAdapter`处理请求。
4. 提取请求中的模型数据，开始执行`Handler(Controller)`。在填充`Handler`的入口参数过程中根据配置，Spring将做一些额外的工作。
 - **消息转换** 将请求消息（如，JSON，XML等格式数据）转换成一个对象，将对象转换成指定的响应消息
 - **数据转换**  将请求消息进行数据转换，如String转换成 Integer或Double等
 - **数据格式转化** 对请求消息进行数据格式化，将字符串转成格式化数字或格式化日期
 - **数据验证** 验证数据的有效性（长度格式等），验证结果保存到 `BindingRequest`或`Error`中。

5. `Handler`执行完成后，向`DispatcherServlet`返回一个`ModelAndView`对象，`ModelAndView`中应该包含该视图名和模型
6. 根据返回的`ModelAndView`对象，选择一个合适的`ViewResolver`（视图解析器）返回给`DispatcherServlet`。
7. `ViewResolver`结合`Model`和`View`来渲染视图
8. 将视图渲染结果返回给客户端
