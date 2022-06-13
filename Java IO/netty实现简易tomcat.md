---
title: netty实现简易tomcat
toc: false
date: 2021-07-27 19:54:15
tags: 
- Java
- IO
categories: [Develop Lan,Java,IO]
---

# 基于Netty手写一个简易的Tomcat容器  

本文主要基于传统的BIO来实现一个简单的Http请求处理过程；    
1、Servlet请求无非就是doGet/doPost，所以我们定义抽象Servlet记忆GET/POST方法；  
2、基于Netty API实现CS通信；  
3、模拟Spring加载配置文件，注册请求以及控制器；  

![](https://oscimg.oschina.net/oscnet/up-6bb2d717164854e73513f9ed1355d1844bc.png)   

## Netty版本
```java
<dependency>
   <groupId>o.netty</groupId>
   <artifactId>netty-all</artifactId>
   <version>4.1.6.Final</version>
</dependency>
```

##  GlRequest 基于Netty&HttpRequest的API操作，非常简单
```java
public class GlRequest {

    private ChannelHandlerContext ctx;
    private HttpRequest req;

    public GlRequest(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public String getUrl() {
        return this.req.uri();
    }

    public String getMethod() {
        return this.req.method().name();
    }

    public Map<String, List<String>> getParams() {
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        return decoder.parameters();
    }

    public String getParam(String name) {
        Map<String, List<String>> params = getParams();
        List<String> strings = params.get(name);
        if (strings == null) {
            return null;
        }
        return strings.get(0);
    }
}
```



##  GlResponse 基于Netty&FullHttpResponse的API操作
> FullHttpResponse作为返回请求的主体；  
```java
public class GlResponse {

    private ChannelHandlerContext ctx;
    private HttpRequest req;

    public GlResponse(ChannelHandlerContext ctx, HttpRequest req) {
        this.req = req;
        this.ctx = ctx;
    }

    public void write(String string) throws Exception {

        if (string == null || string.length() == 0) {
            return;
        }

        try {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(string.getBytes("UTF-8"))
            );

            response.headers().set("Content-Type", "text/html");
            ctx.write(response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ctx.flush();
            ctx.close();
        }
    }
}
```


## GlServlet 定义抽象servlet，定义GET方法和POST方法
> 定义抽象的Servlet和doGet方法和doPost方法，具体的业务去实现自己的方法和逻辑；  
```java
public abstract class GlServlet {
   
       private final static String GET = "GET";
   
       public void service(GlRequest request, GlResponse response) throws Exception {
           if (GET.equals(request.getMethod())) {
               doGet(request, response);
           } else {
               doPost(request, response);
           }
       }
   
       public abstract void doGet(GlRequest request, GlResponse response) throws Exception;
   
       public abstract void doPost(GlRequest request, GlResponse response) throws Exception;
   }
```


## FirstServlet 具体的业务Servlet实现抽象Servlet的方法
```java
public class FirstServlet extends GlServlet {
    @Override
    public void doGet(GlRequest request, GlResponse response) throws Exception {
        // 具体的逻辑
        this.doPost(request, response);

    }

    @Override
    public void doPost(GlRequest request, GlResponse response) throws Exception {
        response.write("This is first servlet from NIO");
    }
}
```

## SecondServlet 具体的业务Servlet实现抽象Servlet方法
```java
public class SecondServlet extends GlServlet {
    
    @Override
    public void doGet(GlRequest request, GlResponse response) throws Exception {
        doPost(request,response);
    }

    @Override
    public void doPost(GlRequest request, GlResponse response) throws Exception {
        response.write("This second request form NIO");
    }
}
```

## web-nio.properties 配置文件
> 配置请求和处理器，Spring中是通过Controller下的@XXXMapping注解去扫描并加载到工厂的；  
```java
servlet.one.className=com.ibli.netty.tomcat.nio.servlet.FirstServlet
servlet.one.url=/firstServlet.do

servlet.two.className=com.ibli.netty.tomcat.nio.servlet.SecondServlet
servlet.two.url=/secondServlet.do
```

## GlTomcat
> 启动服务端，在网页中访问本地8080端口，输入配置文件中定义的url进行测试：  
```java
public class GlTomcat {

    private final Integer PORT = 8080;
    private Properties webXml = new Properties();
    private Map<String, GlServlet> servletMapping = new HashMap<String, GlServlet>();
    public static void main(String[] args) {
        new GlTomcat().start();
    }

    /**
     * Tomcat的启动入口
     */
    private void start() {
        //1、加载web配置文件，解析配置
        init();

        // Boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // Worker线程
        EventLoopGroup workGroup = new NioEventLoopGroup();
        //2、创建Netty服务端对象
        ServerBootstrap server = new ServerBootstrap();
        //3、 配置服务端参数
        server.group(bossGroup, workGroup)
                // 配置主线程的处理逻辑
                .channel(NioServerSocketChannel.class)
                // 子线程的回调逻辑
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel client) {
                        // 处理具体的回调逻辑
                        // 责任链模式
                        //返回-编码
                        client.pipeline().addLast(new HttpResponseEncoder());
                        //请求-解码
                        client.pipeline().addLast(new HttpRequestDecoder());
                        //用户自己的逻辑处理
                        client.pipeline().addLast(new GlTomcatHandler());
                    }
                })
                // 配置主线程可分配的最大线程数
                .option(ChannelOption.SO_BACKLOG, 128)
                //保持长链接
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = null;
        try {
            future = server.bind(this.PORT).sync();
            System.err.println("Gl tomcat started in pory " + this.PORT);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    /**
     * 加载配置文件
     * 这其实使用了策略模式
     */
    private void init() {
        try {
            String WEB_INF = this.getClass().getResource("/").getPath();
            FileInputStream fis = new FileInputStream(WEB_INF + "web-nio.properties");
            webXml.load(fis);
            for (Object k : webXml.keySet()) {
                String key = k.toString();

                if (key.endsWith(".url")) {
                    //servlet.two.url
                    String servletName = key.replaceAll("\\.url", "");
                    String url = webXml.getProperty(key);
                    //servlet.two.className
                    String className = webXml.getProperty(servletName + ".className");
                    //反射创建servlet实例
                    // load-on-startup >=1 :web启动的时候初始化  0：用户请求的时候才启动
                    GlServlet obj = (GlServlet) Class.forName(className).newInstance();
                    // 将url和servlet建立映射关系
                    servletMapping.put(url, obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理用户请求
     */
    public class GlTomcatHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;
                GlRequest request = new GlRequest(ctx,req);
                GlResponse response = new GlResponse(ctx,req);
                String url = request.getUrl();
                if (servletMapping.containsKey(url)){
                    servletMapping.get(url).service(request,response);
                }
                else {
                    response.write("404 Not Fount");
                }
            }
        }
    }

}
```


## 测试结果
> 请求 : http://localhost:8080/secoundServlet.do 这的地址写错误  ⚠️
![](https://oscimg.oschina.net/oscnet/up-4652c388dd368f575bc5c0719f68a8632df.png)  

> 请求 : http://localhost:8080/secondServlet.do    
![](https://oscimg.oschina.net/oscnet/up-cc90b46288898bfe801e33f3a94bfab5070.png)  