---
title: Web开发基础-Servlet
toc: true
date: 2021-08-24 16:00:44
tags:
categories:
---





## 1、什么是servlet

**什么是Serlvet？**

Servlet其实就是一个**遵循Servlet开发的java类**。Serlvet是**由服务器调用的**，**运行在服务器端**。

我们编写java程序想要在网上实现 聊天、发帖、这样一些的交互功能，**普通的java技术是非常难完成的**。sun公司就提供了Serlvet这种技术供我们使用。

## 2、servlet生命周期

**Servlet生命周期可分为5个步骤**

1. **加载Servlet**。当Tomcat第一次访问Servlet的时候，**Tomcat会负责创建Servlet的实例**
2. **初始化**。当Servlet被实例化后，Tomcat会**调用init()方法初始化这个对象**
3. **处理服务**。当浏览器**访问Servlet**的时候，Servlet **会调用service()方法处理请求**
4. **销毁**。当Tomcat关闭时或者检测到Servlet要从Tomcat删除的时候会自动调用destroy()方法，**让该实例释放掉所占的资源**。一个Servlet如果长时间不被使用的话，也会被Tomcat自动销毁
5. **卸载**。当Servlet调用完destroy()方法后，等待垃圾回收。如果**有需要再次使用这个Servlet，会重新调用init()方法进行初始化操作**。

- 简单总结：**只要访问Servlet，service()就会被调用。init()只有第一次访问Servlet的时候才会被调用。 destroy()只有在Tomcat关闭的时候才会被调用。**

## 3、servlet调用流程

![image-20210824161820602](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210824161820602.png)

## 4、Servlet是单例的

### 4.1 为什么Servlet是单例的

**浏览器多次对Servlet的请求**，一般情况下，**服务器只创建一个Servlet对象**，也就是说，Servlet对象**一旦创建了**，就会**驻留在内存中，为后续的请求做服务，直到服务器关闭**。

### 4.2 每次访问请求对象和响应对象都是新的

对于**每次访问请求**，Servlet引擎都会**创建一个新的HttpServletRequest请求对象和一个新的HttpServletResponse响应对象**，然后将这两**个对象作为参数传递给它调用的Servlet的service()方法**，**service方法再根据请求方式分别调用doXXX方法**。

### 4.3 线程安全问题

当多个用户访问Servlet的时候，**服务器会为每个用户创建一个线程**。**当多个用户并发访问Servlet共享资源的时候就会出现线程安全问题**。

原则：

1. 如果一个**变量需要多个用户共享**，则应当在访问该变量的时候，**加同步机制synchronized (对象){}**
2. 如果一个变量**不需要共享**，则**直接在 doGet() 或者 doPost()定义**.这样不会存在线程安全问题

## 5、ServletConfig对象

### 5.1 ServletConfig对象有什么用？

> 通过此对象可以读取web.xml中配置的初始化参数。

现在问题来了，**为什么我们要把参数信息放到web.xml文件中呢**？我们可以直接在程序中都可以定义参数信息，**搞到web.xml文件中又有什么好处呢**？

好处就是：**能够让你的程序更加灵活**【更换需求，更改配置文件web.xml即可，程序代码不用改】

### 5.2 获取web.xml文件配置的参数信息

为Demo1这个Servlet配置一个参数，参数名是name，值是zhongfucheng

```xml
<?xml version="1.0" encoding="utf-8"?>

<servlet> 
  <servlet-name>Demo1</servlet-name>  
  <servlet-class>zhongfucheng.web.Demo1</servlet-class>  
  <init-param> 
    <param-name>name</param-name>  
    <param-value>zhongfucheng</param-value> 
  </init-param> 
</servlet>

<servlet-mapping> 
  <servlet-name>Demo1</servlet-name>  
  <url-pattern>/Demo1</url-pattern> 
</servlet-mapping>
```

