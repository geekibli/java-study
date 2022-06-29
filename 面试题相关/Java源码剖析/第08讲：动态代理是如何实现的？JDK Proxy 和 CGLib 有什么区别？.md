<p>90% 的程序员直接或者间接的使用过动态代理，无论是日志框架或 Spring 框架，它们都包含了动态代理的实现代码。动态代理是程序在运行期间动态构建代理对象和动态调用代理方法的一种机制。</p>
<p>我们本课时的面试题是，如何实现动态代理？JDK Proxy 和 CGLib 有什么区别？</p>
<h3>典型回答</h3>
<p>动态代理的常用实现方式是反射。<strong>反射机制</strong>是指程序在运行期间可以访问、检测和修改其本身状态或行为的一种能力，使用反射我们可以调用任意一个类对象，以及类对象中包含的属性及方法。</p>
<p>但动态代理不止有反射一种实现方式，例如，动态代理可以通过 CGLib 来实现，而 CGLib 是基于 ASM（一个 Java 字节码操作框架）而非反射实现的。简单来说，动态代理是一种行为方式，而反射或 ASM 只是它的一种实现手段而已。</p>
<p>JDK Proxy 和 CGLib 的区别主要体现在以下几个方面：</p>
<ul>
<li>JDK Proxy 是 Java 语言自带的功能，无需通过加载第三方类实现；</li>
<li>Java 对 JDK Proxy 提供了稳定的支持，并且会持续的升级和更新 JDK Proxy，例如 Java 8 版本中的 JDK Proxy 性能相比于之前版本提升了很多；</li>
<li>JDK Proxy 是通过拦截器加反射的方式实现的；</li>
<li>JDK Proxy 只能代理继承接口的类；</li>
<li>JDK Proxy 实现和调用起来比较简单；</li>
<li>CGLib 是第三方提供的工具，基于 ASM 实现的，性能比较高；</li>
<li>CGLib 无需通过接口来实现，它是通过实现子类的方式来完成调用的。</li>
</ul>
<h3>考点分析</h3>
<p>本课时考察的是你对反射、动态代理及 CGLib 的了解，很多人经常会把反射和动态代理划为等号，但从严格意义上来说，这种想法是不正确的，真正能搞懂它们之间的关系，也体现了你扎实 Java 的基本功。和这个问题相关的知识点，还有以下几个：</p>
<ul>
<li>你对 JDK Proxy 和 CGLib 的掌握程度。</li>
<li>Lombok 是通过反射实现的吗？</li>
<li>动态代理和静态代理有什么区别？</li>
<li>动态代理的使用场景有哪些？</li>
<li>Spring 中的动态代理是通过什么方式实现的？</li>
</ul>
<h3>知识扩展</h3>
<h4>1.JDK Proxy 和 CGLib 的使用及代码分析</h4>
<p><strong>JDK Proxy 动态代理实现</strong></p>
<p>JDK Proxy 动态代理的实现无需引用第三方类，只需要实现 InvocationHandler 接口，重写 invoke() 方法即可，整个实现代码如下所示：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">import</span>&nbsp;java.lang.reflect.InvocationHandler;
<span class="hljs-keyword">import</span>&nbsp;java.lang.reflect.InvocationTargetException;
<span class="hljs-keyword">import</span>&nbsp;java.lang.reflect.Method;
<span class="hljs-keyword">import</span>&nbsp;java.lang.reflect.Proxy;

