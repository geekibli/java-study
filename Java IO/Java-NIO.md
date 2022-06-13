---
title: Java-NIO
toc: true
date: 2021-07-28 17:50:38
tags:
- Java
- IO
categories: [Develop Lan,Java,IO]
---
# Java NIO

> Java NIO 对于Java BIO的优化

## Java 非阻塞IO
> 及时不使用线程池，也可以处理多个客户端请求
```java
    public static void main(String[] args) throws IOException, InterruptedException {

        LinkedList<SocketChannel> clients = new LinkedList<>();
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(new InetSocketAddress(9090));
        ss.configureBlocking(false);
        while (true) {
            Thread.sleep(1000L);
            // 非阻塞
            SocketChannel client = ss.accept();
            if (client == null) {
                System.err.println("client is null");
            } else {
                client.configureBlocking(false);
                int port = client.socket().getPort();
                System.err.println("client port " + port);
                clients.add(client);
            }

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);

            // 串型话
            // 真实场景下 每一个client一个独自的buffer
            for (SocketChannel c : clients) {
                // -1 出现空轮训 
                int num = c.read(byteBuffer);
                if (num > 0) {
                    byteBuffer.flip();
                    byte[] aaa = new byte[byteBuffer.limit()];
                    byteBuffer.get(aaa);

                    String b = new String(aaa);
                    System.err.println(c.socket().getPort() + " :   " + b);
                    // 清空 循环下一次client在使用
                    byteBuffer.clear();
                }
            }
        }
    }

```

> 以上可以实现，一个线程可以处理多个客户端链接，服务端非阻塞接收，接收之后，读取数据也是非阻塞的；


> NIO的非阻塞是操作系统内部实现的，底层调用了linux内核的accept函数




>d Java的NIO有什么弊端

- 服务端还是会进行空转
- 不管有没有客户端连接建立，服务端都要不断执行accept方法
- 不管客户端连接有没有传输数据，都会执行一遍read操作


> 资源浪费问题

还是会存在C10k的问题


