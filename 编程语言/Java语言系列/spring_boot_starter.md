
Spring Boot Starter实在SpringBoot组件中提出的一种概念。本质上来讲，就是对依赖组件的一种合成。

传统而言，引入一种新的依赖，首先需要考虑的是如何使用。例如，在WEB项目中不可缺少的依赖JPA等。
1. 依赖其间接依赖，例如JDBC
2. 在引入JPA依赖
3. 配置XML
4. ...

最重要的是每新建一个项目，都需要重复上述步骤。

starter 的主要目的就是为了解决上面的这些问题。

starter 的理念：starter 会把所有用到的依赖都给包含进来，避免了开发者自己去引入依赖所带来的麻烦。需要注意的是不同的 starter 是为了解决不同的依赖，所以它们内部的实现可能会有很大的差异，例如 jpa 的 starter 和 Redis 的 starter 可能实现就不一样，这是因为 starter 的本质在于 synthesize，这是一层在逻辑层面的抽象，也许这种理念有点类似于 Docker，因为它们都是在做一个 “包装” 的操作，如果你知道 Docker 是为了解决什么问题的，也许你可以用 Docker 和 starter 做一个类比。

starter 的实现：虽然不同的 starter 实现起来各有差异，但是他们基本上都会使用到两个相同的内容：ConfigurationProperties 和 AutoConfiguration。因为 Spring Boot 坚信 “约定大于配置” 这一理念，所以我们使用 ConfigurationProperties 来保存我们的配置，并且这些配置都可以有一个默认值，即在我们没有主动覆写原始配置的情况下，默认值就会生效，这在很多情况下是非常有用的。除此之外，starter 的 ConfigurationProperties 还使得所有的配置属性被聚集到一个文件中（一般在 resources 目录下的 application.properties），这样我们就告别了 Spring 项目中 XML 地狱。

如果你想要自己创建一个 starter，那么基本上包含以下几步

1. 创建一个 starter 项目，关于项目的命名你可以参考这里
2. 创建一个 ConfigurationProperties 用于保存你的配置信息（如果你的项目不使用配置信息则可以跳过这一步，不过这种情况非常少见）
3. 创建一个 AutoConfiguration，引用定义好的配置信息；在 AutoConfiguration 中实现所有 starter 应该完成的操作，并且把这个类加入 spring.factories 配置文件中进行声明
4. 打包项目，之后在一个 SpringBoot 项目中引入该项目依赖，然后就可以使用该 starter 了

Demo示例：
1. 首先创建一个项目，XML配置如下
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.bruce</groupId>
    <artifactId>lean-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>


    <!-- 自定义starter都应该继承自该依赖 -->
    <!-- 如果自定义starter本身需要继承其它的依赖，可以参考 https://stackoverflow.com/a/21318359 解决 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starters</artifactId>
        <version>1.5.2.RELEASE</version>
    </parent>
    <dependencies>
        <!-- 自定义starter依赖此jar包 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
    </dependencies>

</project>
```

创建默认配置 properties

```java
@ConfigurationProperties(prefix = "http")
public class HttpProperties {

    // 默认配置，如果application.properties中有配置，则会被覆盖
    private String url = "http://www.baidu.com";

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
```

创建服务

```java
public class HttpClient {

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String crawlingHtml() {
        try{
            URL url = new URL(getUrl());
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }
}
```
创建自动配置

```java
@Configuration
@EnableConfigurationProperties(HttpProperties.class)
public class HttpAutoConfiguration {
    @Resource
    private HttpProperties properties;

    // 在Spring上下文中创建一个对象
    @Bean
    @ConditionalOnMissingBean
    public HttpClient init() {
        HttpClient client = new HttpClient();

        String url = properties.getUrl();
        client.setUrl(url);
        return client;
    }
}

```
最后，我们在 resources 文件夹下新建目录 META-INF，在目录中新建 spring.factories 文件，并且在 spring.factories 中配置 AutoConfiguration：
```txt
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.bruce.starter.HttpAutoConfiguration
```

使用POM引入

```xml
<dependency>
    <groupId>org.bruce</groupId>
    <artifactId>lean-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
```

依赖服务注入

```java
@Service
public class HttpService {
    @Resource
    private HttpClient httpClient;

    public void crawler() {
        System.out.println("url:" + httpClient.getUrl());
        System.out.println("html:" + httpClient.crawlingHtml());
    }
}
```

在application.properties中自定义配置：
```xml
http.url=https://www.zhihu.com/
```






