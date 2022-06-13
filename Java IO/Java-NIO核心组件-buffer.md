---
title: Java-NIO核心组件--buffer
toc: true
date: 2021-07-28 17:51:10
tags:
- Java
- IO
categories: [Develop Lan,Java,IO]
---

# Buffer 读写


## NIO之Buffer
Buffer作为NIO三大核心组件之一，本质上是一块可以写入数据，以及从中读取数据的内存，实际上也是一个byte[]数据,只是在NIO中被封装成了NIO Buffer对象
并提供了一组方法来访问这个内存块。

### 下面是一个简单的Demo
```java
// 读取一个text.txt文件，生成一个新的text1.txt文件
public class FirstNioDemo {
    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("/Users/gaolei/Desktop/text.txt");
        FileOutputStream fileOutputStream = new FileOutputStream("/Users/gaolei/Desktop/text1.txt");

        FileChannel inChannel = fileInputStream.getChannel();
        FileChannel outChannel = fileOutputStream.getChannel();
        // 声明缓冲区大小为1024字节
        ByteBuffer byteBuffer =  ByteBuffer.allocate(1024);
        // 从通道中读取数据
        inChannel.read(byteBuffer);
        // 读模式切换为写模式
        byteBuffer.flip();
        //把缓冲区的数据写到通道
        outChannel.write(byteBuffer);
        // 数据写完之后清空全部缓冲区
        byteBuffer.clear();
        //关闭文件流
        fileInputStream.close();
        fileOutputStream.close();
    }
}
```   

> 执行结果：生成/Users/gaolei/Desktop/text1.txt文件  

**Buffer进行数据读写操作的一般步骤**  
1、写入数据到Buffer  
2、调用flip()方法  
3、从Buffer中读取数据  
4、调用clear()方法或者compact()方法  

> clear()方法会清空整个缓冲区。compact()方法只会清除已经读过的数据。任何未读的数据都被移到缓冲区的起始处，新写入的数据将放到缓冲区未读数据的后面。

 
### Buffer三个核心的属性  
- capacity 容量 与buffer处在什么模式无关
- position 游标位置 指向下一个存放/读取数据的位置 范围（0 ～ capacity–1）
- limit 


### 读写操作中Buffer三大属性的变化
初始状态  
<img src="https://oscimg.oschina.net/oscnet/up-71f90dfd671f80eb9f6142f135b7c2dfc92.png"  height="230" width="395">    
第一次读取数据  
position处于起始位置，limit和capacity都处于结尾  
<img src="https://oscimg.oschina.net/oscnet/up-41b47d9e54d58c7b39caf9e514fc9b5261f.png"  height="230" width="395">  
第二次读取数据  
<img src="https://oscimg.oschina.net/oscnet/up-07f3d1aa1f886b592b386cd4d846810911d.png"  height="230" width="395">  
当写数据的时候，需要调用flip方法： 
当将Buffer从写模式切换到读模式，position会被重置为0. 当从Buffer的position处读取数据时，position向前移动到下一个可读的位置。  
当切换Buffer到读模式时， limit表示你最多能读到多少数据。因此，当切换Buffer到读模式时，limit会被设置成写模式下的position值。换句话说，你能读到之前写入的所有数据（limit被设置成已写数据的数量，这个值在写模式下就是position）     
<img src="https://oscimg.oschina.net/oscnet/up-b9323701bbb34a6c12f61d5ac2652ab7eeb.png"  height="230" width="395">  
Clear方法  
<img src="https://oscimg.oschina.net/oscnet/up-71f90dfd671f80eb9f6142f135b7c2dfc92.png"  height="230" width="395">  


### JAVA NIO下的Buffer分类
- ByteBuffer
- MappedByteBuffer
- CharBuffer
- DoubleBuffer
- FloatBuffer
- IntBuffer
- LongBuffer
- ShortBuffer
> Java基本类型除了布尔类型，都有其对应的Buffer 

### ByteBuffer使用
> 下面以ByteBuffer为例子看一下Buffer如何使用

