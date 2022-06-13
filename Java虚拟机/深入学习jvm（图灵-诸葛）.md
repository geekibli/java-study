# 深入学习jvm（图灵-诸葛）


# 1. 从JDK源码剖析类加载机制

## 1.1 为什么需要类加载？

我们编写的程序代码都是存放在磁盘上面的，在程序运行时，需要把我们的class文件加载到内存，这就是为什么需要类加载。

## 1.2 java代码到底是怎么运行的呢

下面是一个普通的类，我们平时都会定义的，那么他们是怎么加载的呢？

```java
public class Math {

    public static final int initData = 666;
    public static User user = new User();

    public int compute() { //一个方法对应一块栈帧内存区域
        int a = 1;
        int b = 2;
        int c = (a + b) * 10;
        return c;
    }

    public static void main(String[] args) {
        Math math = new Math();
        math.compute();
    }
}
```

下面看一下这个类的main方法是如何运行的吧 👇

<img src="https://oscimg.oschina.net/oscnet/up-10964428d2deadcb19af7dea03f8b40fe4a.png" style="zoom:50%;" />

- java程序会调用底层的jvm类库文件中的函数来创建java虚拟机（C++中实现）
- jvm启动之后创建一个引导类加载实例（也就是我们说的类加载器中的BootstartClassLoader）
- C++调用java代码创建jvm启动器，`sun.misc.Launcher`该类来创建其他的类加载器（ExtClassLoader, AppClassLoader）
- 类加载器都创建完成之后，这些类加载器就可以到指定路径下面去加载类信息了，至于加载到哪里（方法区）后面再说
- 当类加载都完成之后（当然这里还有很多其他的细节），调用启动类的main方法执行java程序

## 1.3 类加载过程是什么？

加载 >> 验证 >> 准备 >> 解析 >> 初始化 >> 使用 >> 卸载

### 1.3.1 类加载的流程图



<img src="https://oscimg.oschina.net/oscnet/up-e1d5215968dd20b4c65191197b08b8470f9.png" style="zoom:50%;" />

### 1.3.2 加载

加载:在硬盘上查找并通过IO读入字节码文件，使用到类时才会加载，例如调用类的 main()方法，new对象等等，在加载阶段会在内存中生成一个代表这个类的 java.lang.Class对象，作为方法区这个类的各种数据的访问入口。

#### 1.3.2.1 如何验证类在用到的时候才会加载

```java
public class TestDynamicLoad {
    static {
        System.out.println("*************load TestDynamicLoad************");
    }

    public static void main(String[] args) {
        new A();
        System.out.println("*************load test************");
         B b = null; //B不会加载，除非这里执行 new B()
    }

    static class A {
        static {
            System.out.println("*************load A************");
        }
        public A (){
            System.out.println("*************initial A************");
        }
    }

    static class B {
        static {
            System.out.println("*************load B************");
        }
        public B (){
            System.out.println("*************initial B************");
        }
    }
}
```

输出结果：

```java
*************load TestDynamicLoad************
*************load A************
*************initial A************
*************load test************
```

### 1.3.3 验证

验证:校验字节码文件的正确性

#### 1.3.3.1 验证的目的是什么？

- 校验字节码格式等信息是否正确，是否符合字节码规范
- 保护jvm

### 1.3.4 准备

给类的静态变量分配内存，并赋予默认值。

```
final修饰的是静态变量，直接赋值，而static修饰的变量，则在准备阶段赋予默认值。
```

### 1.3.5 解析

将符号引用替换为直接引用，该阶段会把一些静态方法(符号引用，比如main()方法)替换为指向数据所存内存的指针或句柄等(直接引用)，这是所谓的静态链接过 程(类加载期间完成)，动态链接是在程序运行期间完成的将符号引用替换为直接引用。

#### 1.3.5.1 什么是静态链接｜动态链接？

像类信息是确定的，这种属于静态符号。

方法可能会设计到多态，不确定，只能动态运行时确定，运行时才能确定内存中的位置。

### 1.3.6 初始化 

对类的静态变量初始化为指定的值，执行静态代码块，构造函数。

### 1.3.6.1 初始化顺序

```java
public class SuperClass {
    static {
        System.out.println("superclass static ...");
    }
    public SuperClass(){
        System.out.println("superclass constructor ...");
    }
}

public class ChildClass extends SuperClass{
    static {
        System.out.println("childclass static ...");
    }
    public ChildClass (){
        System.out.println("childclass constructor");
    }
    public static void main(String[] args) {
        new ChildClass();
    }
}
```

输出结果：

```java
superclass static ...
childclass static ...
superclass constructor ...
childclass constructor
```

总结：

- 静态初始化块优先加载
- 构造器函数再加载
- 父类加载优先子类

## 1.4 类加载器

### 1.4.1 类加载器种类

- 引导类加载器： bootstarpClassLoader (C++中实现) 

  ```
  负责加载支撑JVM运行的位于JRE的lib目录下的核心类库，比如 rt.jar、charsets.jar等
  ```

- 扩展类加载器： static class ExtClassLoader extends URLClassLoader

  ```
  负责加载支撑JVM运行的位于JRE的lib目录下的ext扩展目录中的JAR 类包
  ```

- 应用程序类加载器： static class AppClassLoader extends URLClassLoader

  ```
  负责加载ClassPath路径下的类包，主要就是加载你自己写的那 些类
  ```

- 自定义类加载器 

  ```
  负责加载用户自定义路径下的类包
  ```

#### 1.4.1.1 如何验证加载的path

```java
System.out.println(String.class.getClassLoader());
System.out.println(com.sun.crypto.provider.DESKeyFactory.class.getClassLoader().getClass().getName());
System.out.println(User.class.getClassLoader().getClass().getName());
```

**输出结果**：

```java
null
sun.misc.Launcher$ExtClassLoader
sun.misc.Launcher$AppClassLoader
```

`String.class.getClassLoader()` 为什么是null呢？

