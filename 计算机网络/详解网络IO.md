---
title: 详解网络IO
toc: true
date: 2021-09-10 14:43:37
tags: 
- 网络IO
- epoll
categories: 
---

## 前言

学习思路可以是网络IO的演变过程，从【阻塞io】到【非阻塞io】然后到【多路复用】，后续还有【异步io】

## 1. 阻塞io

应用程序进行 recvfrom 系统调用时将阻塞在此调用，直到该套接字上有数据并且复制到用户空间缓冲区。该模式一般配合多线程使用，

应用进程每接收一个连接，为此连接创建一个线程来处理该连接上的读写以及业务处理。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210910160418632.png" alt="image-20210910160418632" style="zoom:50%;" />

**缺点**：如果套接字上没有数据，进程将一直阻塞。这时其他套接字上有数据也不能进行及时处理。

如果是多线程方式，除非连接关闭否则线程会一直存在，而线程的创建、维护和销毁非常消耗资源，所以能建立的连接数量非常有限。

## 2. 非阻塞io

应用进程调用recefrom函数之后，不等待内核数据准备完成，而是不断轮训（注意这里是用户进程不断轮训，会有用户态到内核态的切换，性能损耗比较严重）

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210910160610025.png" alt="image-20210910160610025" style="zoom:50%;" />



**优点**：代码编写相对简单，进程不会阻塞，可以在同一线程中处理所有连接。

**缺点**：需要频繁的轮询，比较耗CPU，在并发量很大的时候将花费大量时间在没有任何数据的连接上轮询。所以该模型只在专门提供某种功能的系统中才会出现。

## 3. io复用

应用进程阻塞于 **select/poll/epoll** 等系统函数等待某个连接变成可读（有数据过来），再调用 recvfrom 从连接上读取数据。虽然此模式也会阻塞在 select/poll/epoll 上，但与阻塞IO 模型不同它阻塞在等待多个连接上有读（写）事件的发生，明显提高了效率且增加了单线程/单进程中并行处理多连接的可能。



<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210910160743784.png" alt="image-20210910160743784" style="zoom:50%;" />

**优点**：统一管理连接，不一定采用多线程的方式，同时也不需要轮询。只需要阻塞于 select 即可，可以同时管理多个连接。

**缺点**：当 select/poll/epoll 管理的连接数过少时，这种模型将退化成阻塞 IO 模型。并且还多了一次系统调用：一次 select/poll/epoll 一次 recvfrom。

### 3.1 select

1、句柄上限- 默认打开的FD有限制,1024个。

2、重复初始化-每次调用 select()，需要把 fd 集合从用户态拷贝到内核态，内核进行遍历。

3、逐个排查所有FD状态效率不高。

### 3.2 poll

poll和select相比在本质上变化不大，只是poll没有了select方式的最大文件描述符数量的限制。

缺点：逐个排查所有FD状态效率不高。

### 3.3 epoll

<font color=blue>没有fd个数限制，用户态拷贝到内核态只需要一次，使用事件通知机制来触发。通过epoll_ctl注册fd，一旦fd就绪就会通过callback回调机制来激活对应fd，进行相关的I/O操作。</font>

epoll对文件描述符的操作有两种模式：**LT（level trigger）**和**ET（edge trigger）**。LT模式是默认模式，LT模式与ET模式的区别如下：

**LT模式**：当epoll_wait检测到描述符事件发生并将此事件通知应用程序，`应用程序可以不立即

处理该事件`。下次调用epoll_wait时，会再次响应应用程序并通知此事件。

**ET模式**：当epoll_wait检测到描述符事件发生并将此事件通知应用程序，`应用程序必须立即处

理该事件`。如果不处理，下次调用epoll_wait时，不会再次响应应用程序并通知此事件。

**缺点**：

- 跨平台，Linux 支持最好。
- 底层实现复杂。
- 同步。

### 3.4 select/poll/epoll之间的区别

![image-20210910161725824](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210910161725824.png)

## 4. 信号驱动io

应用进程创建 SIGIO 信号处理程序，此程序可处理连接上数据的读写和业务处理。并向操作系统安装此信号，进程可以往下执行。当内核数据准备好会向应用进程发送信号，触发信号处理程序的执

行。再在信号处理程序中进行 recvfrom 和业务处理。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210910161310686.png" alt="image-20210910161310686" style="zoom:50%;" />

**优点：**非阻塞

