 # 详解前端控制器DispatcherServlet
##摘要
本文意在解释前端控制器`DispatcherServlet`基本配置方式、通过源码分析其初始化组件以及组建的加载方式。


Java EE的许多MVC框架中都包含一个用于调度控制的Servlet。 Spring MVC 也提供了一个名为`org.springframework.web.servlet.DispatcherServlet`的Servlet充当前端控制器，所有的请求驱动都围绕这个`DispatcherServlet`来分派请求。

## DispatcherServlet的配置
DispatcherServlet是一个Servlet（它继承自HttpServlet基类）。因此，使用时需要把它配置在Web应用的部署描述文件`web.xml`中，简单的配置信息如下：

```xml
<servlet>
    <!-- Servlet的名称 -->
    <servlet-name>springmvc</servlet-name>
    <!-- Servlet 对应的Java类 -->
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!-- 当前Servlet的参数信息 -->
    <init-param>
      <!-- contextConfigLocation 是参数名称，该参数的值包含SpringMVC的配置文件路径-->
      <param-name>contextConfigLocation</param-name>
      <param-value>/WEB-INF/springmvc-config.xml</param-value>
    </init-param>
    <!-- 加载Web应用启动时立刻加载Servlet -->
    <load-on-startup>1</load-on-startup>
</servlet>

<!-- Servlet 映射声明 -->
<servlet-mapping>
    <!-- 请求对应的Servlet名称 -->
    <servlet-name>springmvc</servlet-name>
    <!-- 监听当前域的所有请求 -->
    <url-pattern>/</url-pattern>
</servlet-mapping>
```
以上配置信息为Java EE Servlet的标准配置。配置了一个`DispatcherServlet`，该`servlet`在Web应用程序启动时立即加载，`DispatcherServlet`加载时需要一个`Spring MVC`配置文件，默认情况下，应用会去应用程序文件夹的`WEB-INF`下查找相应的`.xml`文件。


## 详解DispatcherServlet

```java
protected void initStrategies(ApplicationContext context) {
    initMultipartResolver(context); //初始化文件上传解析器
    initLocaleResolver(context); //初始化本地解析器
    initThemeResolver(context); //初始化主题解析器
    initHandlerMappings(context); //初始化处理器映射器，将请求映射到处理器
    initHandlerAdapters(context); //初始化处理器适配器
    initHandlerExceptionResolvers(context); //初始化处理器异常解析器，如果执行过程中遇到异常将交给HandlerExceptionResolver来解析
    initRequestToViewNameTranslator(context); //初始化请求到视图名称解析器
    initViewResolvers(context); //初始化视图解析器，通过ViewResolver解析逻辑视图名到具体视图实现
    initFlashMapManager(context); //初始化flash映射管理器
}

```

`initStrategies`方法将在`WebApplicationContext`初始化后自动执行，自动扫描上下文的`Bean`,根据名称或类型匹配的机制查找自定义的组建，如果没有找到则会配装一套Spring默认组件。在`org.springframework.web.servlet`路径下有一个`DispatcherServlet.properties`配置文件，该文件指定了`DispatcherServlet`所使用的默认组件。

```
# Default implementation classes for DispatcherServlet's strategy interfaces.
# Used as fallback when no matching beans are found in the DispatcherServlet context.
# Not meant to be customized by application developers.

# 本地化解析器
org.springframework.web.servlet.LocaleResolver=org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver

# 主题解析器
org.springframework.web.servlet.ThemeResolver=org.springframework.web.servlet.theme.FixedThemeResolver

# 处理器映射器（2个）
org.springframework.web.servlet.HandlerMapping=org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping,\
    org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

# 处理器适配器（3个）
org.springframework.web.servlet.HandlerAdapter=org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter,\
    org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter,\
    org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

# 异常处理器（3个）
org.springframework.web.servlet.HandlerExceptionResolver=org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver,\
    org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver,\
    org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver

# 视图名称解析器
org.springframework.web.servlet.RequestToViewNameTranslator=org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator

# 视图 解析器
org.springframework.web.servlet.ViewResolver=org.springframework.web.servlet.view.InternalResourceViewResolver

# FlashMap 映射管理器
org.springframework.web.servlet.FlashMapManager=org.springframework.web.servlet.support.SessionFlashMapManager

```