因为String是jdk原生的类，在加载必须需要引导类加载器来加载，而引导类加载器是在C++中定义的，所以java代码中肯定拿不到。

如何证明呢？

```java
public ExtClassLoader(File[] var1) throws IOException {
            super(getExtURLs(var1), (ClassLoader)null, Launcher.factory);
            SharedSecrets.getJavaNetAccess().getURLClassPath(this).initLookupCache(this);
}
```

我们知道ExtClassLoader的父加载器是BootstarpClassLoader，这里在初始化ExtClassLoader的时候，设置父加载器的时候传的值是null。

### 1.4.2 类加载器之间的关系

- ExtClassLoader 是 AppClassLoader 的父加载器，而不是父类！
- 自定义类加载器的父加载器是 AppClassLoader

### 1.4.3 类加载器什么时候创建的

上面已经提到，在C++程序创建了引导类加载器之后，或创建java的Launcher类，在构造这个类的时候，创建的ExtClassLoader和AppClassLoader。

```java
public Launcher() {
        Launcher.ExtClassLoader var1;
        try {
            var1 = Launcher.ExtClassLoader.getExtClassLoader();
        } catch (IOException var10) {
            throw new InternalError("Could not create extension class loader", var10);
        }

        try {
            this.loader = Launcher.AppClassLoader.getAppClassLoader(var1);
        } catch (IOException var9) {
            throw new InternalError("Could not create application class loader", var9);
        }
}
```

**Launcher是单例的。**

```java
public class Launcher {
    private static URLStreamHandlerFactory factory = new Launcher.Factory();
    private static Launcher launcher = new Launcher();
    private static String bootClassPath = System.getProperty("sun.boot.class.path");
    private ClassLoader loader;
    private static URLStreamHandler fileHandler;

    public static Launcher getLauncher() {
        return launcher;
    }
}
```

## 1.5 双亲委派机制

<img src="https://oscimg.oschina.net/oscnet/up-3f17f266b143eaf2705ce60de4d09324e1f.png" style="zoom:50%;" />

### 1.5.1 什么是双亲委派？

这里类加载其实就有一个双亲委派机制，加载某个类时会先判断是否已经加载过，如果已经加载过，直接返回，如果没加载过，先委托父加载器寻找目标类，父加载器找不到再委托上层父加载器加载，如果所有父加载器在自己的加载类路径下都找不到目标类，则在自己的 类加载路径中查找并载入目标类。 

比如我们的Math类，最先会找应用程序类加载器加载，应用程序类加载器会先委托扩展类加载器加载，扩展类加载器再委托引导类加载器，顶层引导类加载器在自己的类加载路径里找了半天没找到Math类，则向下退回加载Math类的请求，扩展类加载器收到回复就自己加载，在自己的类加载路径里找了半天也没找到Math类，又向下退回Math类的加载请求给应用程序类加载器， 应用程序类加载器于是在自己的类加载路径里找Math类，结果找到了就自己加载了。 

**双亲委派机制说简单点就是，先找父亲加载，不行再由儿子自己加载。**

我们来看下应用程序类加载器AppClassLoader加载类的双亲委派机制源码，AppClassLoader 的loadClass方法最终会调用其父类ClassLoader的loadClass方法，该方法的大体逻辑如下:

- 首先，检查一下指定名称的类是否已经加载过，如果加载过了，就不需要再加载，直接 返回。
- 如果此类没有加载过，那么，再判断一下是否有父加载器;如果有父加载器，则由父加 载器加载(即调用parent.loadClass(name, false);).或者是调用bootstrap类加载器来加载。
- 如果父加载器及bootstrap类加载器都没有找到指定的类，那么调用当前类加载器的 findClass方法来完成类加载。

源码如下：

```java
protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name); 
          	// 如果已经加载了，直接返回
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                  	// 如果父加载器不是null，由上层加载器负责加载
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }
								// 如果没有加载过，则去当前加载器的执行路径下寻找
                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    long t1 = System.nanoTime();
                    c = findClass(name);

                    // this is the defining class loader; record the stats
                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
```

### 1.5.2 为什么需要双亲委派？

- 沙箱安全机制:自己写的java.lang.String.class类不会被加载，这样便可以防止核心
- API库被随意篡改 避免类的重复加载:当父亲已经加载了该类时，就没有必要子ClassLoader再加载一次，保证被加载类的唯一性

#### 1.5.2.1 自定义的 java.lang.String能否被加载

肯定是不能加载的。

```java
package java.lang;
public class String{
public static void main(String[] args) { 
	System.out.println("**************My String Class**************");
}
```

运行结果：

```java
错误: 在类 java.lang.String 中找不到 main 方法, 请将 main 方法定义为: public static void main(String[] args)
否则 JavaFX 应用程序类必须扩展javafx.application.Application
```

为什么提示的是找不到main方法这个错误呢？

因为类加载加载的String类根本就不是我们自己定义的这个类，而是JDK的String，JDK中String的类中有没有main方法。

#### 1.5.2.2 为什么加载先是在app加载器而不是bootstrap加载器？

因为大部分代码都是需要app加载器来加载。

### 1.5.3 如何自定义类加载器

自定义类加载器只需要继承 java.lang.ClassLoader 类，该类有两个核心方法，一个是 loadClass(String, boolean)，实现了双亲委派机制，还有一个方法是findClass，默认实现是空方法，所以我们自定义类加载器主要是重写 方法。

