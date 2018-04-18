# Spring MVC常用注解概述
本文主要介绍了Spring MVC开发过程中常用的一些注解。
1. @Controller
2. @RequestMapping
3. @RequestParam
4. @PathVariable
5. @RequestHeader
6. @CookieValue
7. @SessionAttributes
8. @ModelAttribute

## @Controller注解
`org.springframework.stereotype.Controller`注解类型用于指示某个Java类的实例是一个控制器。使用`@Controller`注解的类不需要继承特定的父类或实现特定的接口，相比于实现`Controller`接口更加简单。更重要的是，`Controller`接口的实现类只能处理一个单一请求动作，而`@Controller`注解的控制器可以同时支持处理多个请求活动，更加灵活。

`@Controller`用于标记一个控制器类。Spring扫描查找应用程序中所有基于注解的控制器类。分发处理器类会扫描使用了该注解的类的方法，并检测该方法是否使用了`@RequestMapping`注解。使用`@RequestMapping`注解的控制器方法才是真正处理请求的处理器。 为了保证Spring能够找到控制器，需要做两件事：
1. 在Spring MVC的配置文件的头文件中引入`spring-context` Schema
2. 使用`<context:component-scan/>`元素，该元素的功能是：启动包扫描功能，将被`@Controller`、`@Service`、`@Reposity`, `@Component`等注解修饰的类注册成为IOC容器的Bean。其中`base-package`属性指定了需要扫描的类包，类包及其递归子包中所有的类都会被处理。例如 `<context:component-scan base-package="org.controller"/>`


