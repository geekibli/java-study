---
title: Java-synchronzied底层原理
toc: true
date: 2021-07-28 17:57:19
tags: 多线程
categories: [Develop Lan,Java,多线程与并发]
---

# synchronzied底层原理

## synchronzied四个层级实现
- Java代码 通过添加synchronzied给对象或者方法或者代码块
- 字节码层级通过一组 MONITORENTER/MONITOREXIT指令
- JVM层级：锁升级过程
- 汇编执行通过 lock comxchg指令保证原子操作


JDK早期，synchronized 叫做重量级锁， 因为申请锁资源必须通过kernel, 系统调用
```java
;hello.asm
;write(int fd, const void *buffer, size_t nbytes)

section data
    msg db "Hello", 0xA
    len equ $ - msg

section .text
global _start
_start:

    mov edx, len
    mov ecx, msg
    mov ebx, 1 ;文件描述符1 std_out
    mov eax, 4 ;write函数系统调用号 4
    int 0x80

    mov ebx, 0
    mov eax, 1 ;exit函数系统调用号
    int 0x80
```

优化后的synchronized如下👇：
## Java层级
```java
  public static void main(String[] args) {
        Object object = new Object();
        System.out.println(ClassLayout.parseInstance(object).toPrintable());

        synchronized (object){
            System.out.println(ClassLayout.parseInstance(object).toPrintable());
        }
    }
```

## 字节码层级
```java
// access flags 0x9
  public static main([Ljava/lang/String;)V
    // parameter  args
    TRYCATCHBLOCK L0 L1 L2 null
    TRYCATCHBLOCK L2 L3 L2 null
   L4
    LINENUMBER 13 L4
    NEW java/lang/Object
    DUP
    INVOKESPECIAL java/lang/Object.<init> ()V
    ASTORE 1
   L5
    LINENUMBER 14 L5
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 1
    INVOKESTATIC org/openjdk/jol/info/ClassLayout.parseInstance (Ljava/lang/Object;)Lorg/openjdk/jol/info/ClassLayout;
    INVOKEVIRTUAL org/openjdk/jol/info/ClassLayout.toPrintable ()Ljava/lang/String;
    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
   L6
    LINENUMBER 16 L6
    ALOAD 1
    DUP
    ASTORE 2
    MONITORENTER
   L0
    LINENUMBER 17 L0
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 1
    INVOKESTATIC org/openjdk/jol/info/ClassLayout.parseInstance (Ljava/lang/Object;)Lorg/openjdk/jol/info/ClassLayout;
    INVOKEVIRTUAL org/openjdk/jol/info/ClassLayout.toPrintable ()Ljava/lang/String;
    INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
   L7
    LINENUMBER 18 L7
    ALOAD 2
    MONITOREXIT
   L1
    GOTO L8
   L2
   FRAME FULL [[Ljava/lang/String; java/lang/Object java/lang/Object] [java/lang/Throwable]
    ASTORE 3
    ALOAD 2
    MONITOREXIT
   L3
    ALOAD 3
    ATHROW
   L8
    LINENUMBER 19 L8
   FRAME CHOP 1
    RETURN
   L9
    LOCALVARIABLE args [Ljava/lang/String; L4 L9 0
    LOCALVARIABLE object Ljava/lang/Object; L5 L9 1
    MAXSTACK = 2
    MAXLOCALS = 4
}
```
> 主要通过MONITORENTER 和 MONITOREXIT 两个字节码指令控制加锁过程


## JVM层级

通过锁升级过程实现加锁；
无锁 -> 偏向锁 -> 自旋锁（轻量级锁 自适应锁）-> 重量级锁
锁升级过程可以查看 [锁升级过程](doc:rjG4EIhi)  复制理解


## 汇编指令级别
linux操作系统安装hsdis插件，查看java代码的汇编指令：
```
public class T {
    static volatile int i = 0;
    
    public static void n() { i++; }
    
    public static synchronized void m() {}
    
    publics static void main(String[] args) {
        for(int j=0; j<1000_000; j++) {
            m();
            n();
        }
    }
}
```
执行以下命令：
```
java -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly T
```
- C1 Compile Level 1 (一级优化)
- C2 Compile Level 2 (二级优化)

> 找到m() n()方法的汇编码，会看到 lock comxchg .....指令

