---
title: Java泛型
toc: false
date: 2021-07-27 19:39:32
tags: Java
categories: [Develop Lan,Java]
---

## Java泛型


## 1、泛型定义
> 使用泛型机制编写的程序代码要比那些杂乱地使用Object变量，然后在进行强制类型转换的代码具有更好的安全性和可读性。  --《Java核心技术》  


泛型是在编译时期作用的；

泛型变量使用大写形式，在Java库中，一般使用变量E表示集合的元素类型，K和V表示表的关键字与值的类型。


## 2、通配符
### 2.1 无边界通配符
无边界通配符又成为非限定通配符
```java
public static void main(String[] args) {
        List<String> list1 = new ArrayList<>();
        list1.add("1");
        list1.add("2");
        list1.add("3");
        list1.add("4");
        loop(list1);
    }

    public static void loop(List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
```


### 2.2 上边界通配符
上边界通配符和下边界通配符都属于限定通配符
```java
public static void main(String[] args) {
        //List中的类型必须是Number的子类，不然会报编译错误
        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(2);
        list1.add(3);
        list1.add(4);
        loop(list1);
    }

    // 传进来的list的类型必须是Number或Number的子类才可以
    public static void loop(List<? extends Number> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
```
> ? extends Number   
> 如果限定的类型有多个，之间使用 & 进行分割

### 2.3 下边界通配符

```java
public static void main(String[] args) {
        //List的泛型是Number 添加的元素只要是Number下的类型就可以
        List<Number> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(2L);
        list1.add(new BigDecimal(22));
        list1.add(4);
        loop(list1);
    }

    /**
     * 通用类型必须是Number到Object之间的类型
     *
     * @param list
     */
    public static void loop(List<? super Number> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
```


## 3、泛型的使用
> 泛型必须先声明，再使用，不然会有编译错误；  
> 泛型的声明是用过一对<>来完成，约定使用一个大写的字母来表示;  
> 通配符不能用作返回值;

```java
public <T> T testA(T t, Test1<T> test1) {
    System.out.println("这是传入的T:" + t);
    t = test1.t;
    System.out.println("这是赋值后的T:" + t);
    return t;
}
```
- 要从泛型类取数据时，用extends；
- 要往泛型类写数据时，用super；
- 既要取又要写，就不用通配符（即extends与super都不用）。





### 3.1 泛型类
```java
public class Demo<K, V> {
    public <K> K test(V v) {
        return null;
    }
}
```


### 3.2 泛型方法

```java
public class DemoTest4<K, V> {

    /**
     * <T> 代表泛型的声明
     *
     * @param t   本方法声明的泛型类型
     * @param <T> 本方法声明的泛型类型
     * @return
     */
    public <T> T test(T t) {
        return null;
    }

    /**
     * 普通的泛型方法
     *
     * @param k   类中定义的泛型类型
     * @param <X> 本方法中声明的泛型类型
     * @return
     */
    public <X> X aa(K k) {
        return (X) null;
    }

    /**
     * 静态方法中是无法使用类中声明的泛型类型的
     * 可以使用在本方法中声明的泛型类型
     *
     * @return
     */
    public static <X> X bb() {
        return null;
    }

}
```

### 3.3 泛型接口
首先看一下不使用泛型接口的Demo
```java
先定义接口，声明两个方法
public interface IGeneric {
    Integer aa(Integer a);
    
    Integer bb(Integer b);
}

//然后创建一个类来实现方法：
public class IntegerDemo implements IGeneric{

    @Override
    public Integer aa(Integer a) {
        return null;
    }

    @Override
    public Integer bb(Integer b) {
        return null;
    }

}

```
上面是没有使用泛型的接口设计，但是aa方法的操作类型相当于在接口中写死了，如果此时我们需要一个String类型的aa方法，那是不是还要在声明一个String类型的接口，然后再去实现呢，这样是不是显得代码很臃肿，代码重复；  
所以我们可以看一下使用泛型之后是怎么样的。

```java
定义泛型接口
public interface IGenericInte<T> {
    T aa(T a);
    T bb(T b);
}

下面是根据不同类型的实现类
泛型传如Integer类型
public class IGenericInteger implements IGenericInte<Integer> {
    @Override
    public Integer aa(Integer a) {
        return null;
    }

    @Override
    public Integer bb(Integer b) {
        return null;
    }
}

泛型传入String类型
public class IGenericString implements IGenericInte<String> {
    @Override
    public String aa(String a) {
        return null;
    }

    @Override
    public String bb(String b) {
        return null;
    }
}
```


