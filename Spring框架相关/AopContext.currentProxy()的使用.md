# AopContext.currentProxy()的使用


常规使用spring的AOP功能，就是对一个service的B方法进行切入记录日志，AOP能起作用。但是假如B方法被service中的A方法调用，切入会失效，无法记录日志(这里的切入失效是指对B的切入记录日志失效，对A方法的切入还是有用的)。



```java
Service类
public void A() {
	B();
}
	
public void B() {
		
}
 
假如service目标对象为s
 
Proxy类
public void A() {
    //对A方法的增强
    s.A(
	    s.B()//对B方法的增强记录日志无法切入 因为方法的调用对象是目标对象不是代理对象
    )
}
	
public void B() {
    //对B方法的增强记录日志
	s.B()
}

```


为什么能够切入目标对象，原理就是创建了代理类，在代理类中调用目标方法前后进行切入；假如代理对象是$proxy0,对于B方法$proxy0.B()，执行流程就是先记录日志再调用目标对象s的B方法，所以可以切入；但是A方法$proxy0.A()，只能对A方法增强，A里面调B的时候使用的是s目标对象s.B()，而不是$proxy0.B()，所以对B的切入无效，因为压根就没用代理对象去调用；

解决方案就是把service的A方法中对B的调用改成代理对象的调用，怎么获取代理对象呢，AopContext.currentProxy()使用ThreadLocal保存了代理对象，因此((Service) AopContext.currentProxy()).B()就能解决。
