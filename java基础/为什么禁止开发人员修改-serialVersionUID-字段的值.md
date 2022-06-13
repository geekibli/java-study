---
title: 为什么禁止开发人员修改 serialVersionUID 字段的值?
toc: true
date: 2021-07-21 17:47:46
tags: 
- Java
categories: [Develop Lan,Java]
---



> 序列化是一种对象持久化的手段。普遍应用在网络传输、RMI 等场景中。类通 过实现 java.io.Serializable 接口以启用其序列化功能。
[Java 对象的序列化与反序列化、深入分析 Java 的序列化与反序列化、单例与 序列化的那些事儿](http://www.hollischuang.com/archives/1150)  

在这几篇文章中，分别介绍过了序列化涉及到的类和接口、如何自定义序列化 策略、transient 关键字和序列化的关系等，还通过学习 ArrayList 对序列化的实现源 码深入学习了序列化。并且还拓展分析了一下序列化对单例的影响等。
但是，还有一个知识点并未展开介绍，那就是关于 serialVersionUID 。这个 字段到底有什么用?如果不设置会怎么样?为什么《Java 开发手册》中有以下规定:
       
<img src='https://oscimg.oschina.net/oscnet/up-5b1a6020460edc9c688c054bdbad957cc28.png'>   


## 背景知识
Serializable 和 Externalizable
类通过实现 java.io.Serializable 接口以启用其序列化功能。未实现此接 口的类将无法进行序列化或反序列化。可序列化类的所有子类型本身都是可序列 化的。
如果读者看过 Serializable 的源码，就会发现，他只是一个空的接口，里 面什么东西都没有。Serializable 接口没有方法或字段，仅用于标识可序列化的 语义。但是，如果一个类没有实现这个接口，想要被序列化的话，就会抛出 java. io.NotSerializableException 异常。

它是怎么保证只有实现了该接口的方法才能进行序列化与反序列化的呢?
原因是在执行序列化的过程中，会执行到以下代码:

```java
if (obj instanceof String) {
            writeString((String) obj, unshared);
        } else if (cl.isArray()) {
            writeArray(obj, desc, unshared);
        } else if (obj instanceof Enum) {
            writeEnum((Enum<?>) obj, desc, unshared);
        } else if (obj instanceof Serializable) {
            writeOrdinaryObject(obj, desc, unshared);
        } else {
            if (extendedDebugInfo) {
                throw new NotSerializableException(
                        cl.getName() + "\n" + debugInfoStack.toString());
            } else {
                throw new NotSerializableException(cl.getName());
            }
        }
```

在进行序列化操作时，会判断要被序列化的类是否是 Enum、Array 和 Serializable 类型，如果都不是则直接抛出 NotSerializableException。
Java 中还提供了 Externalizable 接口，也可以实现它来提供序列化能力。

Externalizable 继承自 Serializable，该接口中定义了两个抽象方法: writeExternal() 与 readExternal()。
当使用 Externalizable 接口来进行序列化与反序列化的时候需要开发人员重 写 writeExternal() 与 readExternal() 方法。否则所有变量的值都会变成默认值。


## transient

transient 关键字的作用是控制变量的序列化，在变量声明前加上该关键字，可 以阻止该变量被序列化到文件中，在被反序列化后，transient 变量的值被设为初始值，如 int 型的是 0，对象型的是 null。


## 自定义序列化策略

在序列化过程中，如果被序列化的类中定义了 writeObject 和 readObject 方法， 虚拟机会试图调用对象类里的 writeObject 和 readObject 方法，进行用户自定义的 序列化和反序列化。

如果没有这样的方法，则默认调用是 ObjectOutputStream 的 defaultWriteOb- ject 方法以及 ObjectInputStream 的 defaultReadObject 方法。
用户自定义的 writeObject 和 readObject 方法可以允许用户控制序列化的过程， 比如可以在序列化的过程中动态改变序列化的数值。

所以，对于一些特殊字段需要定义序列化的策略的时候，可以考虑使用 tran- sient 修 饰， 并 自 己 重 写 writeObject 和 readObject 方 法， 如 java.util. ArrayList 中就有这样的实现。

我们随便找几个 Java 中实现了序列化接口的类，如 String、Integer 等，我们 可以发现一个细节，那就是这些类除了实现了 Serializable 外，还定义了一个 serialVersionUID

<img src='https://oscimg.oschina.net/oscnet/up-74a9507ed04462e44fb2b7dd941d6b00fd4.png'>

那么，到底什么是 serialVersionUID 呢?为什么要设置这样一个字段呢?


## 什么是 serialVersionUID

序列化是将对象的状态信息转换为可存储或传输的形式的过程。我们都知道， Java 对象是保存在 JVM 的堆内存中的，也就是说，如果 JVM 堆不存在了，那么对 象也就跟着消失了。

而序列化提供了一种方案，可以让你在即使 JVM 停机的情况下也能把对象保存 下来的方案。就像我们平时用的 U 盘一样。把 Java 对象序列化成可存储或传输的形 式(如二进制流)，比如保存在文件中。这样，当再次需要这个对象的时候，从文件中 读取出二进制流，再从二进制流中反序列化出对象。

虚拟机是否允许反序列化，不仅取决于类路径和功能代码是否一致，一个非常重 要的一点是两个类的序列化 ID 是否一致，这个所谓的序列化 ID，就是我们在代码中 定义的 serialVersionUID。

## 如果 serialVersionUID 变了会怎样

我们举个例子吧，看看如果 serialVersionUID 被修改了会发生什么?

```java
public class SerializableDemo1 {
            public static void main(String[] args) {
//Initializes The Object User1 user = new User1(); user.setName("hollis");
                //Write Obj to File ObjectOutputStream oos = null; try {
                oos = new ObjectOutputStream(new FileOutputStream("tempFile"));
                oos.writeObject(user);
            } catch(
            IOException e)

            {
                e.printStackTrace();
            } finally

            {
                IOUtils.closeQuietly(oos);
            }
        }
    }

    class User1 implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
```        

我们先执行以上代码，把一个 User1 对象写入到文件中。然后我们修改一下 User1 类，把 serialVersionUID 的值改为 2L。


```java
class User1 implements Serializable {
            private static final long serialVersionUID = 2L;
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
```

然后执行以下代码，把文件中的对象反序列化出来:

```java
 public class SerializableDemo2 {
            public static void main(String[] args) {
//Read Obj from File
                File file = new File("tempFile");
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new FileInputStream(file));
                    User1 newUser = (User1) ois.readObject();
                    System.out.println(newUser);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(ois);
                    try {
                        FileUtils.forceDelete(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
```   

执行结果如下
```java
java.io.InvalidClassException: com.hollis.User1; local class incompatible: stream classdesc
serialVersionUID = 1, local class serialVersionUID = 2
```

可以发现，以上代码抛出了一个 java.io.InvalidClassException，并且 指出 serialVersionUID 不一致。

这是因为，在进行反序列化时，JVM 会把传来的字节流中的 serialVersio- nUID 与本地相应实体类的 serialVersionUID 进行比较，如果相同就认为是一致 的，可以进行反序列化，否则就会出现序列化版本不一致的异常，即是 Invalid- CastException。

这也是《Java 开发手册》中规定，在兼容性升级中，在修改类的时候，不要 修改 serialVersionUID 的原因。除非是完全不兼容的两个版本。所以，serialVersionUID 其实是验证版本一致性的。

如果读者感兴趣，可以把各个版本的 JDK 代码都拿出来看一下，那些向下兼容 的类的 serialVersionUID 是没有变化过的。比如 String 类的 serialVersionUID一直都是 -6849794470754667710L。

但是，作者认为，这个规范其实还可以再严格一些，那就是规定:
如果一个类实现了 Serializable 接口，就必须手动添加一个 private static final long serialVersionUID变量，并且设置初始值。


## 为什么要明确定一个 serialVersionUID


如果我们没有在类中明确的定义一个 serialVersionUID 的话，看看会发生什么。

尝试修改上面的 demo 代码，先使用以下类定义一个对象，该类中不定义 serialVersionUID，将其写入文件。

```java
class User1 implements Serializable {
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
```

然后我们修改 User1 类，向其中增加一个属性。在尝试将其从文件中读取出来， 并进行反序列化。
```java
class User1 implements Serializable {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }
}
```

执 行 结 果:java.io.InvalidClassException: com.hollis.User1; local class incompatible: stream classdesc serialVersionUID = -2986778152837257883, local class serialVersionUID = 7961728318907695402

同样，抛出了 InvalidClassException，并且指出两个 serialVersio- nUID 不同，分别是 -2986778152837257883 和 7961728318907695402。
从这里可以看出，系统自己添加了一个 serialVersionUID。 所以，一旦类实现了 Serializable，就建议明确的定义一个 serialVersionUID。不然在修改类的时候，就会发生异常。

serialVersionUID 有两种显示的生成方式:

一是默认的1L，比如:private static final long serialVersionUID = 1L;
二是根据类名、接口名、成员方法及属性等来生成一个 64 位的哈希字段，比如:private static final long serialVersionUID = xxxxL;


## 小结

serialVersionUID 是用来验证版本一致性的。所以在做兼容性升级的时候， 不要改变类中 serialVersionUID 的值。

如果一个类实现了 Serializable 接口，一定要记得定义 serialVersionUID，否则会发生异常。可以在 IDE 中通过设置，让他帮忙提示，并且可以一键快速生成一 个 serialVersionUID。

之所以会发生异常，是因为反序列化过程中做了校验，并且如果没有明确定义的 话，会根据类的属性自动生成一个。

## 参考资料
> - [Java技术灵魂15问]()
