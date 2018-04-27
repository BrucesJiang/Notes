# Servlet是如何工作的？Servlet 如何实例化、共享变量、并进行多线程处理？
本文来自StackOverFlow的问答，讨论了JavaServlet的工作机制。Servlet如何实例化、共享变量、并行多线程处理。

原文如下：

## Question
Suppose, I have a webserver which holds numerous servlets. For information passing among those servlets I am setting session and instance variables.

Now, if 2 or more users send request to this server then what happens to the session variables? Will they all be common for all the users or they will be different for each user. If they are different, then how was the server able to differentiate between different users?

One more similar question, if there are n users accessing a particular servlet, then this servlet gets instantiated only the first time the first user accessed it or does it get instantiated for all the users separately? In other words, what happens to the instance variables?

## Answer(BalusC)
### ServletContext
When the servlet container (like [Apache Tomcat](http://tomcat.apache.org/)) starts up, it will deploy and load all its web applications. When a web application is loaded, the servlet container creates the [ServletContext](http://docs.oracle.com/javaee/7/api/javax/servlet/ServletContext.html) once and keeps it in the server's memory. The web app's web.xml file is parsed, and each `<servlet>, <filter>` and `<listener>` found (or each class annotated with `@WebServlet, @WebFilter` and `@WebListener` respectively) is instantiated once and kept in the server's memory as well. For each instantiated filter, its `init()` method is invoked with a new [FilterConfig](https://docs.oracle.com/javaee/7/api/javax/servlet/FilterConfig.html).

When the `servlet` container shuts down, it unloads all web applications, invokes the `destroy()` method of all its initialized servlets and filters, and all `ServletContext, Servlet, Filter` and `Listener` instances are trashed.

When a `Servlet` has a `<servlet><load-on-startup>` or `@WebServlet(loadOnStartup)` value greater than `0`, its `init()` method is also invoked during startup with a new [ServletConfig](https://docs.oracle.com/javaee/7/api/javax/servlet/ServletConfig.html). Those servlets are initialized in the same order specified by that value (1 -> 1st, 2 -> 2nd, etc). If the same value is specified for more than one servlet, then each of those servlets is loaded in the order they appear in the `web.xml`, or `@WebServlet` classloading. In the event the "load-on-startup" value is absent, the `init()` method will be invoked whenever the HTTP request hits that servlet for the very first time.

#### HttpServletRequest and HttpServletResponse
The servlet container is attached to a web server that listens for HTTP requests on a certain port number (port 8080 is usually used during development and port 80 in production). When a client (user with a web browser) sends an HTTP request, the servlet container creates new [HttpServletRequest](http://docs.oracle.com/javaee/7/api/javax/servlet/http/HttpServletRequest.html) and [HttpServletResponse](http://docs.oracle.com/javaee/7/api/javax/servlet/http/HttpServletResponse.html) objects and passes them through any defined `Filter` chain and, eventually, the `Servlet` instance.

In the case of [filters](https://stackoverflow.com/tags/servlet-filters/info), the `doFilter()` method is invoked. When its code calls `chain.doFilter(request, response)`, the request and response continue on to the next filter, or hit the servlet if there are no remaining filters.

In the case of [servlets](https://stackoverflow.com/tags/servlets/info), the `service()` method is invoked. By default, this method determines which one of the `doXxx()` methods to invoke based off of `request.getMethod()`. If the determined method is absent from the servlet, then an HTTP 405 error is returned in the response.

The request object provides access to all of the information about the HTTP request, such as its headers and body. The response object provides the ability to control and send the HTTP response the way you want by, for instance, allowing you to set the headers and the body (usually with generated HTML content from a JSP file). When the HTTP response is committed and finished, both the request and response objects are recycled and made for reuse.

#### HttpSession
When a client visits the webapp for the first time and/or the [HttpSession](http://docs.oracle.com/javaee/7/api/javax/servlet/http/HttpSession.html) is obtained for the first time via `request.getSession()`, the servlet container creates a new `HttpSession` object, generates a long and unique ID (which you can get by `session.getId()`), and store it in the server's memory. The servlet container also sets a [Cookie](http://docs.oracle.com/javaee/7/api/javax/servlet/http/Cookie.html) in the Set-Cookie header of the HTTP response with `JSESSIONID` as its name and the unique session ID as its value.

As per the [HTTP cookie specification](http://www.faqs.org/rfcs/rfc2965.html) (a contract a decent web browser and web server have to adhere to), the client (the web browser) is required to send this cookie back in subsequent requests in the `Cookie` header for as long as the cookie is valid (i.e. the unique ID must refer to an unexpired session and the domain and path are correct). Using your browser's built-in HTTP traffic monitor, you can verify that the cookie is valid (press F12 in Chrome / Firefox 23+ / IE9+, and check the Net/Network tab). The servlet container will check the Cookie header of every incoming HTTP request for the presence of the cookie with the name `JSESSIONID` and use its value (the session ID) to get the associated HttpSession from server's memory.

The `HttpSession` stays alive until it has not been used for more than the timeout value specified in `<session-timeout>`, a setting in `web.xml`. The timeout value defaults to 30 minutes. So, when the client doesn't visit the web app for longer than the time specified, the servlet container trashes the session. Every subsequent request, even with the cookie specified, will not have access to the same session anymore; the servlet container will create a new session.

On the client side, the session cookie stays alive for as long as the browser instance is running. So, if the client closes the browser instance (all tabs/windows), then the session is trashed on the client's side. In a new browser instance, the cookie associated with the session wouldn't exist, so it would no longer be sent. This causes an entirely new HTTPSession to be created, with an entirely new session cookie begin used.

#### In a nutshell
- The `ServletContext` lives for as long as the web app lives. It is shared among all requests in all sessions.
- The `HttpSession` lives for as long as the client is interacting with the web app with the same browser instance, and the session hasn't timed out at the server side. It is shared among all requests in the same session.
- The `HttpServletRequest` and `HttpServletResponse` live from the time the servlet receives an HTTP request from the client, until the complete response (the web page) has arrived. It is not shared elsewhere.
- All `Servlet`, `Filter` and `Listener` instances live as long as the web app lives. They are shared among all requests in all sessions.
- Any `attribute` that is defined in `ServletContext`, `HttpServletRequest` and `HttpSession` will live as long as the object in question lives. The object itself represents the "scope" in bean management frameworks such as JSF, CDI, Spring, etc. Those frameworks store their scoped beans as an `attribute` of its closest matching scope.

#### Thread Safety
That said, your major concern is possibly thread safety. You should now know that servlets and filters are shared among all requests. That's the nice thing of Java, it's multithreaded and different threads (read: HTTP requests) can make use of the same instance. It would otherwise be too expensive to recreate, `init()` and `destroy()` them for every single request.

You should also realize that you should never assign any request or session scoped data as an instance variable of a servlet or filter. It will be shared among all other requests in other sessions. That's not thread-safe! The below example illustrates this:
```java
public class ExampleServlet extends HttpServlet {

    private Object thisIsNOTThreadSafe;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object thisIsThreadSafe;

        thisIsNOTThreadSafe = request.getParameter("foo"); // BAD!! Shared among all requests!
        thisIsThreadSafe = request.getParameter("foo"); // OK, this is thread safe.
    } 
}
```
See also:
- [What is the difference between JSF, Servlet and JSP?](https://stackoverflow.com/questions/2095397/what-is-the-difference-between-jsf-servlet-and-jsp)
- [Best option for Session management in Java](https://stackoverflow.com/questions/1700390/best-option-for-session-management-in-java)
- [Difference between / and /* in servlet mapping url pattern](https://stackoverflow.com/questions/4140448/difference-between-and-in-servlet-mapping-url-pattern)
- [doGet and doPost in Servlets](https://stackoverflow.com/questions/2349633/doget-and-dopost-in-servlets)
- [Servlet seems to handle multiple concurrent browser requests synchronously](https://stackoverflow.com/questions/8011138/servlet-seems-to-handle-multiple-concurrent-requests-synchronously/)
- [Why Servlets are not thread Safe?](https://stackoverflow.com/questions/9555842/why-servlets-are-not-thread-safe/)