在Servlet中获取ServletConfig对象，通过ServletConfig对象获取在web.xml文件配置的参数

```java
ServletConfig config = this.getServletConfig();
String name = config.getInitParameter("name");
```



## 6、ServletContext对象

### 6.1 什么是ServletContext对象？

当Tomcat启动的时候，就会创建一个ServletContext对象。它**代表着当前web站点**

### 6.2 ServletContext有什么用？

1. ServletContext既然代表着当前web站点，那么**所有Servlet都共享着一个ServletContext对象**，所以**Servlet之间可以通过ServletContext实现通讯**。
2. ServletConfig获取的是配置的是单个Servlet的参数信息，**ServletContext可以获取的是配置整个web站点的参数信息**
3. **利用ServletContext读取web站点的资源文件**
4. 实现Servlet的转发【用ServletContext转发不多，主要用request转发】

## 7、什么是会话技术

> 基本概念: 指用户开一个浏览器，**访问一个网站,只要不关闭该浏览器，不管该用户点击多少个超链接，访问多少资源，直到用户关闭浏览器，整个这个过程我们称为一次会话**.

### 7.1 什么是Cookie

> Cookie是由W3C组织提出，最早由netscape社区发展的一种机制

- 网页之间的**交互是通过HTTP协议传输数据的，**而Http协议是**无状态的协议**。无状态的协议是什么意思呢？**一旦数据提交完后，浏览器和服务器的连接就会关闭，再次交互的时候需要重新建立新的连接**。
- 服务器无法确认用户的信息，于是乎，W3C就提出了：**给每一个用户都发一个通行证，无论谁访问的时候都需要携带通行证，这样服务器就可以从通行证上确认用户的信息**。通行证就是Cookie

#### 7.11 Cookie的流程

浏览器访问服务器，**如果服务器需要记录该用户的状态，就使用response向浏览器发送一个Cookie，浏览器会把Cookie保存起来。当浏览器再次访问服务器的时候，浏览器会把请求的网址连同Cookie一同交给服务器**。

#### 7.1.2 Cookie API

- Cookie类用于创建一个Cookie对象
- response接口中定义了一个addCookie方法，它用于在其响应头中增加一个相应的Set-Cookie头字段
- request接口中定义了一个getCookies方法，它用于获取客户端提交的Cookie

**常用的Cookie方法：**

- public Cookie(String name,String value)
- setValue与getValue方法
- setMaxAge与getMaxAge方法
- setPath与getPath方法
- setDomain与getDomain方法
- getName方法

#### 7.1.3 cookie使用方式

```java
response.setContentType("text/html;charset=UTF-8");
Cookie cookie = new Cookie("username", "zhongfucheng");
cookie.setMaxAge(1000);
response.addCookie(cookie);
response.getWriter().write("我已经向浏览器发送了一个Cookie");
```

#### 7.1.4 Cookie不可跨域名性

- 很多人在初学的时候可能有一个疑问：在访问Servlet的时候浏览器**是不是把所有的Cookie都带过去给服务器**，**会不会修改了别的网站的Cookie**
- 答案是否定的。Cookie具有不可跨域名性。浏览器判断**一个网站是否能操作另一个网站的Cookie的依据是域名**。所以一般来说，**当我访问baidu的时候，浏览器只会把baidu颁发的Cookie带过去，而不会带上google的Cookie。**

#### 7.1.5 Cookie的有效期

**Cookie的有效期是通过setMaxAge()来设置的**。

- 如果MaxAge为**正数**，**浏览器会把Cookie写到硬盘中，只要还在MaxAge秒之前，登陆网站时该Cookie就有效**【不论关闭了浏览器还是电脑】
- 如果MaxAge为**负数**，**Cookie是临时性的，仅在本浏览器内有效**，关闭浏览器Cookie就失效了，Cookie不会写到硬盘中。Cookie默认值就是-1。这也就为什么在我第一个例子中，如果我没设置Cookie的有效期，在硬盘中就找不到对应的文件。
- 如果MaxAge为**0**，则表示**删除该Cookie**。Cookie机制没有提供删除Cookie对应的方法，把MaxAge设置为0等同于删除Cookie