## 4、泛型擦除
在虚拟机上没有泛型类型对象，所有的对象都属于普通类。Java在处理泛型类型的时候，会处理成一个相应的原始类型。  擦除类型变量，并替换为限定类型，如果没有限定类型，默认使用Object替代。如果有限定类型，并且是多个，会使用第一个限定的类型来替换。

```java
public interface IGenericInte<T> {
    T aa(T a);
    T bb(T b);
}
```
像上面这个T是一个无限定的变量，泛型擦除之后会直接使用Object替换。
当然调用泛型方法时，如果擦除返回类型，编译器插入强制类型转换

```java
Pair<Employee> buddies = ....
Employee buddy = buddies.getFirst();
```
擦除getFirst的返回类型后将返回Object类型。编译器自动插入Employee的强制类型转换，也就是说，编译器调用方法是其实是执行了一下两个虚拟机指令：
- 对原始方法Pair.getFirst()方法的调用
- 将返回的Object类型强制转换为Employee类型

```java
public static <T extends Comparable> T foo(T [] args)
```
在擦除类型之后变成：
```java
public static Comparable T foo(Comparable [] args)
```
参数类型T已经被擦除，只留下限定类型Comparable;

> 总之有关Java泛型转换的事实：
>- 虚拟机没有泛型，只有普通的类和方法
>- 所有的类型参数都用它们的限定类型替换
>- ==桥方法被合成来保证多态==
>- 为了保持类型安全型，必要时插入强制类型转换

第一条应该很好理解，这也是为什么会有泛型擦除这个概念，是因为JVM不能操作泛型；  
第二条就是解释泛型如何进行类型的擦除；  
第三条是泛型方法可能与多态的理念矛盾，所以使用桥方法来过渡或兼容；  
第四条上面也有提到，会出现强制类型转换的情况；  



## 5、泛型的约束与局限性
当然泛型的设计在java中并没有那么完美，它确实可以解决代码结构重用等问题，但是也是有一些局限性，下面是我根据《Java核心技术》进行的总结：