```java
// 创建一个byteBuffer，设置容量为1024字节
ByteBuffer byteBuffer =  ByteBuffer.allocate(1024);
```
1、如下代码，其实调用了new HeapByteBuffer(capacity, capacity)来创建一个buffer  
```java
 public static ByteBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new HeapByteBuffer(capacity, capacity);
    }
```
2、创建了buffer之后要往里面写数据，除了上面从channel中读取数据之外，还可以调用put方法,如下
```java
ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
byteBuffer.put("hello world".getBytes());
```

3、如果写将buffer中的数据写出去，必须先调用flap方法
> flip方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值。

4、将数据写到通道中 inChannel.write(buf);

5、数据写出到通道之后，要将缓存清空，一般调用clear方法
**clear方法**
```java
 public final Buffer clear() {
        //position将被设回0
        position = 0;
        //limit被设置成 capacity的值
        limit = capacity;
        mark = -1;
        return this;
    }
```
Buffer中的数据并未清除，只是这些标记告诉我们可以从哪里开始往Buffer里写数据。  
**compact方法**
如果Buffer中仍有未读的数据，且后续还需要这些数据，但是此时想要先先写些数据，那么使用compact()方法。
```java
public ByteBuffer compact() {
        //compact()方法将所有未读的数据拷贝到Buffer起始处。
        System.arraycopy(hb, ix(position()), hb, ix(0), remaining());
        //position设到最后一个未读元素正后面
        position(remaining());
        //limit属性设置成capacity
        limit(capacity());
        discardMark();
        return this;
    }
现在Buffer准备好写数据了，但是不会覆盖未读的数据 
```
 

### 零拷贝原理
-- 零拷贝，第一次接触零拷贝是在kafka的数据存储部分--
IO流程：  
<img src="https://oscimg.oschina.net/oscnet/up-f5a9accbd021cfe41414ca72391b3889049.png"  height="230" width="395">  
内存映射缓冲区  
<img src="https://oscimg.oschina.net/oscnet/up-a7a80d3426d1497bcaa69f30789718db0ee.png"  height="230" width="395">  
比普通IO操作文件快很多，甚至比channel还要快很多。  
因为避免了很多系统调用（System.read System.write）。减少了内核缓冲区的数据拷贝到用户缓冲区。

举个栗子：
```java
  public static void main(String[] args) throws IOException {
        FileChannel in = FileChannel.open(Paths.get("/Users/gaolei/Desktop/text.txt"), StandardOpenOption.READ);
        FileChannel out = FileChannel.open(Paths.get("/Users/gaolei/Desktop/text1.txt"), StandardOpenOption.READ, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        MappedByteBuffer inBuffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
        MappedByteBuffer outBuffer = out.map(FileChannel.MapMode.READ_WRITE, 0, in.size());

        byte[] bytes = new byte[inBuffer.limit()];
        inBuffer.get(bytes);
        outBuffer.put(bytes);
        in.close();
        out.close();
    }
```
![](https://oscimg.oschina.net/oscnet/up-ec7269566091c2f389749849ad734972de6.png)  
普通的网络IO拷贝流程
1、首先系统从磁盘上拷贝文件到内核空间缓冲区  
2、然后在内核空间拷贝数据到用户空间  
3、第三次，用户缓冲区再将数据拷贝到内核部分的socket缓冲  
4、内核在将存储在socket缓冲区的数据拷贝并发送到网卡缓冲区  
以上一个常规的网络IO经历了4次数据拷贝；  

设置缓冲区的意义在于提升性能，当用户空间仅仅需要一小部分数据的时候，操作系统会在磁盘上读取一块数据方法内核缓冲区，这个叫做局部性原理。

![](https://oscimg.oschina.net/oscnet/up-6209218a39ae427544247f1b3937b4043cc.png)  
零拷贝减去了内核空间数据到用户空间数据的拷贝，从而提升IO性能。假设读取的文件很大，操作系统需要读取磁盘大量数据到内核空间，
这时候内核缓冲区的作用是很难体现的。因为如果用户空间需要少量数据的时候是可以直接在内核空间获取的（局部性原理）。正式因为有了零拷贝，
操作系统在磁盘读取数据之后，可以直接发送到网卡缓冲区，从而大大提升IO性能。