#### 7.1.6 Cookie的域名

Cookie的**domain属性决定运行访问Cookie的域名。domain的值规定为“.域名”**

Cookie的隐私安全机制决定Cookie是不可跨域名的。也就是说www.baidu.com和www.google.com之间的Cookie是互不交接的。**即使是同一级域名，不同二级域名也不能交接**。



### 7.2 什么是Session

Session 是另一种记录浏览器状态的机制。不同的是Cookie保存在浏览器中，Session保存在服务器中。用户使用浏览器访问服务器的时候，服务器把用户的信息以某种的形式记录在服务器，这就是Session。

#### 7.2.1 为什么要使用Session技术？

**Session比Cookie使用方便，Session可以解决Cookie解决不了的事情【Session可以存储对象，Cookie只能存储字符串。】。**

#### 7.2.2 Session API

- long getCreationTime();【获取Session被创建时间】
- **String getId();【获取Session的id】**
- long getLastAccessedTime();【返回Session最后活跃的时间】
- ServletContext getServletContext();【获取ServletContext对象】
- **void setMaxInactiveInterval(int var1);【设置Session超时时间】**
- **int getMaxInactiveInterval();【获取Session超时时间】**
- **Object getAttribute(String var1);【获取Session属性**】
- Enumeration getAttributeNames();【获取Session所有的属性名】
- **void setAttribute(String var1, Object var2);【设置Session属性】**
- **void removeAttribute(String var1);【移除Session属性】**
- **void invalidate();【销毁该Session】**
- boolean isNew();【该Session是否为新的】

#### 7.2.3 session作为域对象

Session有着request和ServletContext类似的方法。其实**Session也是一个域对象**。Session作为一种记录浏览器状态的机制，**只要Session对象没有被销毁，Servlet之间就可以通过Session对象实现通讯**

一般来讲，当我们要存进的是**用户级别的数据就用Session**，那什么是用户级别呢？**只要浏览器不关闭，希望数据还在，就使用Session来保存**。

#### 7.2.4 session生命周期

**Session的生命周期和有效期**

- Session在用户**第一次访问服务器Servlet，jsp等动态资源就会被自动创建，Session对象保存在内存里**，这也就为什么上面的例子可以**直接使用request对象获取得到Session对象**。

- 如果访问HTML,IMAGE等静态资源Session不会被创建。

- Session生成后，只要用户继续访问，服务器就会更新Session的最后访问时间，无论**是否对Session进行读写，服务器都会认为Session活跃了一次**。

- 由于会有越来越多的用户访问服务器，因此Session也会越来越多。**为了防止内存溢出，服务器会把长时间没有活跃的Session从内存中删除，这个时间也就是Session的超时时间**。

- Session的超时时间默认是30分钟，有三种方式可以对Session的超时时间进行修改

  1、修改tomcat的web.xml

  2、修改项目的web.xml

  3、`httpSession.setMaxInactiveInterval(60);`

### 7.3 Session和Cookie的区别

- **从存储方式上比较**

- - Cookie只能存储字符串，如果要存储非ASCII字符串还要对其编码。
  - Session可以存储任何类型的数据，可以把Session看成是一个容器

- **从隐私安全上比较**

- - **Cookie存储在浏览器中，对客户端是可见的**。信息容易泄露出去。如果使用Cookie，最好将Cookie加密
  - **Session存储在服务器上，对客户端是透明的**。不存在敏感信息泄露问题。

- **从有效期上比较**