<span class="hljs-comment">/**
&nbsp;*&nbsp;JDK&nbsp;Proxy&nbsp;相关示例
&nbsp;*/</span>
<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">ProxyExample</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">interface</span>&nbsp;<span class="hljs-title">Car</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">running</span><span class="hljs-params">()</span></span>;
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Bus</span>&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">Car</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">running</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"The&nbsp;bus&nbsp;is&nbsp;running."</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Taxi</span>&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">Car</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">running</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"The&nbsp;taxi&nbsp;is&nbsp;running."</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">/**
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;JDK&nbsp;Proxy
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">JDKProxy</span>&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">InvocationHandler</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;Object&nbsp;target;&nbsp;<span class="hljs-comment">//&nbsp;代理对象</span>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;获取到代理对象</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Object&nbsp;<span class="hljs-title">getInstance</span><span class="hljs-params">(Object&nbsp;target)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.target&nbsp;=&nbsp;target;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;取得代理对象</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;Proxy.newProxyInstance(target.getClass().getClassLoader(),
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;target.getClass().getInterfaces(),&nbsp;<span class="hljs-keyword">this</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">/**
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;执行代理方法
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@param</span>&nbsp;proxy&nbsp;&nbsp;代理对象
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@param</span>&nbsp;method&nbsp;代理方法
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@param</span>&nbsp;args&nbsp;&nbsp;&nbsp;方法的参数
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@return</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@throws</span>&nbsp;InvocationTargetException
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;<span class="hljs-doctag">@throws</span>&nbsp;IllegalAccessException
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Object&nbsp;<span class="hljs-title">invoke</span><span class="hljs-params">(Object&nbsp;proxy,&nbsp;Method&nbsp;method,&nbsp;Object[]&nbsp;args)</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throws</span>&nbsp;InvocationTargetException,&nbsp;IllegalAccessException&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"动态代理之前的业务处理."</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;result&nbsp;=&nbsp;method.invoke(target,&nbsp;args);&nbsp;<span class="hljs-comment">//&nbsp;执行调用方法（此方法执行前后，可以进行相关业务处理）</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;result;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">main</span><span class="hljs-params">(String[]&nbsp;args)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;执行&nbsp;JDK&nbsp;Proxy</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;JDKProxy&nbsp;jdkProxy&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;JDKProxy();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Car&nbsp;carInstance&nbsp;=&nbsp;(Car)&nbsp;jdkProxy.getInstance(<span class="hljs-keyword">new</span>&nbsp;Taxi());
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;carInstance.running();
</code></pre>
<p>以上程序的执行结果是：</p>
<pre><code data-language="java" class="lang-java">动态代理之前的业务处理.
The&nbsp;taxi&nbsp;is&nbsp;running.
</code></pre>
<p>可以看出 JDK Proxy 实现动态代理的核心是实现 Invocation 接口，我们查看 Invocation 的源码，会发现里面其实只有一个 invoke() 方法，源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">interface</span>&nbsp;<span class="hljs-title">InvocationHandler</span>&nbsp;</span>{
&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Object&nbsp;<span class="hljs-title">invoke</span><span class="hljs-params">(Object&nbsp;proxy,&nbsp;Method&nbsp;method,&nbsp;Object[]&nbsp;args)</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throws</span>&nbsp;Throwable</span>;
}
</code></pre>
<p>这是因为在动态代理中有一个重要的角色也就是代理器，它用于统一管理被代理的对象，显然 InvocationHandler 就是这个代理器，而 invoke() 方法则是触发代理的执行方法，我们通过实现 Invocation 接口来拥有动态代理的能力。</p>
<p><strong>CGLib 的实现</strong></p>
<p>在使用 CGLib 之前，我们要先在项目中引入 CGLib 框架，在 pom.xml 中添加如下配置：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-comment">&lt;!--&nbsp;https://mvnrepository.com/artifact/cglib/cglib&nbsp;--&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>cglib<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>cglib<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">version</span>&gt;</span>3.3.0<span class="hljs-tag">&lt;/<span class="hljs-name">version</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span>
</code></pre>
<p>CGLib 实现代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">package</span>&nbsp;com.lagou.interview;

<span class="hljs-keyword">import</span>&nbsp;net.sf.cglib.proxy.Enhancer;
<span class="hljs-keyword">import</span>&nbsp;net.sf.cglib.proxy.MethodInterceptor;
<span class="hljs-keyword">import</span>&nbsp;net.sf.cglib.proxy.MethodProxy;

<span class="hljs-keyword">import</span>&nbsp;java.lang.reflect.Method;