```java
public class MyClassLoader extends ClassLoader {
    private String classPath;

    public MyClassLoader(String classPath) {
        this.classPath = classPath;
    }

    private byte[] loadByte(String name) throws Exception {
        name = name.replaceAll("\\.", "/");
        FileInputStream fis = new FileInputStream(classPath + "/" + name
                + ".class");
        int len = fis.available();
        byte[] data = new byte[len];
        fis.read(data);
        fis.close();
        return data;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = loadByte(name);
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }


    public static void main(String args[]) throws Exception {
        //初始化自定义类加载器，会先初始化父类ClassLoader，其中会把自定义类加载器的父加载器设置为应用程序类加载器AppClassLoader
        MyClassLoader classLoader = new MyClassLoader("/export/data");
        //export/data/com/ibli/jvm 几级目录，将User类的复制类User.class丢入该目录
        Class clazz = classLoader.loadClass("com.ibli.jvm.User");
        Object obj = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("sout", null);
        method.invoke(obj, null);
        System.out.println(clazz.getClassLoader().getClass().getName());
    }
}
```

定义一个User类

```java
public class User {
    public void sout(){
        System.out.println("user sout ...");
    }
}
```

输出结果：

```java
user sout ...
com.ibli.jvm.MyClassLoader
```

我现在要加载 `com.ibli.jvm.User`类，在 `/export/data/com/ibli/jvm`下有一个`User.class`。

我定义的加载器去加载的时候，上层的加载器们的各自路径下都没有这个类，所以最终肯定是由我自定义的类加载器来加载。

### 1.5.4 如何打破双亲委派

#### 1.5.4.1 什么是打破双亲委派

类通过子加载器加载 不用父加载器加载。

#### 1.5.4.2 如何打破

重写loadClass方法。

```java
protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);

            if (c == null) {
                // If still not found, then invoke findClass in order
                // to find the class.
                long t1 = System.nanoTime();

                c = findClass(name);

                // this is the defining class loader; record the stats
                sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                sun.misc.PerfCounter.getFindClasses().increment();
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
```

那我们就重写一下loadClass方法，那使用父加载器的逻辑删除掉就可以了。

但是会保一个错误，找不到Object类文件

```java
java.io.FileNotFoundException: /export/data/java/lang/Object.class (No such file or directory)
	at java.io.FileInputStream.open0(Native Method)
	at java.io.FileInputStream.open(FileInputStream.java:195)
```

为什么呢？ 因为在java中所有的类都有一个公共的基类 Object，加载的时候，如果有父类，肯定优先加载父类。

如何解决这个问题呢？

我们尝试一下在 `/export/data/java/lang/`路径下添加一下这个类，再次尝试。

```text
java.lang.SecurityException: Prohibited package name: java.lang
	at java.lang.ClassLoader.preDefineClass(ClassLoader.java:662)
	at java.lang.ClassLoader.defineClass(ClassLoader.java:761)
	at java.lang.ClassLoader.defineClass(ClassLoader.java:642)
	at com.ibli.jvm.MyClassLoader.findClass(MyClassLoader.java:32)
	at com.ibli.jvm.MyClassLoader.loadClass(MyClassLoader.java:59)
```

依然是不可以的，这里报错信息提示的是 `Prohibited package name: java.lang`。

这是为什么呢？

这里再一次印证了上面我们提到的，JDK自己定义的类文件绝对不允许自定义加载的，这个我们自己的Object是不可能被加载的。

如何解决上面的问题呢？

这里需要对Object特殊处理一下，Object就是用AppClassLoader来加载就行了。

```java
if (!name.startsWith("com.ibli.jvm")) {
      c = Launcher.getLauncher().getClassLoader().loadClass(name);
      // 或者 c = this.getParent().loadClass(name);
}else {
       c = findClass(name);
}
```

运行结果是：

```
user sout ...
com.ibli.jvm.MyClassLoader
```

### 1.5.5 Tomcat打破双亲委派？



#### 1.5.5.1 Tomcat 如果使用默认的双亲委派类加载机制行不行? 

我们思考一下: Tomcat是个web容器， 那么它要解决什么问题:

- 一个web容器可能需要部署两个应用程序，不同的应用程序可能会依赖同一个第三方类库的不同版本，不能要求同一个类库在同一个服务器只有一份，因此要保证每个应用程序的类库都是 独立的，保证相互隔离。
- 部署在同一个web容器中**相同的类库相同的版本**可以共享。否则，如果服务器有10个应用程 序，那么要有10份相同的类库加载进虚拟机。
-  web容器也有自己依赖的类库，不能与应用程序的类库混淆。基于安全考虑，应该让容器的类库和程序的类库隔离开来。
- web容器要支持jsp的修改，我们知道，jsp 文件最终也是要编译成class文件才能在虚拟机中 运行，但程序运行后修改jsp已经是司空见惯的事情， web容器需要支持 jsp 修改后不用重启。



#### 1.5.5.2 Tomcat为什么不使用默认的双亲委派

如果tomcat使用默认的双亲委派会有哪些问题：

第一个问题，**<font color=green>如果使用默认的类加载器机制，那么是无法加载两个相同类库的不同版本的，默认的类加器是不管你是什么版本的，只在乎你的全限定类名，并且只有一份</font>**。 

第二个问题，默认的类加载器是能够实现的，因为他的职责就是保证唯一性。 

第三个问题和第一个问题一样。 

我们再看第四个问题，我们想我们要怎么实现jsp文件的热加载，jsp 文件其实也就是class文件，那么如果修改了，但类名还是一样，类加载器会直接取方法区中已经存在的，修改后的jsp是不会重新加载的。那么怎么办呢? 我们可以直接卸载掉这jsp文件的类加载器，所以你应该想到了，每个jsp文件对应一个唯一的类加载器，当一个jsp文件修改了，就直接卸载这个jsp类加载器。重新创建类加载器，重新加载jsp文件。



#### 1.5.5.3 tomcat类加载流程