- - Cookie保存在硬盘中，只需要设置maxAge属性为比较大的正整数，即使关闭浏览器，Cookie还是存在的
  - **Session的保存在服务器中，设置maxInactiveInterval属性值来确定Session的有效期。并且Session依赖于名为JSESSIONID的Cookie，该Cookie默认的maxAge属性为-1。如果关闭了浏览器，该Session虽然没有从服务器中消亡，但也就失效了。**

- **从对服务器的负担比较**

- - Session是保存在服务器的，每个用户都会产生一个Session，如果是并发访问的用户非常多，是不能使用Session的，Session会消耗大量的内存。
  - Cookie是保存在客户端的。不占用服务器的资源。像baidu、Sina这样的大型网站，一般都是使用Cookie来进行会话跟踪。

- **从浏览器的支持上比较**

- - 如果浏览器禁用了Cookie，那么Cookie是无用的了！
  - 如果浏览器禁用了Cookie，Session可以通过URL地址重写来进行会话跟踪。

- **从跨域名上比较**

- - Cookie可以设置domain属性来实现跨域名
  - Session只在当前的域名内有效，不可跨域名



## 8、forward和redirect的区别

### 8.1 **实际发生位置不同，地址栏不同**

1、转发是发生在服务器的
2、转发是由服务器进行跳转的，细心的朋友会发现，在转发的时候，浏览器的地址栏是没有发生变化的，在我访问Servlet111的时候，即使跳转到了Servlet222的页面，浏览器的地址还是Servlet111的。也就是说浏览器是不知道该跳转的动作，转发是对浏览器透明的。通过上面的转发时序图我们也可以发现，实现转发只是一次的http请求，一次转发中request和response对象都是同一个。这也解释了，为什么可以使用request作为域对象进行Servlet之间的通讯。
3、重定向是发生在浏览器的 - 重定向是由浏览器进行跳转的，进行重定向跳转的时候，浏览器的地址会发生变化的。曾经介绍过：实现重定向的原理是由response的状态码和Location头组合而实现的。这是由浏览器进行的页面跳转实现重定向会发出两个http请求，request域对象是无效的，因为它不是同一个request对象

### 8.2 用法不同

很多人都搞不清楚转发和重定向的时候，资源地址究竟怎么写。有的时候要把应用名写上，有的时候不用把应用名写上。很容易把人搞晕。记住一个原则： **给服务器用的直接从资源名开始写，给浏览器用的要把应用名写上**

`request.getRequestDispatcher("/资源名 URI").forward(request,response)`
转发时"/"代表的是本应用程序的根目录【zhongfucheng】  
`response.send("/web应用/资源名 URI"); `
重定向时"/"代表的是webapps目录  

### 8.3 能够去往的URL的范围不一样

转发是服务器跳转只能去往当前web应用的资源
重定向是服务器跳转，可以去往任何的资源

### 8.4 传递数据的类型不同
转发的request对象可以传递各种类型的数据，包括对象
重定向只能传递字符串

### 8.5 跳转的时间不同
转发时：执行到跳转语句时就会立刻跳转
重定向：整个页面执行完之后才执行跳转

**那么转发(forward)和重定向(redirect)使用哪一个？**

根据上面说明了转发和重定向的区别也可以很容易概括出来**。转发是带着转发前的请求的参数的。重定向是新的请求**。

### 8.6 典型的应用场景：
1. 转发: 访问 Servlet 处理业务逻辑，然后 forward 到 jsp 显示处理结果，浏览器里 URL 不变
2. 重定向: 提交表单，处理成功后 redirect 到另一个 jsp，防止表单重复提交，浏览器里 URL 变了






[Servlet第六篇【Session介绍、API、生命周期、应用、与Cookie区别】(修订版)](https://mp.weixin.qq.com/s?__biz=MzI4Njg5MDA5NA==&mid=2247484755&idx=7&sn=fb35232f3c15e2b4336498ac9f8804f1&chksm=ebd74452dca0cd44942721a159088a2f286d4e5c5f2bcdc7e264f0dccc8f9928d66858e475d4###rd)