**缺点：**在前一个通知信号没被处理的情况下，后一个信号来了也不能被处理。所以在信号量大

的时候会导致后面的信号不能被及时感知。

## 5. 异步io

应用进程通过 aio_read 告知内核启动某个操作，在整个操作完成之后内核再通知应用进程，包括把

数据从内核空间拷贝到用户空间。

<font color=blue>**信号驱动 IO 是内核通知我们何时可以启动一个 IO 操作，而异步 IO 模型是由内核通知我们 IO 操作何时完成**</font>。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210910161126217.png" alt="image-20210910161126217" style="zoom:50%;" />

**<font color=green>注：前 4 种模型都是带有阻塞部分的，有的阻塞在等待数据准备好，有的阻塞在从内核空间拷贝数据到用户空间(信号量io)。而这种模型应用进程从调用 aio_read 到数据被拷贝到用户空间，不用任何阻塞，所以该种模式叫异步 IO 模型。</font>**

**优点**：没有任何阻塞，充分利用系统内核将 IO 操作与计算逻辑并行。

**缺点**：编程复杂、操作系统支持不好。目前只有 windows 下的 iocp 实现了真正的 AIO。linux 

下在 2.6 版本中才引入，目前并不完善，所以 Linux 下一般采用多路复用模型。


## 6. Reactor 和 Proactor

Reactor 是<font color=blue>**非阻塞同步网络模式，感知的是就绪可读写事件**</font>。在每次  感知到有事件发生（比如可读就绪事件）后，就需要应用进程主动调用 read 方法来完成数据的读  取，也就是要应用进程主动将 socket 接收缓存中的数据读到应用进程内存中，这个过程是同步的，  读取完数据后应用进程才能处理数据。

Proactor 是<font color=blue>**异步网络模式， 感知的是已完成的读写事件**</font>。在发起异步读写请求时，需要传入数据缓冲区的地址（用来存放结果数据）等信息，这样系统内核才可以自动帮我们把数据的读写工作完成，<font color=green>**这里的读写工作全程由操作系统来做，并不需**</font>要像 Reactor 那样还需要应用进程主动发起 read/write 来读写数据**</font>，操作系统完成读写工作后，就会通知应用进程直接处理数据。

因此，<font color=blue>**Reactor 可以理解为「来了事件操作系统通知应用进程，让应用进程来处理」**</font>，而 <font color=green> **Proactor 可以理解为「来了事件操作系统来处理，处理完再通知应用进程」</font>。这里的「事件」就是有新连接、有数据可读、有数据可写的这些 I/O 事件这里的「处理」包含从驱动读取到内核以及从内核读取到用户空间。

![](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/20210910173232.png)  

可参见 https://mp.weixin.qq.com/s/px6-YnPEUCEqYIp_YHhDzg	


## 7. 参考资料
> - [「网络IO套路」当时就靠它追到女友](https://mp.weixin.qq.com/s/x-AZQO5uiuu5svIvScotzA)
> - [彻底理解 IO多路复用](https://juejin.cn/post/6844904200141438984)
> - [看完这个，Java IO从此不在难](https://juejin.cn/post/6844903678227267597)
> - [从操作系统层面理解Linux下的网络IO模型](https://juejin.cn/post/6844904048198451214)
> - [五种IO模型介绍和对比](https://juejin.cn/post/6844903728718462990#heading-8)
> - [服务器网络编程之 IO 模型](https://juejin.cn/post/6844903812738596878#heading-3)
> - [网络编程与高效IO](https://www.processon.com/view/5f1e369ee0b34d54dacc18b9?fromnew=1)
> - [高性能网络IO模式Reactor](https://juejin.cn/post/6979761228251922469)
> - [Linux IO模式及 select、poll、epoll详解](https://juejin.cn/post/6844903488170786824#heading-15)
> - [这次答应我，一举拿下 I/O 多路复用！](https://mp.weixin.qq.com/s?__biz=MzUxODAzNDg4NQ==&mid=2247489558&idx=1&sn=7a96604032d28b8843ca89cb8c129154&scene=21#wechat_redirect)
> - [原来 8 张图，就能学废 Reactor 和 Proactor](https://mp.weixin.qq.com/s/px6-YnPEUCEqYIp_YHhDzg	)
> - [聊聊IO多路复用之select、poll、epoll详解](https://www.jianshu.com/p/dfd940e7fca2)
> - [聊聊Linux 五种IO模型](https://www.jianshu.com/p/486b0965c296)

