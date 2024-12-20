---
title: JVM-内存结构
toc: true
date: 2021-07-30 11:53:42
tags: JVM
categories:
---


作为程序员，最常接触到Java虚拟机的部分应该是内存结构这一部分了，同样这一部分的内容很多，面试也是最常被问到的。虽然JDK已经发布了16版本，但是国内大部分企业都还在使用JDK8。 今天学习一下虚拟机的运行时数据区的组成和各个组件的功能。

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210730144621195.png" alt="image-20210730144621195" style="zoom:50%;" />

JDK8官方网站文档链接 -- 》 [JDK](https://docs.oracle.com/javase/8/docs/index.html)

# 运行时数据区

Java虚拟机在执行Java程序的过程中会把它所管理的内存划分为若干个不同的数据区域。这些数据区域有各自的用途，以及创建和销毁的时间，有的区域随着虚拟机进程的启动而存在，有些区域则依赖用户的启动和结束而建立和销毁。

![image-20210730141548126](https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210730141548126.png)

## 程序计数器

程序计数器（Program Counter Register），它是一块较小的内存空间，可以看作是当前线程所执行的字节码的行号指示器。在虚拟机的模型概念中，字节码解释器的工作就是通过改变这个计数器的值来选取下一条需要执行的字节码指令，分支、循环、跳转、异常处理、线程恢复等基础功能都需要依赖这个计数器来完成。

> 如果线程正在执行一个 Java 方法，这个计数器记录的是正在执行的虚拟机字节码指令的地址；如果正在执行的是 Native 方法，这个计数器的值则为 (Undefined)。

由于Java虚拟机的多线程是通过线程轮流切换处理器执行时间的方式实现的，在任何一个确定的时刻，一个处理器的一个核只会执行一条线程中的指令，因此，<font color=red>为了线程切换后能够恢复到正确的执行位置，每一条线程都需要拥有一个独立的程序计数器，各条线程之间的计数器互不影响，独立存储</font>，这类内存区域称为“线程私有”的内存，即如上图所示，每一个线程都会拥有自己的一块内存区域。

程序计数器在执行本地方法时（例如调用C语言代码）计数器值为空，其他时候则是指向正在执行的虚拟机字节码指令的地址。

程序计数器是在Java虚拟机规范中唯一一个没有规定任何OutOfMemoryError情况的区域，因为Java程序计数器它所需要存储的内容仅仅就是下一个需要待执行的命令的地址，其所需内存是创建时即可只晓的，不需要后期进行扩容等其他的操作。

## Java虚拟机栈

Java虚拟机栈（Java Virtual Machine Stacks），<font color=red>Java虚拟机栈也是线程私有的,它的生命周期与线程相同</font>。Java每个方法在执行的同时都会创建一个栈帧用于存储`局部变量表`、`操作数栈`、`动态链接`、`方法出口`等信息。每一个方法从调用直至方法执行完成的过程，就对应着一个栈帧在虚拟机栈中入栈到出栈的过程。

虚拟机栈中局部变量表部分与Java对象内存分配关系密切，局部变量表存放了编译器可知的各种基本数据类型（boolean、byte、char、short、int、float、long、double）、对象引用（reference类型，该类型可能是一个指向对象起始地址的引用指针，也可能是一个代表对象的句柄或其他于此对象相关的位置）和returnAddress类型（指向了一条字节码指令的地址）。

局部变量表中，64位长度的long和double类型的数据会占用2个局部变量空间，其余的数据类型只占用一个。`局部变量表所需的内存空间在编译期间完成分配`，当进入一个方法时，这个方法需要在帧中分配多大的局部变量空间是完全确定的，在方法运行期间不会改变局部变量表的大小。

在Java虚拟机规范中，对这个区域规定了两种异常状况：

如果线程请求的栈深度大于虚拟机所允许的深度，将抛出StackOverflowError异常；
如果虚拟机栈可以动态拓展，如果拓展时无法申请到足够的内存，就会抛出OutOfMemoryError异常。

## 本地方法栈

本地方法栈（Native Method Stack）与虚拟机栈作用类似，它们之间的区别是虚拟机栈为虚拟机执行Java方法，而本地方法栈则为虚拟机执行Native方法服务。有些虚拟机会将本地方法栈和虚拟机栈合二为一。与虚拟机栈一样，本地方法栈也会抛出StackOverflowErro和OutOfMemoryError异常。

## Java堆

Java堆（Java Heap），对于大多数的应用来说，Java堆是虚拟机所管理的最大的一块内存。`Java堆是被所有的线程所共享的，在虚拟机启动时创建`。此内存区域的唯一目的就是存放对象实例，几乎所有的对象实例都是在这里分配内存的（Java虚拟机规范中描述为所有的对象实例和数组都要在堆上分配内存）。

**Java堆是垃圾收集器管理的主要区域**，因此很多时候也被称为GC堆。从内存回收的角度来看，由于现在收集器基本都是采用分代算法收集器，所以Java堆中还可以细分为：新生代和老年代；再细致一点可以分为Eden空间、From Survivor空间、To Survivor空间等。从内存分配的角度来看，线程共享的Java堆中可能划分出多个线程私有的分配缓冲区（Thread Local Allocation Buffer，TLAB）。

根据Java虚拟机规范，***Java堆可以处于物理上不连续的内存空间中，只要逻辑上是连续的即可***，在实现时既可以是固定大小的，也可以是可拓展的，当前主流的虚拟机都是按照可拓展来实现的。如果在堆中没有内存完成实例分配，并且堆也无法再拓展时，将会抛出OutOfMemoryError异常。

在 Java 中，堆被划分成两个不同的区域：**新生代 ( Young )**、**老年代 ( Old )**。**新生代 ( Young )** 又被划分为三个区域：**Eden**、**From Survivor(S0)**、**To Survivor(S1)**。如图所示：

<img src="https://xcu-oss.oss-cn-beijing.aliyuncs.com/image/gao/image-20210730150932873.png" alt="image-20210730150932873" style="zoom: 30%;" />

这样划分的目的是为了使JVM能够更好的管理内存中的对象，包括内存的分配以及回收。  而新生代按eden和两个survivor的分法，是为了

- 有效空间增大，eden+1个survivor；
- 有利于对象代的计算，当一个对象在S0/S1中达到设置的XX:MaxTenuringThreshold值后，会将其挪到老年代中，即只需扫描其中一个survivor。如果没有S0/S1,直接分成两个区，该如何计算对象经过了多少次GC还没被释放。
- 两个Survivor区可解决内存碎片化

### 堆栈相关参数

| **参数**                       | **描述**                                                     |
| ------------------------------ | ------------------------------------------------------------ |
| -Xms                           | 堆内存初始大小，单位m、g                                     |
| -Xmx                           | 堆内存最大允许大小，一般不要大于物理内存的80%                |
| -Xmn                           | 年轻代内存初始大小                                           |
| -Xss                           | 每个线程的堆栈大小，即JVM栈的大小                            |
| -XX:NewRatio                   | 年轻代(包括Eden和两个Survivor区)与年老代的比值               |
| -XX:NewSzie(-Xns)              | 年轻代内存初始大小,可以缩写-Xns                              |
| -XX:MaxNewSize(-Xmx)           | 年轻代内存最大允许大小，可以缩写-Xmx                         |
| -XX:SurvivorRatio              | 年轻代中Eden区与Survivor区的容量比例值，默认为8，即8:1       |
| -XX:MinHeapFreeRatio           | GC后，如果发现空闲堆内存占到整个预估堆内存的40%，则放大堆内存的预估最大值，但不超过固定最大值。 |
| -XX:MaxHeapFreeRatio           | 预估堆内存是堆大小动态调控的重要选项之一。堆内存预估最大值一定小于或等于固定最大值(-Xmx指定的数值)。前者会根据使用情况动态调大或缩小，以提高GC回收的效率，默认70% |
| -XX:MaxTenuringThreshold       | 垃圾最大年龄，设置为0的话,则年轻代对象不经过Survivor区,直接进入年老代。对于年老代比较多的应用,可以提高效率.如果将此值设置为一个较大值,则年轻代对象会在Survivor区进行多次复制,这样可以增加对象再年轻代的存活 时间,增加在年轻代即被回收的概率 |
| -XX:InitialTenuringThreshold   | 可以设定老年代阀值的初始值                                   |
| -XX:+PrintTenuringDistribution | 查看每次minor GC后新的存活周期的阈值                         |



> **Note：** 每次GC 后会调整堆的大小，为了**防止动态调整带来的性能损耗**，一般设置-**Xms、-Xmx 相等**。

新生代的三个设置参数：-Xmn，-XX:NewSize，-XX:NewRatio的优先级：
 （1）.最高优先级：  -XX:NewSize=1024m和-XX:MaxNewSize=1024m
 （2）.次高优先级：  -Xmn1024m  （默认等效效果是：-XX:NewSize==-XX:MaxNewSize==1024m）
 （3）.最低优先级：-XX:NewRatio=2
 推荐使用的是-Xmn参数，原因是这个参数很简洁，相当于一次性设定NewSize和MaxNewSIze，而且两者相等。

## 方法区

方法区（Method Area）与Java堆一样，是线程共享的，**它用于存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据**。`类加载的信息和数据就放在方法区。`

Java虚拟机规范堆方法区的限制非常宽松，除了和Java堆一样不需要连续的内存和可以选择固定大小或者可拓展外，还可以选择不实现垃圾收集。相对而言，垃圾收集行为在这个区域是比较少出现的，这个区域的内存回收目标主要是针对常量池的回收和对类型的卸载，一般来说，这个区域的内存回收成绩比较令人难以满意，尤其时类型卸载，条件相当苛刻，但是这个区域的内存回收也是必要的。

根据Java虚拟机规范规定，当方法区无法满足内存分配需求时，将抛出OutOfMemoryError异常。

## 运行时常量池

运行时常量池（Runtime Constant Pool）是方法区的一部分，Class文件中除了类的版本、字段、方法、接口等描述信息以外，还有一项信息是常量池，用于存放编译期生成的各种字面量和符号引用，这部分内容将在类加载后进入方法区的运行时常量池中存放。

运行时常量池相对于Class文件常量池的另外一个重要特征是具备动态性，Java语言并不要求常量一定只有编译器才能产生，也就是并非预置入Class文件中常量池的内容才能进入方法区运行时常量池，运行期间也可能将新的常量池放入池中，这种特性被开发人员利用得比较多的便是String类的intern()方法。

当常量池无法再申请到内存时会抛出OutOfMemoryError异常。

## 直接内存

直接内存（Direct Memory)并不是虚拟机运行时数据区的一部分，也不是Java虚拟机规范所定义的内存区域，但是这部分内存也被频繁的使用，而且也可能导致OutOfMemoryError异常出现。