### 5.1 不能使用基础数据类型实例化类型参数
原因是类型擦除之后，如果使用Object原始类型，Object是无法存储基本数据类型的值。所以只能通过其包装类型声明；
### 5.2 运行时查询类型只适用与原始类型
```java
public class DemoTest5<T> {
    public static void main(String[] args) {
        DemoTest5<String> demoTest5 = new DemoTest5<>();
        DemoTest5<Integer> demoTest4 = new DemoTest5<>();
        System.err.println(demoTest4.getClass().equals(demoTest5.getClass()));
    }
}
```
demoTest4.getClass().equals(demoTest5.getClass())其实比较的是DemoTest5这个类类型，我们输出一下demoTest4.getClass()的结果看一下：
```
class com.ibli.javaBase.generics.DemoTest5
```
所以这里有一道非常经典的面试题，[如何判断一个泛型他的具体类型是什么，这里我们可以使用反射去拿到泛型的具体类型；](https://blog.csdn.net/IBLiplus/article/details/108672223)

### 5.3 不能创造参数化类型的数组
对于参数化类型的数组，在类型擦除之后，会变成Object[]类型，如果此时试图存储一个String类型的元素，就会抛出一个Array-StoreException异常；  
主要目的还是处于到数组安全的保护，可以参考几篇文章:  
> [1、如果Java不支持参数化类型数组，那么Arrays.asList()如何处理它们？](https://cloud.tencent.com/developer/ask/195960)  
> [2、java不能创建参数化类型的泛型数组](https://blog.csdn.net/qq_41286138/article/details/105250938)  
> [3、java.lang.ArrayStoreException](https://www.cnblogs.com/shuilangyizu/p/5916402.html)

### 5.4 Varargs警告
向参数个数可变的方法传递一个泛型类型的实例的场景，编译器会发出警告！
抑制这种警告的方式有两种：
- 在调用方法上增加注解@SuppressWarnings("unchecked")
- 还可以使用@SafeVarargs注解直接标注方法
> 参考 [java不能创建参数化类型的泛型数组](https://blog.csdn.net/qq_41286138/article/details/105250938)  



### 5.5 不能实例化类型变量
不能使用new T(..) 或则new T[...]和T.class这样的表达式的类型变量；因为类型擦除后，T变成Object，显然我们在这里并不是想要创建一个Object实例。解决办法是在调用者提供一个构造器表达式，下面是用Supplier函数实现：

```java
public class Pair<T> {

        private T first;
        private T second;

        public T getFirst() {
            return first;
        }

        public void setFirst(T first) {
            this.first = first;
        }

        public T getSecond() {
            return second;
        }

        public void setSecond(T second) {
            this.second = second;
        }

        public Pair(T first, T second) {
            this.first = first;
            this.second = second;
        }

        public static <T> Pair<T> build(Supplier<T> constr) {
            return new Pair<>(constr.get(), constr.get());
        }

        /**
         * Cannot infer type arguments for Pair2<>
         * 当函数头返回值为Pair时,无法推断,改为Pair2后可以推断.
         * @param c1
         * @return
         */
        public static <T> Pair<T> build(Class<T> c1){
            try {
                return new Pair<>(c1.newInstance(),c1.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }
}
```
Supplier是一个函数接口，返回一个无参数并且返回类型为T的函数：
```java
@FunctionalInterface
public interface Supplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}
```

```java
public class TestMakePair {

    public static void main(String[] args) {

        /**
         * 1.接受Supplier<T>--它是一个函数式接口。表示无参数且返回类型为T的函数。
         * 因为不能实例化类型变量，如：
         * public Pair() {first = new T();second = new T();}
         * 所以最好的方式是让调用者提供一个构造器表达式.形式如下:
         * @param constr
         * @return
         */
        Pair<String> pair = Pair.build(String::new);
        System.out.println(pair.getFirst().length());

        /**
         * public void buildT(){
         2.传统的方式是通过Class.newInstance方法来构造泛型对象.
         但由于细节过于复杂,T.class是不合法的.它会被擦除为Object.class.如下:
         Illegal class literal for the type parameter T
         T.class.newInstance();
         }
         * 3.
         * T.class是不合法的,但若API涉及如下
         * reason:因为String.class是Class<String>的一个实例.
         */
        Pair<String> pair1 = Pair.build(String.class);
        System.out.println(pair1.getFirst().length());
    }
}

执行结果：
0
0
```


### 5.6 不能构造泛型数组

就像不能实例化一个泛型实例一样，也不能实例化数组。数组本身也有类型，用来监控存储在JVM中的数组，这个类型会被擦除，例如：
```java
public static <T extends Comparable> T[] foo(T[] a){
    T[] mm = new T[2];
    ...
}
```
类型擦除，会让这个方法永远构造Comparabel[2]数组；

### 5.7 泛型类的静态上下文中类型变量无效
这个应该是比较好理解的，上文也提到过了，泛型类型是作用在泛型类上的，一些静态的方法或这静态的属性不能够使用泛型类的变量类型，编译器会直接报错；


### 5.8 不能抛出或者捕获泛型类的实例
Java既不能抛出也不能捕获泛型类对象，实际上，甚至泛型类扩展Throwable都是不合法的。
```java
public static <T extends Throwable> void doWork(Class<T> t){
    try{
        ...
    }catch (T ex){  此处无法捕获    catch必须捕获具体的异常
        ....
    }
}

```

在异常规范中使用类型变量是允许的，如下：

```java
public static <T extends Throwable> void doWork(Class<T> t) throws T {
  try{
        ...
    }catch (Throwable ex){  
        t.initCause(ex);
        throw t;
    }
}

```

### 5.9 可以消除对受查异常的检查

Java异常处理要求必须为所有的受查异常提供一个处理器，但是使用泛型，可以规避这一点；

```java
@SuppressWarnings("unchecked")
public static <T extends Throwable> void throwAs(Throwable e) throws T{
    throw (T)e;
}
```

调用上面的方法，编译器会认为t是一个非受查异常;

### 5.10 注意擦除后的冲突
比如一个泛型类的equals方法，擦除之后，和Object的equals冲突；解决办法是重新命名引发错误的方法；



## 6、泛型的继承关系

如果Manage extends Employee,那么Pair< Manage >是Pair< Employee >的子类吗？ 不是的！
但是泛型类可以扩展或实现其他的泛型类，很典型的一个例子ArrayList:
```
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable{}
```
ArrayList[E]继承了AbstractList[E];


## 对于Java泛型的一些思考
> 编译器如何推断出具体的类型？ 参考资料：[深入理解 Java 泛型](https://blog.csdn.net/u011240877/article/details/53545041#擦除的实现原理)



> <p align="middle"> -------------------   他日若遂凌云志 敢笑黄巢不丈夫 ------------------- </p>
> 