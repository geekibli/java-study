---
title: Java-NIO核心组件--channel
toc: true
date: 2021-07-28 17:51:42
tags:
- Java
- IO
categories: [Develop Lan,Java,IO]
---
# NIO核心组件 - Channel

## SocketChannel 和 ServerSocketChannel
学习此部分可以对比Socket和ServerSocket

服务端代码
```java
public class NioSocketServer01 {
    public static void main(String[] args) {
        try {
            // ServerSocketChannel 支持阻塞/非阻塞
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 设置成非阻塞。默认阻塞true
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(8080));
            // 循环监听客户端连接
            while (true) {
                // 如果有客户端连接，则返回一个socketChannel实例，否则socketChannel=null
                SocketChannel socketChannel = serverSocketChannel.accept();
                // 代码执行到此处，说明有客户端链接
                if (socketChannel != null) {
                    // 读取客户端发送的数据，并输出
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    socketChannel.read(buffer);
                    System.err.println(new String(buffer.array()));
                    // 将数据在写会客户端
                    buffer.flip();
                    socketChannel.write(buffer);
                    
                    //验证客户端 socketChannel设置成false时，从服务端read数据的操作变成非阻塞的
                    //ByteBuffer buffer = ByteBuffer.allocate(1024);
                    //buffer.put("this is server!");
                    //buffer.flip();
                    //socketChannel.write(buffer);

                } else {
                    Thread.sleep(1000L);
                    System.err.println("no client");
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
客户端代码
```java
public class NioSocketClient1 {
    public static void main(String[] args) {
        try {
            
            SocketChannel socketChannel = SocketChannel.open();
            // 默认阻塞IO true
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("localhost", 8080));
            // finishConnect的主要作用就是确认通道连接已建立，方便后续IO操作（读写）不会因连接没建立而导致NotYetConnectedException异常。
            if (socketChannel.isConnectionPending()) {
                // finishConnect一直阻塞到connect建立完成
                socketChannel.finishConnect();
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put("hello world".getBytes());
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            
            byteBuffer.clear();
            int r = socketChannel.read(byteBuffer); // 非阻塞方法 byteBuffer的数据还是上面put的
            if (r > 0) {
                System.out.println("get msg:{}" + new String(byteBuffer.array()));
            } else {
                System.out.println("server no back");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```