如果开发者需要使用自定义类型的组件，则只需要在Spring配置文件中配置自定义的Bean组件即可。SpringMVC如果发现上下文中有用户自定义的组建，就不会使用默认组建。

以下是`DispatcherServlet`装配每种组件的细节：

- **本地化解析器** 只允许一个实例。
  1. 查找名为`localeResolver`、类型为`LocaleResolver`的Bean作为该类型组件
  2. 如果没有找到，则使用默认的实现类`AcceptHeaderLocaleResolver`作为该类型组建 
- **主题解析器** 只允许一个实例
  1. 查找名为`themeResolver`、类型为`ThemeResolver`的`Bean`作为该类型的组件
  2. 如果没有找到，则使用默认的实现类`FixedThemeResolver`作为该类型组件
- **处理器映射** 允许多个实例
  1. 如果`detectAllHandlerMappings`的属性为`true`（默认为`true`），则根据类型匹配机制查找上下文以及Spring容器中所有类型为`HandlerMapping`的`Bean`，将它们作为该类型组件
  2. 如果 `detectAllHandlerMappings`的属性为`false`，则查找名为`handlerMapping`、类型为`HandlerMapping`的`Bean`作为该类型的组件
  3. 如果通过以上两种方式都没有找到，则使用`BeanNameUrlHandlerMapping`实现类创建该类型的组件
- **处理器适配器** 允许多个实例
  1. 如果`detectAllHandlerAdapters`的属性为`true`（默认为`true`），则根据类型匹配机制查找上下文以及Spring容器中所有类型为`HandlerAdapter`的`Bean`，将它们作为该类型组件。
  2. 如果`detectAllHandlerAdapters`的属性为`false`，则查找名为`handlerAdapter`、类型为`HandlerAdapter`的`Bean`作为该类型组件。
  3. 如果通过以上两种方式都没有找到，则使用`DispatcherServlet.properties`配置文件中指定的三个实现类分别创建要给适配器，并将其添加到适配器列表中。
- **处理器异常解析器** 允许多个实例
  1. 如果`detectAllHandlerExceptionResolvers`的属性为`true`（默认为`true`），则根据类型匹配机制查找上下文以及Spring容器中所有类型为`HandlerExceptionResolver`的`Bean`，将它们作为该类型组件。
  2. 如果`detectAllHandlerExceptionResolver`的属性为`false`，则查找名为`handlerExdeptionResolver`、类型为`HandlerExceptionResolver`的`Bean`作为该类型的组件。
  3. 如果通过以上两种方式都没有找到，则查找`DispatcherServlet.properties`配置文件中定义的默认实现类，注意，该文件中没有对应处理器异常解析器的默认实现类，用户可以自定义处理器异常解析器的实现类，将之添加到`DispatcherServlet.properties`配置文件中。
- **视图名称解析器** 只允许一个实例
  1. 查找名为`viewNameTranslator`、类型为`RequestToViewNameTranslator`的`Bean`作为该类型组件。
  2. 如果没有找到，则使用默认的实现类`DefaultRequestToViewNameTranslator`作为该类型的组件。
- **视图解析器** 允许多个实例
  1. 如果`detectAllViewResolvers`的属性为`true`（默认为`true`），则根据类型匹配机制查找上下文以及Spring容器中所有类型为`ViewResolver`的`Bean`，将它们作为该类型组件。
  2. 如果`detectAllViewResolvers`的属性为`false`，则查找名为`viewResolver`、类型为`ViewResolver`的`Bean`作为该类型组件。
  3. 如果通过以上两种方式都没有找到，则查找`DispatcherServlet.properties`配置文件中定义的默认实现类 `InternalResourceViewResolver`作为该类型的组件。
- **文件上传解析器** 只允许一个实例
  1. 查找名为`multipartResolver`、类型为`MultipartResolver`的`Bean`作为该类型组件。
  2. 如果用户没有在上下文中显式定义`MultipartResolver`类型的组建，则`DispathcerServlet`将不会加载该类型的组件。
- **FlashMap**映射管理
  1. 查找名为`FlashMapManager`、类型为`SessionFlashMapManager`的`Bean`作为该类型组件，用于管理`FlashMap`,即默认数据存储在`HttpSession`中。

如果同一个类型的组件存在多个，那么它们之间的优先级确定方式如下：这些组件都实现了`org.springframework.core.Ordered`接口，所以可以通过`Order`属性确定优先级的顺序，值越小的优先级越高。


