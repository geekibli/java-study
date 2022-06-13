---
title: Java-NIO核心组件--selector
toc: true
date: 2021-07-28 17:51:30
tags:
- Java
- IO
categories: [Develop Lan,Java,IO]
---

# 多路复用器

## select

1、select选择器会告诉客户端哪些连接有数据要读取，但是读取的操作还是用户自己触发的，这种叫做「同步」


```java
package com.ibli.javaBase.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author gaolei
 * @Date 2021/4/3 4:09 下午
 * @Version 1.0
 */
public class SelectMultiple {

    private ServerSocketChannel server = null;
    private Selector selector = null;
    int port = 9090;

    public void initServer() throws IOException {
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(port));
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        initServer();
        System.err.println("server started ....");

        while (true) {
            // selector.select() 调用系统内核的select
            while (selector.select() > 0) {
                // 从多路复用器中选择有效的key
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectionKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (key.isAcceptable()) {
                        acceptHandle(key);
                    } else if (key.isReadable()) {
                        readHandle(key);
                    }
                }

            }

        }
    }

    public void acceptHandle(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel client = ssc.accept();
        client.configureBlocking(false);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        client.register(selector, SelectionKey.OP_READ, byteBuffer);
        System.err.println("client arrived " + client.getRemoteAddress());
    }


    public void readHandle(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        int read = 0;
        while (true) {
            read = client.read(buffer);
            if (read > 0) {
                // 服务端读到的数据，再写一遍给到客户端
                buffer.flip();
                while (buffer.hasRemaining()) {
                    client.write(buffer);
                }
                buffer.clear();
            } else if (read == 0) {
                break;
            } else {
                // client 发生错误 或者断开 read == -1
                // 导致空转 最终CPU达到100%
                client.close();
                break;
            }
        }
    }

}


```

> 上面的写法是一个selector既担任boss又担任worker 






