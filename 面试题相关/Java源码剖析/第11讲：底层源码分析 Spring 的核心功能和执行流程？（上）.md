<p>Spring Framework 已是公认的 Java 标配开发框架了，甚至还有人说 Java 编程就是面向 Spring 编程的，可见 Spring 在整个 Java 体系中的重要位置。</p>
<p>Spring 中包含了众多的功能和相关模块，比如 spring-core、spring-beans、spring-aop、spring-context、spring-expression、spring-test 等，本课时先从面试中必问的问题出发，来帮你更好的 Spring 框架。</p>
<p>我们本课时的面试题是，Spring Bean 的作用域有哪些？它的注册方式有几种？</p>
<h3>典型回答</h3>
<p>在 Spring 容器中管理一个或多个 Bean，这些 Bean 的定义表示为 BeanDefinition 对象，这些对象包含以下重要信息：</p>
<ul>
<li>Bean 的实际实现类</li>
<li>Bean 的作用范围</li>
<li>Bean 的引用或者依赖项</li>
</ul>
<p>Bean 的注册方式有三种：</p>
<ul>
<li>XML 配置文件的注册方式</li>
<li>Java 注解的注册方式</li>
<li>Java API 的注册方式</li>
</ul>
<h4>1. XML 配置文件注册方式</h4>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">id</span>=<span class="hljs-string">"person"</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"org.springframework.beans.Person"</span>&gt;</span>
&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">property</span>&nbsp;<span class="hljs-attr">name</span>=<span class="hljs-string">"id"</span>&nbsp;<span class="hljs-attr">value</span>=<span class="hljs-string">"1"</span>/&gt;</span>
&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">property</span>&nbsp;<span class="hljs-attr">name</span>=<span class="hljs-string">"name"</span>&nbsp;<span class="hljs-attr">value</span>=<span class="hljs-string">"Java"</span>/&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<h4>2. Java 注解注册方式</h4>
<p>可以使用 @Component 注解方式来注册 Bean，代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Component</span>
<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Person</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;Integer&nbsp;id;
&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;String&nbsp;name
&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;忽略其他方法</span>
}
</code></pre>
<p>也可以使用 @Bean 注解方式来注册 Bean，代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Configuration</span>
<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Person</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Bean</span>
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Person&nbsp;&nbsp;<span class="hljs-title">person</span><span class="hljs-params">()</span></span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;Person();
&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;忽略其他方法</span>
}
</code></pre>
<p>其中 @Configuration 可理解为 XML 配置里的 &lt;beans&gt; 标签，而 @Bean 可理解为用 XML 配置里面的 &lt;bean&gt; 标签。</p>
<h4>3. Java API 注册方式</h4>
<p>使用 BeanDefinitionRegistry.registerBeanDefinition() 方法的方式注册 Bean，代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">CustomBeanDefinitionRegistry</span>&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">BeanDefinitionRegistryPostProcessor</span>&nbsp;</span>{
	<span class="hljs-meta">@Override</span>
	<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">postProcessBeanFactory</span><span class="hljs-params">(ConfigurableListableBeanFactory&nbsp;beanFactory)</span>&nbsp;<span class="hljs-keyword">throws</span>&nbsp;BeansException&nbsp;</span>{
	}
	<span class="hljs-meta">@Override</span>
	<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">postProcessBeanDefinitionRegistry</span><span class="hljs-params">(BeanDefinitionRegistry&nbsp;registry)</span>&nbsp;<span class="hljs-keyword">throws</span>&nbsp;BeansException&nbsp;</span>{
		RootBeanDefinition&nbsp;personBean&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;RootBeanDefinition(Person<span class="hljs-class">.<span class="hljs-keyword">class</span>)</span>;
		<span class="hljs-comment">//&nbsp;新增&nbsp;Bean</span>
		registry.registerBeanDefinition(<span class="hljs-string">"person"</span>,&nbsp;personBean);
	}
}
</code></pre>
<p>Bean 的作用域一共有 5 个。</p>
<p>（1）<strong>singleton 作用域</strong>：表示在 Spring 容器中只有一个 Bean 实例，以单例的形式存在，是默认的 Bean 作用域。</p>
<p>配置方式，缺省即可，XML 的配置方式如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"..."</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<p>（2）<strong>prototype 作用域</strong>：原型作用域，每次调用 Bean 时都会创建一个新实例，也就是说每次调用 getBean() 方法时，相当于执行了 new Bean()。</p>
<p>XML 的配置方式如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"..."</span>&nbsp;<span class="hljs-attr">scope</span>=<span class="hljs-string">"prototype"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<p>（3）<strong>request 作用域</strong>：每次 Http 请求时都会创建一个新的 Bean，该作用域仅适应于 WebApplicationContext 环境。</p>
<p>XML 的配置方式如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"..."</span>&nbsp;<span class="hljs-attr">scope</span>=<span class="hljs-string">"request"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<p>Java 注解的配置方式如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Scope</span>(WebApplicationContext.SCOPE_REQUEST)
</code></pre>
<p>或是：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@RequestScope</span>(WebApplicationContext.SCOPE_REQUEST)
</code></pre>
<p>（4）<strong>session 作用域</strong>：同一个 Http Session 共享一个 Bean 对象，不同的 Session 拥有不同的 Bean 对象，仅适用于 WebApplicationContext 环境。</p>
<p>XML 的配置方式如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"..."</span>&nbsp;<span class="hljs-attr">scope</span>=<span class="hljs-string">"session"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<p>Java 注解的配置方式如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Scope</span>(WebApplicationContext.SCOPE_SESSION)
</code></pre>
<p>或是：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@RequestScope</span>(WebApplicationContext.SCOPE_SESSION)
</code></pre>
<p>（5）<strong>application 作用域</strong>：全局的 Web 作用域，类似于 Servlet 中的 Application。</p>
<p>XML 的配置方式如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"..."</span>&nbsp;<span class="hljs-attr">scope</span>=<span class="hljs-string">"application"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<p>Java 注解的配置方式如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Scope</span>(WebApplicationContext.SCOPE_APPLICATION)
</code></pre>
<p>或是：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@RequestScope</span>(WebApplicationContext.SCOPE_APPLICATION)
</code></pre>
<h3>考点分析</h3>
<p>在 Spring 中最核心的概念是 AOP（面向切面编程）、IoC（控制反转）、DI（依赖注入）等（此内容将会在下一课时中讲到），而最实用的功能则是 Bean，他们是概念和具体实现的关系。和 Bean 相关的面试题，还有以下几个：</p>
<ul>
<li>什么是同名 Bean？它是如何产生的？应该如何避免？</li>
<li>聊一聊 Bean 的生命周期。</li>
</ul>
<h3>知识扩展</h3>
<h4>1.同名 Bean 问题</h4>
<p>每个 Bean 拥有一个或多个标识符，在基于 XML 的配置中，我们可以使用 id 或者 name 来作为 Bean 的标识符。通常 Bean 的标识符由字母组成，允许使用特殊字符。</p>
<p>同一个 Spring 配置文件中 Bean 的 id 和 name 是不能够重复的，否则 Spring 容器启动时会报错。但如果 Spring 加载了多个配置文件的话，可能会出现同名 Bean 的问题。<strong>同名 Bean</strong> 指的是多个 Bean 有相同的 name 或者 id。</p>
<p>Spring 对待同名 Bean 的处理规则是使用最后面的 Bean 覆盖前面的 Bean，所以我们在定义 Bean 时，尽量使用长命名非重复的方式来定义，避免产生同名 Bean 的问题。</p>
<p>Bean 的 id 或 name 属性并非必须指定，如果留空的话，容器会为 Bean 自动生成一个唯一的</p>
<p>名称，这样也不会出现同名 Bean 的问题。</p>
<h4>2.Bean 生命周期</h4>
<p>对于 Spring Bean 来说，并不是启动阶段就会触发 Bean 的实例化，只有当客户端通过显式或者隐式的方式调用 BeanFactory 的 getBean() 方法时，它才会触发该类的实例化方法。当然对于 BeanFactory 来说，也不是所有的 getBean() 方法都会实例化 Bean 对象，例如作用域为 singleton 时，只会在第一次，实例化该 Bean 对象，之后会直接返回该对象。但如果使用的是 ApplicationContext 容器，则会在该容器启动的时候，立即调用注册到该容器所有 Bean 的实例化方法。</p>
<p>getBean() 既然是 Bean 对象的入口，我们就先从这个方法说起，getBean() 方法是属于 BeanFactory 接口的，它的真正实现是 AbstractAutowireCapableBeanFactory 的 createBean() 方法，而 createBean() 是通过 doCreateBean() 来实现的，具体源码实现如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Override</span>
<span class="hljs-function"><span class="hljs-keyword">protected</span>&nbsp;Object&nbsp;<span class="hljs-title">createBean</span><span class="hljs-params">(String&nbsp;beanName,&nbsp;RootBeanDefinition&nbsp;mbd,&nbsp;@Nullable&nbsp;Object[]&nbsp;args)</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throws</span>&nbsp;BeanCreationException&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(logger.isTraceEnabled())&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.trace(<span class="hljs-string">"Creating&nbsp;instance&nbsp;of&nbsp;bean&nbsp;'"</span>&nbsp;+&nbsp;beanName&nbsp;+&nbsp;<span class="hljs-string">"'"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;RootBeanDefinition&nbsp;mbdToUse&nbsp;=&nbsp;mbd;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;确定并加载&nbsp;Bean&nbsp;的&nbsp;class</span>
&nbsp;&nbsp;&nbsp;&nbsp;Class&lt;?&gt;&nbsp;resolvedClass&nbsp;=&nbsp;resolveBeanClass(mbd,&nbsp;beanName);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(resolvedClass&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>&nbsp;&amp;&amp;&nbsp;!mbd.hasBeanClass()&nbsp;&amp;&amp;&nbsp;mbd.getBeanClassName()&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mbdToUse&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;RootBeanDefinition(mbd);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mbdToUse.setBeanClass(resolvedClass);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;验证以及准备需要覆盖的方法</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">try</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mbdToUse.prepareMethodOverrides();
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">catch</span>&nbsp;(BeanDefinitionValidationException&nbsp;ex)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;beanName,&nbsp;<span class="hljs-string">"Validation&nbsp;of&nbsp;method&nbsp;overrides&nbsp;failed"</span>,&nbsp;ex);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">try</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;给BeanPostProcessors&nbsp;一个机会来返回代理对象来代替真正的&nbsp;Bean&nbsp;实例，在这里实现创建代理对象功能</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;bean&nbsp;=&nbsp;resolveBeforeInstantiation(beanName,&nbsp;mbdToUse);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(bean&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;bean;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">catch</span>&nbsp;(Throwable&nbsp;ex)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;BeanCreationException(mbdToUse.getResourceDescription(),&nbsp;beanName,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"BeanPostProcessor&nbsp;before&nbsp;instantiation&nbsp;of&nbsp;bean&nbsp;failed"</span>,&nbsp;ex);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">try</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;创建&nbsp;Bean</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;beanInstance&nbsp;=&nbsp;doCreateBean(beanName,&nbsp;mbdToUse,&nbsp;args);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(logger.isTraceEnabled())&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.trace(<span class="hljs-string">"Finished&nbsp;creating&nbsp;instance&nbsp;of&nbsp;bean&nbsp;'"</span>&nbsp;+&nbsp;beanName&nbsp;+&nbsp;<span class="hljs-string">"'"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;beanInstance;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">catch</span>&nbsp;(BeanCreationException&nbsp;|&nbsp;ImplicitlyAppearedSingletonException&nbsp;ex)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;ex;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">catch</span>&nbsp;(Throwable&nbsp;ex)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;BeanCreationException(
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mbdToUse.getResourceDescription(),&nbsp;beanName,&nbsp;<span class="hljs-string">"Unexpected&nbsp;exception&nbsp;during&nbsp;bean&nbsp;creation"</span>,&nbsp;ex);
&nbsp;&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p>doCreateBean 源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">protected</span>&nbsp;Object&nbsp;<span class="hljs-title">doCreateBean</span><span class="hljs-params">(<span class="hljs-keyword">final</span>&nbsp;String&nbsp;beanName,&nbsp;<span class="hljs-keyword">final</span>&nbsp;RootBeanDefinition&nbsp;mbd,&nbsp;<span class="hljs-keyword">final</span>&nbsp;@Nullable&nbsp;Object[]&nbsp;args)</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throws</span>&nbsp;BeanCreationException&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;实例化&nbsp;bean，BeanWrapper&nbsp;对象提供了设置和获取属性值的功能</span>
&nbsp;&nbsp;&nbsp;&nbsp;BeanWrapper&nbsp;instanceWrapper&nbsp;=&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果&nbsp;RootBeanDefinition&nbsp;是单例，则移除未完成的&nbsp;FactoryBean&nbsp;实例的缓存</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(mbd.isSingleton())&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;instanceWrapper&nbsp;=&nbsp;<span class="hljs-keyword">this</span>.factoryBeanInstanceCache.remove(beanName);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(instanceWrapper&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;创建&nbsp;bean&nbsp;实例</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;instanceWrapper&nbsp;=&nbsp;createBeanInstance(beanName,&nbsp;mbd,&nbsp;args);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;获取&nbsp;BeanWrapper&nbsp;中封装的&nbsp;Object&nbsp;对象，其实就是&nbsp;bean&nbsp;对象的实例</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">final</span>&nbsp;Object&nbsp;bean&nbsp;=&nbsp;instanceWrapper.getWrappedInstance();
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;获取&nbsp;BeanWrapper&nbsp;中封装&nbsp;bean&nbsp;的&nbsp;Class</span>
&nbsp;&nbsp;&nbsp;&nbsp;Class&lt;?&gt;&nbsp;beanType&nbsp;=&nbsp;instanceWrapper.getWrappedClass();
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(beanType&nbsp;!=&nbsp;NullBean<span class="hljs-class">.<span class="hljs-keyword">class</span>)&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mbd.resolvedTargetType&nbsp;=&nbsp;beanType;
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;应用&nbsp;MergedBeanDefinitionPostProcessor&nbsp;后处理器，合并&nbsp;bean&nbsp;的定义信息</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;Autowire&nbsp;等注解信息就是在这一步完成预解析，并且将注解需要的信息放入缓存</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">synchronized</span>&nbsp;(mbd.postProcessingLock)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(!mbd.postProcessed)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">try</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;applyMergedBeanDefinitionPostProcessors(mbd,&nbsp;beanType,&nbsp;beanName);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">catch</span>&nbsp;(Throwable&nbsp;ex)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;BeanCreationException(mbd.getResourceDescription(),&nbsp;beanName,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"Post-processing&nbsp;of&nbsp;merged&nbsp;bean&nbsp;definition&nbsp;failed"</span>,&nbsp;ex);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mbd.postProcessed&nbsp;=&nbsp;<span class="hljs-keyword">true</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;earlySingletonExposure&nbsp;=&nbsp;(mbd.isSingleton()&nbsp;&amp;&amp;&nbsp;<span class="hljs-keyword">this</span>.allowCircularReferences&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;isSingletonCurrentlyInCreation(beanName));
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(earlySingletonExposure)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(logger.isTraceEnabled())&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.trace(<span class="hljs-string">"Eagerly&nbsp;caching&nbsp;bean&nbsp;'"</span>&nbsp;+&nbsp;beanName&nbsp;+
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"'&nbsp;to&nbsp;allow&nbsp;for&nbsp;resolving&nbsp;potential&nbsp;circular&nbsp;references"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;为了避免循环依赖，在&nbsp;bean&nbsp;初始化完成前，就将创建&nbsp;bean&nbsp;实例的&nbsp;ObjectFactory&nbsp;放入工厂缓存（singletonFactories）</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;addSingletonFactory(beanName,&nbsp;()&nbsp;-&gt;&nbsp;getEarlyBeanReference(beanName,&nbsp;mbd,&nbsp;bean));
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;对&nbsp;bean&nbsp;属性进行填充</span>
&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;exposedObject&nbsp;=&nbsp;bean;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">try</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;populateBean(beanName,&nbsp;mbd,&nbsp;instanceWrapper);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;调用初始化方法，如&nbsp;init-method&nbsp;注入&nbsp;Aware&nbsp;对象</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;exposedObject&nbsp;=&nbsp;initializeBean(beanName,&nbsp;exposedObject,&nbsp;mbd);
&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">catch</span>&nbsp;(Throwable&nbsp;ex)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(ex&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;BeanCreationException&nbsp;&amp;&amp;&nbsp;beanName.equals(((BeanCreationException)&nbsp;ex).getBeanName()))&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;(BeanCreationException)&nbsp;ex;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">else</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;BeanCreationException(
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mbd.getResourceDescription(),&nbsp;beanName,&nbsp;<span class="hljs-string">"Initialization&nbsp;of&nbsp;bean&nbsp;failed"</span>,&nbsp;ex);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(earlySingletonExposure)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果存在循环依赖，也就是说该&nbsp;bean&nbsp;已经被其他&nbsp;bean&nbsp;递归加载过，放入了提早公布的&nbsp;bean&nbsp;缓存中</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Object&nbsp;earlySingletonReference&nbsp;=&nbsp;getSingleton(beanName,&nbsp;<span class="hljs-keyword">false</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(earlySingletonReference&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果&nbsp;exposedObject&nbsp;没有在&nbsp;initializeBean&nbsp;初始化方法中被增强</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(exposedObject&nbsp;==&nbsp;bean)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;exposedObject&nbsp;=&nbsp;earlySingletonReference;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">else</span>&nbsp;<span class="hljs-keyword">if</span>&nbsp;(!<span class="hljs-keyword">this</span>.allowRawInjectionDespiteWrapping&nbsp;&amp;&amp;&nbsp;hasDependentBean(beanName))&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;依赖检测</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String[]&nbsp;dependentBeans&nbsp;=&nbsp;getDependentBeans(beanName);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Set&lt;String&gt;&nbsp;actualDependentBeans&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;LinkedHashSet&lt;&gt;(dependentBeans.length);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">for</span>&nbsp;(String&nbsp;dependentBean&nbsp;:&nbsp;dependentBeans)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(!removeSingletonIfCreatedForTypeCheckOnly(dependentBean))&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;actualDependentBeans.add(dependentBean);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;如果&nbsp;actualDependentBeans&nbsp;不为空，则表示依赖的&nbsp;bean&nbsp;并没有被创建完，即存在循环依赖</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(!actualDependentBeans.isEmpty())&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;BeanCurrentlyInCreationException(beanName,
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"Bean&nbsp;with&nbsp;name&nbsp;'"</span>&nbsp;+&nbsp;beanName&nbsp;+&nbsp;<span class="hljs-string">"'&nbsp;has&nbsp;been&nbsp;injected&nbsp;into&nbsp;other&nbsp;beans&nbsp;["</span>&nbsp;+
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;StringUtils.collectionToCommaDelimitedString(actualDependentBeans)&nbsp;+
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"]&nbsp;in&nbsp;its&nbsp;raw&nbsp;version&nbsp;as&nbsp;part&nbsp;of&nbsp;a&nbsp;circular&nbsp;reference,&nbsp;but&nbsp;has&nbsp;eventually&nbsp;been&nbsp;"</span>&nbsp;+
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"wrapped.&nbsp;This&nbsp;means&nbsp;that&nbsp;said&nbsp;other&nbsp;beans&nbsp;do&nbsp;not&nbsp;use&nbsp;the&nbsp;final&nbsp;version&nbsp;of&nbsp;the&nbsp;"</span>&nbsp;+
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"bean.&nbsp;This&nbsp;is&nbsp;often&nbsp;the&nbsp;result&nbsp;of&nbsp;over-eager&nbsp;type&nbsp;matching&nbsp;-&nbsp;consider&nbsp;using&nbsp;"</span>&nbsp;+
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-string">"'getBeanNamesOfType'&nbsp;with&nbsp;the&nbsp;'allowEagerInit'&nbsp;flag&nbsp;turned&nbsp;off,&nbsp;for&nbsp;example."</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">try</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;注册&nbsp;DisposableBean&nbsp;以便在销毁时调用</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;registerDisposableBeanIfNecessary(beanName,&nbsp;bean,&nbsp;mbd);
&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">catch</span>&nbsp;(BeanDefinitionValidationException&nbsp;ex)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;BeanCreationException(
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mbd.getResourceDescription(),&nbsp;beanName,&nbsp;<span class="hljs-string">"Invalid&nbsp;destruction&nbsp;signature"</span>,&nbsp;ex);
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;exposedObject;
}
</code></pre>
<p>从上述源码中可以看出，在 doCreateBean() 方法中，首先对 Bean 进行了实例化工作，它是通过调用 createBeanInstance() 方法来实现的，该方法返回一个 BeanWrapper 对象。BeanWrapper 对象是 Spring 中一个基础的 Bean 结构接口，说它是基础接口是因为它连基本的属性都没有。</p>
<p>BeanWrapper 接口有一个默认实现类 BeanWrapperImpl，其主要作用是对 Bean 进行填充，比如填充和注入 Bean 的属性等。</p>
<p>当 Spring 完成 Bean 对象实例化并且设置完相关属性和依赖后，则会调用 Bean 的初始化方法 initializeBean()，初始化第一个阶段是检查当前 Bean 对象是否实现了 BeanNameAware、BeanClassLoaderAware、BeanFactoryAware 等接口，源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">invokeAwareMethods</span><span class="hljs-params">(<span class="hljs-keyword">final</span>&nbsp;String&nbsp;beanName,&nbsp;<span class="hljs-keyword">final</span>&nbsp;Object&nbsp;bean)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(bean&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;Aware)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(bean&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;BeanNameAware)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((BeanNameAware)&nbsp;bean).setBeanName(beanName);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(bean&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;BeanClassLoaderAware)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ClassLoader&nbsp;bcl&nbsp;=&nbsp;getBeanClassLoader();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(bcl&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((BeanClassLoaderAware)&nbsp;bean).setBeanClassLoader(bcl);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(bean&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;BeanFactoryAware)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((BeanFactoryAware)&nbsp;bean).setBeanFactory(AbstractAutowireCapableBeanFactory.<span class="hljs-keyword">this</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p>其中，BeanNameAware 是把 Bean 对象定义的 beanName 设置到当前对象实例中；BeanClassLoaderAware 是将当前 Bean 对象相应的 ClassLoader 注入到当前对象实例中；BeanFactoryAware 是 BeanFactory 容器会将自身注入到当前对象实例中，这样当前对象就会拥有一个 BeanFactory 容器的引用。</p>
<p>初始化第二个阶段则是 BeanPostProcessor 增强处理，它主要是对 Spring 容器提供的 Bean 实例对象进行有效的扩展，允许 Spring 在初始化 Bean 阶段对其进行定制化修改，比如处理标记接口或者为其提供代理实现。</p>
<p>在初始化的前置处理完成之后就会检查和执行 InitializingBean 和 init-method 方法。</p>
<p>InitializingBean 是一个接口，它有一个 afterPropertiesSet() 方法，在 Bean 初始化时会判断当前 Bean 是否实现了 InitializingBean，如果实现了则调用 afterPropertiesSet() 方法，进行初始化工作；然后再检查是否也指定了 init-method，如果指定了则通过反射机制调用指定的 init-method 方法，它的实现源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">protected</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">invokeInitMethods</span><span class="hljs-params">(String&nbsp;beanName,&nbsp;<span class="hljs-keyword">final</span>&nbsp;Object&nbsp;bean,&nbsp;@Nullable&nbsp;RootBeanDefinition&nbsp;mbd)</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throws</span>&nbsp;Throwable&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;判断当前&nbsp;Bean&nbsp;是否实现了&nbsp;InitializingBean，如果是的话需要调用&nbsp;afterPropertiesSet()</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">boolean</span>&nbsp;isInitializingBean&nbsp;=&nbsp;(bean&nbsp;<span class="hljs-keyword">instanceof</span>&nbsp;InitializingBean);
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(isInitializingBean&nbsp;&amp;&amp;&nbsp;(mbd&nbsp;==&nbsp;<span class="hljs-keyword">null</span>&nbsp;||&nbsp;!mbd.isExternallyManagedInitMethod(<span class="hljs-string">"afterPropertiesSet"</span>)))&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(logger.isTraceEnabled())&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;logger.trace(<span class="hljs-string">"Invoking&nbsp;afterPropertiesSet()&nbsp;on&nbsp;bean&nbsp;with&nbsp;name&nbsp;'"</span>&nbsp;+&nbsp;beanName&nbsp;+&nbsp;<span class="hljs-string">"'"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(System.getSecurityManager()&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{&nbsp;<span class="hljs-comment">//&nbsp;安全模式</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">try</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;AccessController.doPrivileged((PrivilegedExceptionAction&lt;Object&gt;)&nbsp;()&nbsp;-&gt;&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((InitializingBean)&nbsp;bean).afterPropertiesSet();&nbsp;<span class="hljs-comment">//&nbsp;属性初始化</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">null</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;},&nbsp;getAccessControlContext());
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">catch</span>&nbsp;(PrivilegedActionException&nbsp;pae)&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">throw</span>&nbsp;pae.getException();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}&nbsp;<span class="hljs-keyword">else</span>&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;((InitializingBean)&nbsp;bean).afterPropertiesSet();&nbsp;<span class="hljs-comment">//&nbsp;属性初始化</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;判断是否指定了&nbsp;init-method()</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(mbd&nbsp;!=&nbsp;<span class="hljs-keyword">null</span>&nbsp;&amp;&amp;&nbsp;bean.getClass()&nbsp;!=&nbsp;NullBean<span class="hljs-class">.<span class="hljs-keyword">class</span>)&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;String&nbsp;initMethodName&nbsp;=&nbsp;mbd.getInitMethodName();
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">if</span>&nbsp;(StringUtils.hasLength(initMethodName)&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;!(isInitializingBean&nbsp;&amp;&amp;&nbsp;<span class="hljs-string">"afterPropertiesSet"</span>.equals(initMethodName))&nbsp;&amp;&amp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;!mbd.isExternallyManagedInitMethod(initMethodName))&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;利用反射机制执行指定方法</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;invokeCustomInitMethod(beanName,&nbsp;bean,&nbsp;mbd);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p>初始化完成之后就可以正常的使用 Bean 对象了，在 Spring 容器关闭时会执行销毁方法，但是 Spring 容器不会自动去调用销毁方法，而是需要我们主动的调用。</p>
<p>如果是 BeanFactory 容器，那么我们需要主动调用 destroySingletons() 方法，通知 BeanFactory 容器去执行相应的销毁方法；如果是 ApplicationContext 容器，那么我们需要主动调用 registerShutdownHook() 方法，告知 ApplicationContext 容器执行相应的销毁方法。</p>
<blockquote>
<p>注：本课时源码基于 Spring 5.2.2.RELEASE。</p>
</blockquote>
<h3>小结</h3>
<p>本课时我们讲了 Bean 的三种注册方式：XML、Java 注解和 JavaAPI，以及 Bean 的五个作用域：singleton、prototype、request、session 和 application；还讲了读取多个配置文件可能会出现同名 Bean 的问题，以及通过源码讲了 Bean 执行的生命周期，它的生命周期如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image3/M01/89/0C/Cgq2xl6WvHqAdmt4AABGAn2eSiI631.png" alt=""></p>

---

### 精选评论

##### **家：
> 对于单例非懒加载的bean，spring容器启动就会创建放入容器。