<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">CGLibExample</span>&nbsp;</span>{

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Car</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">running</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"The&nbsp;car&nbsp;is&nbsp;running."</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">/**
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;CGLib&nbsp;代理类
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*/</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">CGLibProxy</span>&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">MethodInterceptor</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;Object&nbsp;target;&nbsp;<span class="hljs-comment">//&nbsp;代理对象</span>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Object&nbsp;<span class="hljs-title">getInstance</span><span class="hljs-params">(Object&nbsp;target)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.target&nbsp;=&nbsp;target;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Enhancer&nbsp;enhancer&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;Enhancer();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;设置父类为实例类</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;enhancer.setSuperclass(<span class="hljs-keyword">this</span>.target.getClass());
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;回调方法</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;enhancer.setCallback(<span class="hljs-keyword">this</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;创建代理对象</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;enhancer.create();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Object&nbsp;<span class="hljs-title">intercept</span><span class="hljs-params">(Object&nbsp;o,&nbsp;Method&nbsp;method,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object[]&nbsp;objects,&nbsp;MethodProxy&nbsp;methodProxy)</span>&nbsp;<span class="hljs-keyword">throws</span>&nbsp;Throwable&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"方法调用前业务处理."</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;result&nbsp;=&nbsp;methodProxy.invokeSuper(o,&nbsp;objects);&nbsp;<span class="hljs-comment">//&nbsp;执行方法调用</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;result;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;执行&nbsp;CGLib&nbsp;的方法调用</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">main</span><span class="hljs-params">(String[]&nbsp;args)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;创建&nbsp;CGLib&nbsp;代理类</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CGLibProxy&nbsp;proxy&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;CGLibProxy();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;初始化代理对象</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Car&nbsp;car&nbsp;=&nbsp;(Car)&nbsp;proxy.getInstance(<span class="hljs-keyword">new</span>&nbsp;Car());
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;执行方法</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;car.running();
</code></pre>
<p>以上程序的执行结果是：</p>
<pre><code data-language="java" class="lang-java">方法调用前业务处理.
The&nbsp;car&nbsp;is&nbsp;running.
</code></pre>
<p>可以看出 CGLib 和 JDK Proxy 的实现代码比较类似，都是通过实现代理器的接口，再调用某一个方法完成动态代理的，唯一不同的是，CGLib 在初始化被代理类时，是通过 Enhancer 对象把代理对象设置为被代理类的子类来实现动态代理的。因此被代理类不能被关键字 final 修饰，如果被 final 修饰，再使用 Enhancer 设置父类时会报错，动态代理的构建会失败。</p>
<h4>2.Lombok 原理分析</h4>
<p>在开始讲 Lombok 的原理之前，我们先来简单地介绍一下 Lombok，它属于 Java 的一个热门工具类，使用它可以有效的解决代码工程中那些繁琐又重复的代码，如 Setter、Getter、toString、equals 和 hashCode 等等，向这种方法都可以使用 Lombok 注解来完成。</p>
<p>例如，我们使用比较多的 Setter 和 Getter 方法，在没有使用 Lombok 之前，代码是这样的：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Person</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;Integer&nbsp;id;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;String&nbsp;name;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Integer&nbsp;<span class="hljs-title">getId</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;id;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">setId</span><span class="hljs-params">(Integer&nbsp;id)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.id&nbsp;=&nbsp;id;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;String&nbsp;<span class="hljs-title">getName</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;name;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">setName</span><span class="hljs-params">(String&nbsp;name)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.name&nbsp;=&nbsp;name;
&nbsp;&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p>在使用 Lombok 之后，代码是这样的：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Data</span>
<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Person</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;Integer&nbsp;id;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;String&nbsp;name;
}
</code></pre>
<p>可以看出 Lombok 让代码简单和优雅了很多。</p>
<blockquote>
<p>小贴士：如果在项目中使用了 Lombok 的 Getter 和 Setter 注解，那么想要在编码阶段成功调用对象的 set 或 get 方法，我们需要在 IDE 中安装 Lombok 插件才行，比如 Idea 的插件如下图所示：</p>
</blockquote>
<p><img src="https://s0.lgstatic.com/i/image3/M01/09/05/Ciqah16HCB6AcjsPAAIhVz8yo1o620.png" alt=""></p>
<p>接下来讲讲 Lombok 的原理。</p>
<p>Lombok 的实现和反射没有任何关系，前面我们说了反射是程序在运行期的一种自省（introspect）能力，而 Lombok 的实现是在编译期就完成了，为什么这么说呢？</p>
<p>回到我们刚才 Setter/Getter 的方法，当我们打开 Person 的编译类就会发现，使用了 Lombok 的 @Data 注解后的源码竟然是这样的：</p>
<p><img src="https://s0.lgstatic.com/i/image3/M01/82/1B/Cgq2xl6HCB6AXaC5AAHG477g0yQ093.png" alt=""></p>
<p>可以看出 Lombok 是在编译期就为我们生成了对应的字节码。</p>
<p>其实 Lombok 是基于 Java 1.6 实现的 JSR 269: Pluggable Annotation Processing API 来实现的，也就是通过编译期自定义注解处理器来实现的，它的执行步骤如下：</p>
<p><img src="https://s0.lgstatic.com/i/image3/M01/82/1B/Cgq2xl6HCB-AAsAKAACfTHorgDA111.png" alt=""></p>
<p>从流程图中可以看出，在编译期阶段，当 Java 源码被抽象成语法树（AST）之后，Lombok 会根据自己的注解处理器动态修改 AST，增加新的代码（节点），在这一切执行之后就生成了最终的字节码（.class）文件，这就是 Lombok 的执行原理。</p>
<h4>3.动态代理知识点扩充</h4>
<p>当面试官问动态代理的时候，经常会问到它和静态代理的区别？静态代理其实就是事先写好代理类，可以手工编写也可以使用工具生成，但它的缺点是每个业务类都要对应一个代理类，特别不灵活也不方便，于是就有了动态代理。</p>
<p>动态代理的常见使用场景有 RPC 框架的封装、AOP（面向切面编程）的实现、JDBC 的连接等。</p>
<p>Spring 框架中同时使用了两种动态代理 JDK Proxy 和 CGLib，当 Bean 实现了接口时，Spring 就会使用 JDK Proxy，在没有实现接口时就会使用 CGLib，我们也可以在配置中指定强制使用 CGLib，只需要在 Spring 配置中添加 &lt;aop:aspectj-autoproxy proxy-target-class="true"/&gt; 即可。</p>
<h3>小结</h3>
<p>本课时我们介绍了 JDK Proxy 和 CGLib 的区别，JDK Proxy 是 Java 语言内置的动态代理，必须要通过实现接口的方式来代理相关的类，而 CGLib 是第三方提供的基于 ASM 的高效动态代理类，它通过实现被代理类的子类来实现动态代理的功能，因此被代理的类不能使用 final 修饰。</p>
<p>除了 JDK Proxy 和 CGLib 之外，我们还讲了 Java 中常用的工具类 Lombok 的实现原理，它其实和反射是没有任何关系的；最后讲了动态代理的使用场景以及 Spring 中动态代理的实现方式，希望本文可以帮助到你。</p>