![](https://oscimg.oschina.net/oscnet/up-73a62d636202f492f8da71a9246c6d6639e.png)

Tomcat的几个主要类加载器:

- commonLoader: Tomcat最基本的类加载器，加载路径中的class可以被Tomcat容器本身以及各个Webapp访问; 

- catalinaLoader: Tomcat容器私有的类加载器，加载路径中的class对于Webapp不可见; 

- sharedLoader: 各个Webapp共享的类加载器，加载路径中的class对于所有Webapp可见，但是对于Tomcat容器不可见; 

- WebappClassLoader:各个Webapp私有的类加载器，加载路径中的class只对当前Webapp可见，比如加载war包里相关的类，每个war包应用都有自己的WebappClassLoader，实现相互隔离，比如不同war包应用引入了不同的spring版本， 这样实现就能加载各自的spring版本;



从图中的委派关系中可以看出: 

- CommonClassLoader能加载的类都可以被CatalinaClassLoader和SharedClassLoader使用， 从而实现了公有类库的共用
- CatalinaClassLoader和SharedClassLoader自己能加载的类则与对方相互隔离。
-  WebAppClassLoader可以使用SharedClassLoader加载到的类，但各个WebAppClassLoader 实例之间相互隔离。 
- 而JasperLoader的加载范围仅仅是这个JSP文件所编译出来的那一个.Class文件，它出现的目的就是为了被丢弃: **<font color=red>当Web容器检测到JSP文件被修改时，会替换掉目前的JasperLoader的实例， 并通过再建立一个新的Jsp类加载器来实现JSP文件的热加载功能。</font>**

tomcat 这种类加载机制违背了java 推荐的双亲委派模型了吗? 答案是:违背了。

**<font color=blue> 很显然，tomcat 为了实现隔离性，每个 webappClassLoader加载自己的目录下的class文件，不会传递给父类加载器，打破了双亲委派机制。</font>**



如何实现Tomcat的加载webapp的效果呢；

```java
public static void main(String args[]) throws Exception {
        //初始化自定义类加载器，会先初始化父类ClassLoader，其中会把自定义类加载器的父加载器设置为应用程序类加载器AppClassLoader
        MyClassLoader classLoader = new MyClassLoader("/export/data/");
        Class clazz = classLoader.loadClass("com.ibli.jvm.User");
        Object obj = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("sout", null);
        method.invoke(obj, null);
        System.out.println(clazz.getClassLoader().getClass().getName());

        MyClassLoader classLoader1 = new MyClassLoader("/export/data1/");
        Class clazz1 = classLoader.loadClass("com.ibli.jvm.User");
        Object obj1 = clazz.newInstance();
        Method method1 = clazz.getDeclaredMethod("sout", null);
        method.invoke(obj, null);
        System.out.println(clazz.getClassLoader().getClass().getName());
    }
```

注意：

- 两个User类的文件名相同，但是编译的时候里面的sout方法输出的内容是不同的，方便区分
- 构建了两个加载器

输出结果：

```
user sout ...
com.ibli.jvm.MyClassLoader

user1 sout ...
com.ibli.jvm.MyClassLoader
```

这里两个加载器其实不是一个实例，注意呦！

注意:**同一个JVM内，两个相同包名和类名的类对象可以共存，因为他们的类加载器可以不一样，所以看两个类对象是否是同一个，除了看类的包名和类名是否都相同之外，还需要他们的类 加载器也是同一个才能认为他们是同一个**。





# 2. JVM整体结构的深度剖析



## 2.1 JVM整体结构图

![](https://oscimg.oschina.net/oscnet/up-1cba664a7c4e0078f707899a9ee33412c3b.png)



JVM的整体结构如上面所示，主要分成3大部分：

- 类加载子系统
- 运行时数据区
- 字节码执行引擎

我们编写的程序，通过类加载子系统加载到运行时数据区，也就是内存中，然后通过字节码执行引擎来执行，这是一个简单的大体描述。其中，最为重要的也就是运行时数据区这一部分，东西非常重要，也非常多，和我们平时的优化也息息相关。

下面我们通过一个具体的实例来展开分别学习一下运行时数据区的每个部分

```java
public class Math {

    public static final int initData = 666;
    public static User1 user1 = new User1();

    public int compute() { //一个方法对应一块栈帧内存区域
        int a = 1;
        int b = 2;
        int c = (a + b) * 10;
        return c;
    }

    public static void main(String[] args) {
        Math math = new Math();
        math.compute();
    }
}
```



## 2.2 虚拟机栈

首先是虚拟机栈也叫做线程栈，其实就是之前我们经常说的，创建java线程的时候，会为每个线程在内存中存放一个单独的空间来存储线程私有的数据，其实就是线程栈。

Java虚拟机栈（Java Virtual Machine Stack）**是线程私有的**，它的生命周期与线程相同。虚拟机栈描述的是Java方法执行的线程内存模型：每个方法被执行的时候，Java虚拟机都会同步创建一个栈帧（Stack Frame）用于存储局部变量表、操作数栈、动态连接、方法出口等信息。每一个方法被调用直至执行完毕的过程，就对应着一个栈帧在虚拟机栈中从入栈到出栈的过程。

![](https://oscimg.oschina.net/oscnet/up-8bae23ae1d35aef8a88ea582d816340ed96.png)

### 2.2.1 局部变量表

局部变量表存放了编译期可知的各种Java虚拟机基本数据类型（boolean、byte、char、short、int、float、long、double）、对象引用（reference类型，它并不等同于对象本身，可能是一个指向对象起始地址的引用指针，也可能是指向一个代表对象的句柄或者其他与此对象相关的位置）和returnAddress类型（指向了一条字节码指令的地址）。

> 对于引用类型的对象，通常的说法是，对象的实例数据存放在堆中，而线程栈中存放的是这个对象的引用，也就是栈中存放的是这个对象在堆内存中实际的内存地址。
>
> 这种说法并非十分准确，实际真是的是，栈中存储的是这个对象在C++中对应对象的地址，然后那个C++对象才是真实指向对中的地址。

#### 2.2.1.1 局部变量表的数据怎么存储的？

这些数据类型在局部变量表中的存储空间以 **<font color=red>局部变量槽（Slot）</font>** 来表示，其中64位长度的long和double类型的数据会占用两个变量槽，其余的数据类型只占用一个。局部变量表所需的内存空间在编译期间完成分配，当进入一个方法时，这个方法需要在栈帧中分配多大的局部变量空间是完全确定的，在方法运行期间不会改变局部变量表的大小。

#### 2.2.1.2 Slot槽的特点

- 当一个实例方法被调用的时候，它的方法参数和方法体内部定义的局部变量将会按照声明顺序被复制到局部变量表中的每一个slot上

- 如果需要访问局部变量表中一个64bit的局部变量值时，只需要使用前一个索引即可。（比如：访问long或者double类型变量）

- 如果当前帧是由构造方法或者实例方法创建的（意思是当前帧所对应的方法是构造器方法或者是普通的实例方法），那么该对象引用this将会存放在index为0的slot处,其余的参数按照参数表顺序排列。**(非静态方法的局部变量表index=0的位置存放this指针)**

- 静态方法中不能引用this，是因为静态方法所对应的栈帧当中的局部变量表中不存在this

#### 2.2.1.3 Slot槽的重复利用

栈帧中的局部变量表中的槽位是可以重复利用的，如果一个局部变量过了其作用域，那么在其作用域之后申明的新的局部变量就很有可能会复用过期局部变量的槽位，从而达到节省资源的目的。

```java
private void test2() {
        int a = 0;
        {
            int b = 0;
            b = a+1;
        }
        //变量c使用之前以及经销毁的变量b占据的slot位置
        int c = a+1;
}
```

在以上情况下使用slot的数量为3个,this占0号、a单独占1个槽号、c重复使用了b的槽号。

### 2.2.2 操作数栈

#### 2.2.2.1 什么是操作数栈

相对于成员变量（或属性），如果是基本数据变量，则在栈中创建，随栈销毁而销毁。对象一般在堆中创建，栈中对象句柄为堆中对象的引用。

>  **逃逸分析可在栈中创建对象** 逃逸分析大概值的是对象永远只作用于当前方法（栈桢）的时候，对象的创建会选择直接在栈上，而不会在堆上创建。

是一个先进后出的栈结构,只要的作用是在程序运行期间存储计算所需要的值或者临时结果

在方法执行过程中，根据字节码指令，往栈中写入数据或提取数据，即入栈（push）或出栈（pop）,某些字节码指令将值压入操作数栈，其余的字节码指令将操作数取出栈，使用他们后再把结果压入栈。（如字节码指令bipush操作）比如：执行复制、交换、求和等操作。

```java
iconst_5 将int类型常量5压入栈
lconst_0 将long类型常量0压入栈
lconst_1 将long类型常量1压入栈
fconst_0 将float类型常量0压入栈
fconst_1 将float类型常量1压入栈
dconst_0 将double类型常量0压入栈
dconst_1 将double类型常量1压入栈
bipush 将一个8位带符号整数压入栈
sipush 将16位带符号整数压入栈
ldc 把常量池中的项压入栈
ldc_w 把常量池中的项压入栈(使用宽索引)
ldc2_w 把常量池中long类型或者double类型的项压入栈(使用宽索引) 从栈中的局部变量中装载值的指令
iload 从局部变量中装载int类型值
```

上面是一些局部变量的操作指令。

比如上面Math的方法：

```java
public int compute() { //一个方法对应一块栈帧内存区域
        int a = 1;
        int b = 2;
        int c = (a + b) * 10;
        return c;
}
```

经过反汇编之后可以看到这个java代码对应的jvm指令：

```java
public int compute();
    Code:
       0: iconst_1
       1: istore_1
       2: iconst_2
       3: istore_2
       4: iload_1
       5: iload_2
       6: iadd
       7: bipush        10
       9: imul
      10: istore_3
      11: iload_3
      12: ireturn
```



#### 2.2.2.2 操作数栈有什么特点

- 操作数栈，**主要用于保存计算过程的中间结果，同时作为计算过程中变量临时的存储空间**。

- 操作数栈就是jvm执行引擎的一个工作区，当一个方法开始执行的时候，一个新的栈帧也会随之被创建出来，这个方法的操作数栈是空的
- 每一个操作数栈都会拥有一个明确的栈深度用于存储数值，其所需的**最大深度在编译期就定义好了**，保存在方法的code属性中，为max_stack的值。
- 栈中的任何一个元素都是可以任意的java数据类型.32bit的类型占用一个栈单位深度,64bit的类型占用两个栈深度单位
- 操作数栈并非采用访问索引的方式来进行数据访问的，而是只能通过标准的入栈push和出栈pop操作来完成一次数据访问
- 如果被调用的方法带有返回值的话，其返回值将会被压入当前栈帧的操作数栈中，并更新PC寄存器中下一条需要执行的字节码指令。
- 操作数栈中的元素的数据类型必须与字节码指令的序列严格匹配，这由编译器在编译期间进行验证，同时在类加载过程中的类验证阶段的数据流分析阶段要再次验证。



### 2.2.3 动态链接

上面讲类加载的时候应该提到过，在解析阶段 ，会把符号引用设置成直接引用。

```java
public static void main(String[] args) {
        Math math = new Math();
        math.compute();
}
```

比如main方法，就是静态符号 ，而 `math.compute();`则只能在运行时动态获取。

> 这部分涉及到了java特性中的 **多态**

#### 2.2.3.1 什么是静态链接｜动态链接

- **静态链接**

当一个 字节码文件被装载进JVM内部时，如果被调用的目标方法在编译期可知，且运行期保持不变时。这种情况下将调用方法的符号引用转换为直接引用的过程称之为静态链接。

- **动态链接**

如果被调用的方法在编译期无法被确定下来，也就是说，只能够在程序运行期将调用方法的符号引用转换为直接引用，由于这种引用转换过程具备动态性，因此也就被称之为动态链接。

#### 2.2.3.2 什么是绑定？

对应的方法的绑定机制为：**早起绑定**（Early Binding）和**晚期绑定**（Late Bingding）。绑定是一个字段、方法或者类在符号引用被替换为直接引用的过程，这仅仅发生一次。

- **早期绑定**

早期绑定就是指被调用的目标方法如果在编译期可知，且运行期保持不变时，即可将这个方法与所属的类型进行绑定，这样一来，由于明确了被调用的目标方法究竟是哪一个，因此也就可以使用静态链接的方式将符号引用转换为直接引用。

- **晚期绑定**

如果被调用的方法在编译期无法被确定下来，只能够在程序运行期根据实际的类型绑定相关的方法，这种绑定方式也就被称之为晚期绑定。

#### 2.2.3.3 方法调用是怎么实现的

- 普通调用指令：

  - 1.`invokestatic`：调用静态方法，解析阶段确定唯一方法版本；

  - 2.`invokespecial`:调用方法、私有及父类方法，解析阶段确定唯一方法版本；

  - 3.`invokevirtual`调用所有虚方法；

  - 4.`invokeinterface`：调用接口方法；

- 动态调用指令（Java7新增）：
  - 5.`invokedynamic`：动态解析出需要调用的方法，然后执行

前四条指令固化在虚拟机内部，方法的调用执行不可人为干预，而`invokedynamic`指令则支持由用户确定方法版本。

其中invokestatic指令和invokespecial指令调用的方法称为非虚方法；

其中invokevirtual（final修饰的除外，JVM会把final方法调用也归为invokevirtual指令，但要注意final方法调用不是虚方法）、invokeinterface指令调用的方法称称为虚方法。


如何验证上面所说的呢？

```java
/**
 * 解析调用中非虚方法、虚方法的测试
 */
class Father {
    public Father(){
        System.out.println("Father默认构造器");
    }

    public static void showStatic(String s){
        System.out.println("Father show static"+s);
    }

    public final void showFinal(){
        System.out.println("Father show final");
    }

    public void showCommon(){
        System.out.println("Father show common");
    }

}

public class Son extends Father{
    public Son(){
        super();
    }

    public Son(int age){
        this();
    }

    public static void main(String[] args) {
        Son son = new Son();
        son.show();
    }

    //不是重写的父类方法，因为静态方法不能被重写
    public static void showStatic(String s){
        System.out.println("Son show static"+s);
    }

    private void showPrivate(String s){
        System.out.println("Son show private"+s);
    }

    public void show(){
        //invokestatic
        showStatic(" 大头儿子");
        //invokestatic
        super.showStatic(" 大头儿子");
        //invokespecial
        showPrivate(" hello!");
        //invokespecial
        super.showCommon();
        //invokevirtual 因为此方法声明有final 不能被子类重写，所以也认为该方法是非虚方法
        showFinal();
        //虚方法如下
        //invokevirtual
        showCommon();//没有显式加super，被认为是虚方法，因为子类可能重写showCommon
        info();

        MethodInterface in = null;
        //invokeinterface  不确定接口实现类是哪一个 需要重写
        in.methodA();

    }

    public void info(){

    }

}

interface MethodInterface {
    void methodA();
}

```

然后反汇编Son.class ， 得到如下字节码指令文件：

```java
Compiled from "Son.java"
public class com.ibli.jvm.Son extends com.ibli.jvm.Father {
  public com.ibli.jvm.Son();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method com/ibli/jvm/Father."<init>":()V
       4: return

  public com.ibli.jvm.Son(int);
    Code:
       0: aload_0
       1: invokespecial #2                  // Method "<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: new           #3                  // class com/ibli/jvm/Son
       3: dup
       4: invokespecial #2                  // Method "<init>":()V
       7: astore_1
       8: aload_1
       9: invokevirtual #4                  // Method show:()V
      12: return

  public static void showStatic(java.lang.String);
    Code:
       0: getstatic     #5                  // Field java/lang/System.out:Ljava/io/PrintStream;
       3: new           #6                  // class java/lang/StringBuilder
       6: dup
       7: invokespecial #7                  // Method java/lang/StringBuilder."<init>":()V
      10: ldc           #8                  // String Son show static
      12: invokevirtual #9                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      15: aload_0
      16: invokevirtual #9                  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;
      19: invokevirtual #10                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;
      22: invokevirtual #11                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
      25: return

  public void show();
    Code:
       0: ldc           #13                 // String  大头儿子
       2: invokestatic  #14                 // Method showStatic:(Ljava/lang/String;)V
       5: ldc           #13                 // String  大头儿子
       7: invokestatic  #15                 // Method com/ibli/jvm/Father.showStatic:(Ljava/lang/String;)V
      10: aload_0
      11: ldc           #16                 // String  hello!
      13: invokespecial #17                 // Method showPrivate:(Ljava/lang/String;)V
      16: aload_0
      17: invokespecial #18                 // Method com/ibli/jvm/Father.showCommon:()V
      20: aload_0
      21: invokevirtual #19                 // Method showFinal:()V
      24: aload_0
      25: invokevirtual #20                 // Method showCommon:()V
      28: aload_0
      29: invokevirtual #21                 // Method info:()V
      32: aconst_null
      33: astore_1
      34: aload_1
      35: invokeinterface #22,  1           // InterfaceMethod com/ibli/jvm/MethodInterface.methodA:()V
      40: return

  public void info();
    Code:
       0: return
}
```



### 2.2.4 方法出口

1.调用者（方法的调用者可能也是一个方法）的pc计数器的值作为返回地址，即调用该方法的指令的下一条指令的地址。而通过异常退出时，返回地址是要通过异常表来确定，栈帧中一般不会保存这部分信息。

2.在方法执行的过程中遇到了异常（Exception），并且这个异常没有在方法内进行处理，也就是只要在本方法的异常表中没有搜素到匹配的异常处理器，就会导致方法退出，简称异常完成出口方法执行过程中抛出异常时的异常处理，存储在一个异常处理表，方便在发生异常的时候找到处理异常的代码。




## 2.2 程序计数器

程序计数器之前也被称为PC寄存器。

程序计数器（Program Counter Register）是一块较小的内存空间，它可以看作是当前线程所执行的字节码的行号指示器。在Java虚拟机的概念模型里 ，字节码解释器工作时就是通过改变这个计数器的值来选取下一条需要执行的字节码指令，它是程序控制流的指示器，分支、循环、跳转、异常处理、线程恢复等基础功能都需要依赖这个计数器来完成。

由于Java虚拟机的多线程是通过线程轮流切换、分配处理器执行时间的方式来实现的，在任何一个确定的时刻，一个处理器（对于多核处理器来说是一个内核）都只会执行一条线程中的指令。因此，为了线程切换后能恢复到正确的执行位置，每条线程都需要有一个独立的程序计数器，各条线程之间计数器互不影响，独立存储，我们称这类内存区域为“线程私有”的内存。

如果线程正在执行的是一个Java方法，这个计数器记录的是正在执行的虚拟机字节码指令的地址；如果正在执行的是本地（Native）方法，**这个计数器值则应为空**（Undefined）。

**<font color=red>此内存区域是唯一一个在《Java虚拟机规范》中没有规定任何OutOfMemoryError情况的区域。</font>**



## 2.3 本地方法栈

本地方法栈（Native Method Stacks）与虚拟机栈所发挥的作用是非常相似的，其区别只是虚拟机栈为虚拟机执行Java方法（也就是字节码）服务，而本地方法栈则是为虚拟机使用到的本地（Native）方法服务。

![](https://oscimg.oschina.net/oscnet/up-8bae23ae1d35aef8a88ea582d816340ed96.png)

**本地方法栈也是线程私有的。**



## 2.4 方法区

### 2.4.1 什么是方法区

方法区（Method Area）与Java堆一样，是各个线程共享的内存区域，它用于存储已被虚拟机加载的类型信息、常量、静态变量、即时编译器编译后的代码缓存等数据。虽然《Java虚拟机规范》中把方法区描述为堆的一个逻辑部分，但是它却有一个别名叫作“非堆”（Non-Heap），目的是与Java堆区分开来。

### 2.4.2 方法区的参数配置

关于元空间的JVM参数有两个：-XX:MetaspaceSize=N和 -XX:MaxMetaspaceSize=N
-XX：MaxMetaspaceSize： 设置元空间最大值， 默认是-1， 即不限制， 或者说只受限于本地内存大小。
-XX：MetaspaceSize： 指定元空间触发Fullgc的初始阈值(元空间无固定初始大小)， 以字节为单位，默认是21M左右，达到该值就会触发full gc进行类型卸载， 同时收集器会对该值进行调整： 如果释放了大量的空间， 就适当降低该值； 如果释放了很少的空间， 那么在不超过-XX：MaxMetaspaceSize（如果设置了的话） 的情况下， 适当提高该值。

> 这个跟早期jdk版本的-XX:PermSize参数意思不一样，-XX:PermSize代表永久代的初始容量。

由于调整元空间的大小需要Full GC，这是非常昂贵的操作，如果应用在启动的时候发生大量Full GC，通常都是由于永久代或元空间发生了大小调整，基于这种情况，一般建议在JVM参数中将MetaspaceSize和MaxMetaspaceSize设置成一样的值，并设置得比初始值要大，对于8G物理内存的机器来说，一般我会将这两个值都设置为256M。

**Full GC会同时收集方法区和堆**

## 2.5 堆

### 2.5.1 堆的分区结构

![](https://oscimg.oschina.net/oscnet/up-b0c839ade955bb783a355f34bf004bdab86.png)



Java堆（Java Heap）是虚拟机所管理的内存中最大的一块。Java堆是被所有线程共享的一块内存区域，在虚拟机启动时创建。此内存区域的唯一目的就是存放对象实例，Java世界里“几乎”所有的对象实例都在这里分配内存。

Java堆是垃圾收集器管理的内存区域，因此一些资料中它也被称作“GC堆

> 如果从分配内存的角度看，所有线程共享的Java堆中可以划分出多个线程私有的分配缓冲区（Thread Local Allocation Buffer，TLAB），以提升对象分配时的效率。

### 2.5.2 为什么eden和service的配比是8:1

 这个和垃圾回收机制有关，主要考虑到大部分对象都是“朝生暮死”，每次垃圾回收的时候，都回收 eden + 1个service区的对象，而剩余的对象移动到另一个service区。

也有一些对象因为某些原因，比如分代年龄达到15，会被移动到老年代。

这个8:1能够更大限度的利用堆的资源。

### 2.5.3 什么是垃圾回收

这个下面会有专门的垃圾回收章节展开。主要是清理内存中没用的对象，是释放内存。至于判断哪些对象是“垃圾”，有引用计数法和可达性分析等。

不同分区的对象，根据它的特性也有不同的垃圾回收算法和垃圾收集器。

这里我们只需要堆是垃圾回收的主要区域，当然方法区也会进行垃圾回收。

### 2.5.4 大名鼎鼎的STW

#### 2.5.4.1 什么是stw？

Stop the world 的简称。就是在垃圾收集器进行Full GC的时候，需要暂停用户线程，对于用户来说，好像服务停止了一样。

当然这个时间是很短暂的。我们进行调优的时候，主要的目标就是为了尽量缩短STW的时间间隔和减少Young GC的次数。

#### 2.5.4.2 为什么会有stw？

因为GC的过程中，如果还有用户线程工作的话，势必会产生新的对象，甚至新的垃圾。这样会对垃圾回收过程造成干扰，无法判断哪些是新生成的垃圾对象。

**<font color=red>静态的对象是如何存储的？ 引用是存储在方法区的  对象实际是存储在堆的</font>**

## 2.6 直接内存

直接内存（Direct Memory）并不是虚拟机运行时数据区的一部分，也不是《Java虚拟机规范》中定义的内存区域。**但是这部分内存也被频繁地使用，而且也可能导致OutOfMemoryError异常出现**。

在JDK 1.4中新加入了NIO（New Input/Output）类，引入了一种基于通道（Channel）与缓冲区（Buffer）的I/O方式，它可以使用Native函数库直接分配堆外内存，然后通过一个存储在Java堆里面的DirectByteBuffer对象作为这块内存的引用进行操作。这样能在一些场景中显著提高性能，因为避免了在Java堆和Native堆中来回复制数据。

显然，本机直接内存的分配不会受到Java堆大小的限制，但是，既然是内存，则肯定还是会受到本机总内存（包括物理内存、SWAP分区或者分页文件）大小以及处理器寻址空间的限制，一般服务器管理员配置虚拟机参数时，会根据实际内存去设置-Xmx等参数信息，但经常忽略掉直接内存，使得各个内存区域总和大于物理内存限制（包括物理的和操作系统级的限制），从而导致动态扩展时出现OutOfMemoryError异常。

## 2.7 运行时错误

### 2.7.1 OOM

#### 2.7.1.1 什么是OOM

OOM，全称“Out Of Memory”，翻译成中文就是“内存用完了”，来源于java.lang.OutOfMemoryError。看下关于的官方说明： Thrown when the Java Virtual Machine cannot allocate an object because it is out of memory, and no more memory could be made available by the garbage collector. 意思就是说，当JVM因为没有足够的内存来为对象分配空间并且垃圾回收器也已经没有空间可回收时，就会抛出这个**error**（注：非exception，因为这个问题已经严重到不足以被应用处理）。

#### 2.7.1.2 哪些地方会发生OOM

按照JVM规范，JAVA虚拟机在运行时会管理以下的内存区域：

- JAVA虚拟机栈：Java方法执行的内存模型，每个Java方法的执行对应着一个栈帧的进栈和出栈的操作。
- 本地方法栈：类似“ JAVA虚拟机栈 ”，但是为native方法的运行提供内存环境。
- JAVA堆：对象内存分配的地方，内存垃圾回收的主要区域，所有线程共享。可分为新生代，老生代。
- 方法区：用于存储已经被JVM加载的类信息、常量、静态变量、即时编译器编译后的代码等数据。Hotspot中的“永久代”。
- 运行时常量池：方法区的一部分，存储常量信息，如各种字面量、符号引用等。
- 直接内存：并不是JVM运行时数据区的一部分， 可直接访问的内存， 比如NIO会用到这部分。

程序计数器：当前线程执行的字节码的行号指示器，线程私有，按照JVM规范，除了程序计数器不会抛出OOM外，其他各个内存区域都可能会抛出OOM。

#### 2.7.1.3 什么情况下会发生OOM

**内存泄露**：申请使用完的内存没有释放，导致虚拟机不能再次使用该内存，此时这段内存就泄露了，因为申请者不用了，而又不能被虚拟机分配给别人用。

**内存溢出**：申请的内存超出了JVM能提供的内存大小，此时称之为溢出。

**超大对象**：通常是一个大的数组

#### 2.7.1.4 解决办法

- 如果是超大对象，可以检查其合理性，比如是否一次性查询了数据库全部结果，而没有做结果数限制
- 如果是业务峰值压力，可以考虑添加机器资源，或者做限流降级。
- 如果是内存泄漏，需要找到持有的对象，修改代码设计，比如关闭没有释放的连接。
- 根据业务合理配置堆内存参数

### 2.7.2 StackOverFlow

#### 2.7.2.1 什么是StackOverFlow

StackOverflowError 是一个java中常出现的错误：在jvm运行时的数据区域中有一个java虚拟机栈，当执行java方法时会进行压栈弹栈的操作。在栈中会保存局部变量，操作数栈，方法出口等等。jvm规定了栈的最大深度，当执行时栈的深度大于了规定的深度，就会抛出StackOverflowError错误。

#### 2.7.2.2 哪些地方会发生StackOverFlow

- 虚拟机栈/线程栈
- 本地方法栈

#### 2.7.2.3 什么情况下会发生StackOverFlow

- 无限递归循环调用（最常见）。
- 执行了大量方法，导致线程栈空间耗尽。
- 方法内声明了海量的局部变量。
- native 代码有栈上分配的逻辑，并且要求的内存还不小，比如 java.net.SocketInputStream.read0 会在栈上要求分配一个 64KB 的缓存（64位 Linux）。

#### 2.7.2.4 解决办法

常见的解决方法包括以下几种：

- 修复引发无限递归调用的异常代码， 通过程序抛出的异常堆栈，找出不断重复的代码行，按图索骥，修复无限递归 Bug。
- 排查是否存在类之间的循环依赖。
- 排查是否存在在一个类中对当前类进行实例化，并作为该类的实例变量。
- 通过 JVM 启动参数 -Xss 增加线程栈内存空间， 某些正常使用场景需要执行大量方法或包含大量局部变量，这时可以适当地提高线程栈空间限制，例如通过配置 -Xss2m 将线程栈空间调整为 2 mb。



方法区是内存连续的吗？



# 3.Java内存分配机制



什么是逃逸分析 默认开启



什么是标量替换

对象的属性拆散 栈桢只存出属性 

对象创建分配的内存是连续的吗



上面的两个例子？？？？



大对象直接进入老年代？什么是大对象？

eden都放不下 ？有个参数去控制

为什么这么设计？

有两种垃圾收集器支持收集大对象 series parNew



长期存活的对象进入老年代？



对象动态年龄判断



如何设置jvm参数

参考因素：系统硬件参数  还要考虑压力参数 并发量



年龄动态判断机制



空间担保机制



如何判断对象是否是垃圾 

引用计数算法 以及 四种引用











































[jvm结构](https://www.processon.com/view/5fb5d9e4e0b34d0d2241b8ac?fromnew=1)

[JVM 深入理解Java虚拟机](https://www.processon.com/view/5f781d8763768906e65d5b4d?fromnew=1)

**[JVM (三)运行时数据区](https://juejin.cn/post/6935362175322030088)**



