在JDK1.4中新加入的NIO（New Input/Output）类，引入了一种基于通道与缓冲区的I/O方式，它可以使用Native函数库直接分配堆外内存，然后通过一个存储在Java堆中的DirectByteBuffer对象作为这块内存的引用进行操作，这样能在一些场景中显著提高性能，因为避免了在Java堆中和Native堆中来回复制数据。

直接内存虽然不会受到Java堆大小的限制，但是受到本机总内存大小以及处理器寻址空间的限制，如果忽略了直接内存，当各个区域内存总和大于服务器内存时，将会导致动态拓展时出现OutOfMemoryError异常。





## 扩展

**下面几种不同的变量的存放位置分别是什么？**

```
public class StaticObjTest {
static class Test{
// 静态变量
// 一个java.lang.Class类型的对象实例引用了此变量
static ObjectHolder staticObj = new ObjectHolder();
// 实例变量
ObjectHolder instanceObj = new ObjectHolder();
void foo() {
// 局部变量
ObjectHolder localObj = new ObjectHolder()();
System.out.println("done");
}
}
private static class ObjectHolder{
}
public static void main(String[] args) {
Test test = new StaticObjTest.Test();
test.foo();
}
}
```

以上代码中，静态变量staticObj随着Test的类型信息存放在方法区，  
实例变量instanceObj随着Test的对象实例存放在堆区，  
局部变量localObj则是存放在foo()方法栈帧的局部变量表中。  
**三个变量引用对应的对象实体都是在堆空间**。  

## 参考资料

https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.5

https://blog.csdn.net/qq_21122519/article/details/94408118

https://www.processon.com/view/5ec5d7c60791290fe0768668?fromnew=1