---

### 精选评论

##### *巍：
> “JDK Proxy 是 Java 语言内置的动态代理，必须要通过实现接口的方式来代理相关的类”为什么必须要实现接口呢？如果不实现接口，还有其他方式么？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 必须要这样实现，这相当于给 JVM 一个它能听懂的内置的指令，就好像用北京话作为普通话一样，它需要有一个“规范”来统一大家的行为，让人们能听懂彼此在说什么。而这些特殊的指令是 Java 内置的不可更改的。

##### **将：
> 不是很明白文中提供的使用JDKProxy和CGLib的方式创建对象及方法调用的意义是什么？为啥不直接创建相关的对象，比如CGLib的案例中，直接创建Car对象，感觉更加直接

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 为了能统一的做一些处理，例如记录日志，或者授权。可以看一下即将更新的 Spring 下的文章里面，有关于 AOP 的介绍。

##### *韵：
> 老师，target应该是被代理对象吧，注解里写的是代理对象，容易误解。getInstance返回的才是代理对象吧。

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 感谢建议。重点是后半句“通过 getInstance 方法获取到代理对象的”，其实看懂这个就可以啦，哈哈。

##### *俊：
> 睡前再刷一波

##### *健：
> 老师，您文中图是什么工具画的呀

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是用 draw.io 画的

##### **戈多：
> 老师讲得不错，认真学习中

