---
title: netty实现简易RPC调用
toc: false
date: 2021-07-27 20:01:08
tags:
- Java
- IO
categories: [Develop Lan,Java,IO]
---

# 基于Netty手写一个RPC简易远程调用

![](https://oscimg.oschina.net/oscnet/up-b813c676214fc02b6f107ca7f6133a635fb.png)    

## 抽象协议
```java
@Data
public class InvokerProtocol implements Serializable {

    // 基于二进制流调用协议

    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 形参
     */
    private Class<?>[] params;
    /**
     * 实参
     */
    private Object[] values;
}
```

## 注册中心

### RpcRegistry 基于Netty实现的RPC注册中心  
>1、 ServerBootstrap 启动8080端口，等待客户端链接；  
>2、 RegisterHandler用来处理RPC接口的发现和注册；    
```java
public class RpcRegistry {

    private Integer post;

    public RpcRegistry(Integer post) {
        this.post = post;
    }
    
    private void start() {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        // 接受客户端请求的处理
                        ChannelPipeline pipeline = ch.pipeline();
                        //配置通用解码器
                        int fieldLength = 4;
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, fieldLength, 0, fieldLength));
                        pipeline.addLast(new LengthFieldPrepender(fieldLength));
                        //对象编码器
                        pipeline.addLast("encoder", new ObjectEncoder());
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast(new RegisterHandler());

                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            ChannelFuture future = server.bind(this.post).sync();
            System.out.println("Rpc registry started in port " + this.post);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new RpcRegistry(8080).start();
    }
    
}
```
### RegisterHandler 执行RPC的发现和注册 
> 1、扫描固定包下或者路径下的类;  
> 2、接口为key，具体实例作为value；  

```java
public class RegisterHandler extends ChannelInboundHandlerAdapter {
    /**
     * 注册中心容器
     */
    private static final ConcurrentHashMap<String, Object> REGISTRY_MAP = new ConcurrentHashMap<String, Object>();

    private List<String> classNameList = new ArrayList<String>();

    public RegisterHandler() {
        // 1、扫描所有需要注册的类
        scannerClass("com.ibli.netty.rpc.provider");
        // 执行注册
        doRegistry();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result;
        InvokerProtocol request = (InvokerProtocol) msg;
        if (REGISTRY_MAP.containsKey(request.getClassName())) {
            Object provider = REGISTRY_MAP.get(request.getClassName());
            Method method = provider.getClass().getMethod(request.getMethodName(), request.getParams());
            result = method.invoke(provider, request.getValues());
            ctx.write(result);
            ctx.flush();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void doRegistry() {
        if (classNameList.isEmpty()) {
            return;
        }

        for (String className : classNameList) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                REGISTRY_MAP.put(i.getName(), clazz.newInstance());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

    }

    private void scannerClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scannerClass(packageName + "." + file.getName());
            } else {
                classNameList.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }
    }
}
```

## API以及实现
### RPC接口 定义一个简单的服务接口
> 作为一个微服务对外暴露的API;  
```java
public interface IRpcService {
    int add(int a, int b);

    int mul(int a, int b);

    int sub(int a, int b);

    int div(int a, int b);
}
```

### RPC接口实现
> provider实现具体的接口，提供具体的服务；  
```java
public class RpcServiceImpl implements IRpcService {
    public int add(int a, int b) {
        return a + b;
    }

    public int mul(int a, int b) {
        return a * b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public int div(int a, int b) {
        return a / b;
    }
}
```

## RPC调用方

### 调用RPC
```java
public class RpcConsumer {
    public static void main(String[] args) {
        IRpcService rpc = RpcProxy.create(IRpcService.class);
        System.err.println(rpc.add(1,3));
        System.err.println(rpc.mul(3,3));
        System.err.println(rpc.sub(14,3));
    }
}
```


## RpcProxy 动态代理对象请求RPC
> 通过Netty Bootstrap访问8080端口；
```java
public class RpcProxy {

    public static <T> T create(Class<?> clazz) {
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?>[] interfaces = clazz.isInterface() ?
                new Class[]{clazz} :
                clazz.getInterfaces();
        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, proxy);
        return result;
    }

    public static class MethodProxy implements InvocationHandler {
        private Class<?> clazz;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            } else {
                return rpcInvoke(proxy, method, args);
            }
        }

        private Object rpcInvoke(Object proxy, Method method, Object[] args) {

            //封装请求的内容
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setParams(method.getParameterTypes());
            msg.setValues(args);
            
            final RpcProxyHandler consumerHandler = new RpcProxyHandler();
            EventLoopGroup group = new NioEventLoopGroup();

            try {
                Bootstrap client = new Bootstrap();
                client.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                //接收课客户端请求的处理流程
                                ChannelPipeline pipeline = ch.pipeline();

                                int fieldLength = 4;
                                //通用解码器设置
                                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, fieldLength, 0, fieldLength));
                                //通用编码器
                                pipeline.addLast(new LengthFieldPrepender(fieldLength));
                                //对象编码器
                                pipeline.addLast("encoder", new ObjectEncoder());
                                //对象解码器
                                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                                pipeline.addLast("handler", consumerHandler);
                            }
                        })
                        .option(ChannelOption.TCP_NODELAY, true);

                ChannelFuture future = client.connect("localhost", 8080).sync();
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }

            return consumerHandler.getResponse();
        }
    }
}
```

### RPC调用方接受并处理调用结果
```java
public class RpcProxyHandler extends ChannelInboundHandlerAdapter {

    private Object response;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.response = msg;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    public Object getResponse() {
        return this.response;
    }
}
```


