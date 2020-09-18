> 个人技术博客
[CSDN](https://blog.csdn.net/IBLiplus)      [Github](https://github.com/GaoLeiplus/javaDancer)
[掘金](https://juejin.im/user/4424090522228919)

![](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1600431374677&di=df4d8b050b37c3dd26ebd4be5f8db182&imgtype=0&src=http%3A%2F%2F5b0988e595225.cdn.sohucs.com%2Fimages%2F20181030%2Fe77642a513614a56bd12943124f89341.jpg)

 ## 反射
> 反向探知，在程序运行是动态的获取类的相关属性
这种动态获取类的内容以及动态调用对象的方法和获取属性的机制，叫做java反射机制；

### 反射的优缺点
> 优点  
> 增加了程序的灵活性，避免的固有逻辑写死到程序中
> 代码简介，提高程序的复用性


> 缺点  
> 相比于直接调用，反射有比较大的性能消耗  
> 内部暴露和安全隐患  （因为反射可以操作private成员变量和调用private成员方法）



### 反射的基本操作

#### 获取类对象的4种方式

```
// 调用forName方法得到一个对象，这也是最容易想到的方式
Class<?> object = Class.forName("com.ibli.javaBase.reflection.User");

// 通过实例对象调用getClass方法
Teacher teacher = new Teacher();
Class<?> objectT = teacher.getClass();

// 通过类加载器的方式
Class<?> loader = ClassLoader.getSystemClassLoader().loadClass("com.ibli.javaBase.reflection.User");

//通过一个类.class
Class<?> tt = Teacher.class;
```

#### 基本信息操作


| 类修饰符 | PUBLIC | PRIVATE | PROTECTED | STATIC | FINAL | SYNCHRONIZED | VOLATILE | TRANSIENT | NATIVE | INTERFACE | ABSTRACT |  
| :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: | :----: |
| modifiers | 1 | 2 | 4 |  8 | 16 | 32 | 64 | 128 | 256 | 512 | 1024 |


```
// 类的修饰符 具体的值可以参考JDK API文档中的定义 返回值是int类型  public：1
System.err.println(tt.getModifiers());
// 包名
System.err.println(tt.getPackage());
// 类的名称
System.err.println(tt.getName());
// 父类
System.err.println(tt.getSuperclass());
// 类加载器
System.err.println(tt.getClassLoader());
// 简称
System.err.println(tt.getSimpleName());
// 类实现的所有的接口
System.err.println(tt.getInterfaces().length);
// 所有的注解类型
System.err.println(tt.getAnnotations().length);
```
执行结果：
```
1
package com.ibli.javaBase.reflection
com.ibli.javaBase.reflection.Teacher
class java.lang.Object
sun.misc.Launcher$AppClassLoader@18b4aac2
Teacher
0
0
```


#### 查看类的变量
```
// User extend Person(aa,bb)
Class<User> obj = User.class;
User user = obj.newInstance();
// 能够拿到类的所有的变量
Field[] fields = obj.getDeclaredFields();
for (Field field : fields){
    System.out.println(field.getModifiers() + " " + field.getName());
}
System.out.println("    ");

// 只能够拿到类的public的变量
Field[] fields1 = obj.getFields();
for (Field field : fields1){
    System.out.println(field.getModifiers() + " " + field.getName());
}
System.out.println("     ");
```
执行结果：
```
2 age
2 name
1 sex
10 height
    
1 sex
1 aa
1 bb
```
结论：  
- getDeclaredFields  
（1）getDeclaredFields能够获取本类的所有成员变量，无论是public还是private;  
（2）但是不能获取父类的任何属性；  
（3）可以获取static类型的属性；
- getFields  
（1）只能够获取本类的public属性；  
（2）能够获取父类的public属性；  
（3）可以获取static类型的属性；  


#### 修改属性
```
// 设置Person中的变量aa
Field aaField = obj.getField("aa");
aaField.setInt(user,111);
System.err.println(user.getAa());

// 设置User私有成员变量
Field ageField = obj.getDeclaredField("age");
// 设置访问权限
ageField.setAccessible(true);
ageField.set(user,333);
System.err.println(user.getAge());
```
执行结果：
```
111
333
```


#### 查看方法
```
Class<User> obj = User.class;
User user = obj.newInstance();

// 可以获取父类的方法
Method[] methods = obj.getMethods();
for (Method method : methods) {
    System.out.println(method.getModifiers() + "  " + method.getName());
}
System.err.println(" -----  ");

// 获取本类中的所有方法
Method[] methods1 = obj.getDeclaredMethods();
for (Method method : methods1) {
    System.out.println(method.getModifiers() + "  " + method.getName());
}
System.err.println(" 。。。。。。 ");
// 执行结果就不展示了
```
结论：
- getDeclaredMethods  
（1）可以获取本类中的所有方法； 
（2）可以获取本类的静态方法
- getMethods  
（1）可以获取本类中的所有==公有==方法；  
（2）可以获取父类中的所有==公有==方法；  
（3）可以获取本类和父类的公有静态方法；

#### 调用方法
```
// 访问私有方法
Method sleep = obj.getDeclaredMethod("sleep");
sleep.setAccessible(true);
sleep.invoke(user);

// 如果是静态方法，invoke第一个参数传null即可
Method say = obj.getDeclaredMethod("say",String.class);
say.setAccessible(true);
say.invoke(null,"hello java");
```
执行结果：
```
Im sleeping!
say hello java
```


#### 构造器的使用
```
Class<User> obj = User.class;
// 查询共有的构造器
Constructor<?>[] constructors = obj.getConstructors();
for (Constructor<?> constructor : constructors){
    System.out.println(constructor.getModifiers() + "   " + constructor.getName());
}

// 可以获取私有的构造器
Constructor<?>[] constructors1 = obj.getDeclaredConstructors();
for (Constructor<?> constructor : constructors1){
    System.err.println(constructor.getModifiers() + "   " + constructor.getName());
}
```
执行结果：
```
1   com.ibli.javaBase.reflection.User
1   com.ibli.javaBase.reflection.User

1   com.ibli.javaBase.reflection.User
2   com.ibli.javaBase.reflection.User
1   com.ibli.javaBase.reflection.User
```

结论：
- getConstructors  
（1）获得本类所有的公有构造器  
- getDeclaredConstructors  
（1）获得本类所有的构造器（public&private）  

#### 实例化对象
```
// 使用newInstance创建对象 调用无参构造器
User user = obj.newInstance();
// 获取构造器来实例化对象
Constructor<User> constructor = obj.getDeclaredConstructor(Integer.class, String.class);
constructor.setAccessible(true);
User temp = constructor.newInstance(22, "java");
System.err.println(temp.getAge() + " " + temp.getName());
```
执行结果：
> 22 java




### 反射性能为什么差

> 可以从两方面考虑，第一个是反射生成Class对象时性能差，第二是通过反射调用对象方式是的性能差；

（1） 调用forName 本地方法  
（2）每次newInstance 都会进行一次安全检查  
（3）在默认情况下，方法的反射调用为委派实现，委派给本地实现来进行方法调用。在调用超过 15次之后，委派实现便会将委派对象切换至动态实现。这个动态实现的字节码是自动生成的，它将直接使用 invoke 指令来调用目标方法。

方法的反射调用会带来不少性能开销，原因主要有三个：
- 变长参数方法导致的Object数组   
- 基本类型的自动装箱、拆箱  (参考资料2)
- 还有最重要的方法内联。


> 参考资料  
> (1)[反射为什么慢](https://blog.csdn.net/xqlovetyj/article/details/82798864)  
> (2)[关于装箱拆箱为什么会影响效率](https://blog.csdn.net/Admin_Jhon/article/details/52873468?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-4.add_param_isCf&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-4.add_param_isCf)  
>(3)[jvm之方法内联优化](https://zhuanlan.zhihu.com/p/55630861)



### 使用反射注意点

- 在获取Field,method,construtor的时候，应尽量避免是用getDelcaredXXX(),应该传进参数获取指定的字段，方法和构造器；
- 使用缓存机制缓存反射操作相关元数据的原因是因为反射操作相关元数据的实时获取是比较耗时的


 > <p align="middle"> ---------------------   前途浩浩荡荡 万事尽可期待。-----------------------  </p>


