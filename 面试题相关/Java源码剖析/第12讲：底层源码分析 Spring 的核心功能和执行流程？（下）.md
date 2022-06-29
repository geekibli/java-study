<p>上一课时我们讲了 Bean 相关的内容，它其实也是属于 IoC 的具体实现之一，本课时我们就来讲讲 Spring 中其他几个高频的面试点，希望能起到抛砖引玉的作用，能为你理解 Spring 打开一扇门。因为 Spring 涉及的内容和知识点太多了，用它来写一本书也绰绰有余，因此这里我们只讲核心的内容，希望下来你能查漏补缺，完善自己的 Spring 技术栈。</p>
<p>我们本课时的面试题是，谈一谈你对 IoC 和 DI 的理解。</p>
<h3>典型回答</h3>
<p><strong>IoC</strong>（Inversion of Control，翻译为“控制反转”）不是一个具体的技术，而是一种设计思想。与传统控制流相比，IoC 会颠倒控制流，在传统的编程中需要开发者自行创建并销毁对象，而在 IoC 中会把这些操作交给框架来处理，这样开发者就不用关注具体的实现细节了，拿来直接用就可以了，这就是<strong>控制反转</strong>。</p>
<p>IoC 很好的体现出了面向对象的设计法则之一——好莱坞法则：“别找我们，我们找你”。即由 IoC 容器帮对象找到相应的依赖对象并注入，而不是由对象主动去找。</p>
<p>举个例子，比如说传统找对象，先要设定好你的要求，如身高、体重、长相等，然后再一个一个的主动去找符合要求的对象，而 IoC 相当于，你把这些要求直接告诉婚介中心，由他们直接给你匹配到符合要求的对象，理想情况下是直接会帮你找到合适的对象，这就是传统编程模式和 IoC 的区别。</p>
<p><strong>DI</strong>（Dependency Injection，翻译为“<strong>依赖注入</strong>”）表示组件间的依赖关系交由容器在运行期自动生成，也就是说，由容器动态的将某个依赖关系注入到组件之中，这样就能提升组件的重用频率。通过依赖注入机制，我们只需要通过简单的配置，就可指定目标需要的资源，完成自身的业务逻辑，而不需要关心资源来自哪里、由谁实现等问题。</p>
<p>IoC 和 DI 其实是同一个概念从不同角度的描述的，由于控制反转这个概念比较含糊（可能只理解成了容器控制对象这一个层面，很难让人想到谁来维护对象关系），所以 2004 年被开发者尊称为“教父”的 Martin Fowler（世界顶级专家，敏捷开发方法的创始人之一）又给出了一个新的名字“依赖注入”，相对 IoC 而言，“依赖注入”明确描述了“<strong>被注入对象依赖 IoC 容器配置依赖对象</strong>”。</p>
<h3>考点分析</h3>
<p>IoC 和 DI 为 Spring 框架设计的精髓所在，也是面试中必问的考点之一，这个优秀的设计思想对于初学者来说可能理解起来比较困难，但对于 Spring 的使用者来说可以很快的看懂。因此如果对于此概念还有疑问的话，建议先上手使用 Spring 实现几个小功能再回头来看这些概念，相信你会豁然开朗。</p>
<p>Spring 相关的高频面试题，还有以下这些：</p>
<ul>
<li>Spring IoC 有哪些优势？</li>
<li>IoC 的注入方式有哪些？</li>
<li>谈一谈你对 AOP 的理解。</li>
</ul>
<h3>知识扩展</h3>
<h4>1.Spring IoC 的优点</h4>
<p>IoC 的优点有以下几个：</p>
<ul>
<li>使用更方便，拿来即用，无需显式的创建和销毁的过程；</li>
<li>可以很容易提供众多服务，比如事务管理、消息服务等；</li>
<li>提供了单例模式的支持；</li>
<li>提供了 AOP 抽象，利用它很容易实现权限拦截、运行期监控等功能；</li>
<li>更符合面向对象的设计法则；</li>
<li>低侵入式设计，代码的污染极低，降低了业务对象替换的复杂性。</li>
</ul>
<h4>2.Spring IoC 注入方式汇总</h4>
<p>IoC 的注入方式有三种：构造方法注入、Setter 注入和接口注入。</p>
<p><strong>① 构造方法注入</strong></p>
<p>构造方法注入主要是依赖于构造方法去实现，构造方法可以是有参的也可以是无参的，我们平时 new 对象时就是通过类的构造方法来创建类对象的，每个类对象默认会有一个无参的构造方法，Spring 通过构造方法注入的代码示例如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Person</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">Person</span><span class="hljs-params">()</span>&nbsp;</span>{
	}
	<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">Person</span><span class="hljs-params">(<span class="hljs-keyword">int</span>&nbsp;id,&nbsp;String&nbsp;name)</span>&nbsp;</span>{
		<span class="hljs-keyword">this</span>.id&nbsp;=&nbsp;id;
		<span class="hljs-keyword">this</span>.name&nbsp;=&nbsp;name;
	}
	<span class="hljs-keyword">private</span>&nbsp;<span class="hljs-keyword">int</span>&nbsp;id;
	<span class="hljs-keyword">private</span>&nbsp;String&nbsp;name;
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;忽略&nbsp;Setter、Getter&nbsp;的方法</span>
}
</code></pre>
<p>applicationContext.xml 配置如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">id</span>=<span class="hljs-string">"person"</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"org.springframework.beans.Person"</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">constructor-arg</span>&nbsp;<span class="hljs-attr">value</span>=<span class="hljs-string">"1"</span>&nbsp;<span class="hljs-attr">type</span>=<span class="hljs-string">"int"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">constructor-arg</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">constructor-arg</span>&nbsp;<span class="hljs-attr">value</span>=<span class="hljs-string">"Java"</span>&nbsp;<span class="hljs-attr">type</span>=<span class="hljs-string">"java.lang.String"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">constructor-arg</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<p><strong>② Setter 注入</strong></p>
<p>Setter 方法注入的方式是目前 Spring 主流的注入方式，它可以利用 Java Bean 规范所定义的 Setter/Getter 方法来完成注入，可读性和灵活性都很高，它不需要使用声明式构造方法，而是使用 Setter 注入直接设置相关的值，实现示例如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">id</span>=<span class="hljs-string">"person"</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"org.springframework.beans.Person"</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">property</span>&nbsp;<span class="hljs-attr">name</span>=<span class="hljs-string">"id"</span>&nbsp;<span class="hljs-attr">value</span>=<span class="hljs-string">"1"</span>/&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">property</span>&nbsp;<span class="hljs-attr">name</span>=<span class="hljs-string">"name"</span>&nbsp;<span class="hljs-attr">value</span>=<span class="hljs-string">"Java"</span>/&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<p><strong>③ 接口注入</strong></p>
<p>接口注入方式是比较古老的注入方式，因为它需要被依赖的对象实现不必要的接口，带有侵入性，因此现在已经被完全舍弃了，所以本文也不打算做过多的描述，大家只要知道有这回事就行了。</p>
<h4>3.Spring AOP</h4>
<p>AOP（Aspect-OrientedProgramming，面向切面编程）可以说是 OOP（Object-Oriented Programing，面向对象编程）的补充和完善，OOP 引入封装、继承和多态性等概念来建立一种公共对象处理的能力，当我们需要处理公共行为的时候，OOP 就会显得无能为力，而 AOP 的出现正好解决了这个问题。比如统一的日志处理模块、授权验证模块等都可以使用 AOP 很轻松的处理。</p>
<p>Spring AOP 目前提供了三种配置方式：</p>
<ul>
<li>基于 Java API 的方式；</li>
<li>基于 @AspectJ（Java）注解的方式；</li>
<li>基于 XML &lt;aop /&gt; 标签的方式。</li>
</ul>
<p><strong>① 基于 Java API 的方式</strong></p>
<p>此配置方式需要实现相关的接口，例如 MethodBeforeAdvice 和 AfterReturningAdvice，并且在 XML 配置中定义相应的规则即可实现。</p>
<p>我们先来定义一个实体类，代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">package</span>&nbsp;org.springframework.beans;


<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Person</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Person&nbsp;<span class="hljs-title">findPerson</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Person&nbsp;person&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;Person(<span class="hljs-number">1</span>,&nbsp;<span class="hljs-string">"JDK"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"findPerson&nbsp;被执行"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;person;
&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">Person</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-title">Person</span><span class="hljs-params">(Integer&nbsp;id,&nbsp;String&nbsp;name)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.id&nbsp;=&nbsp;id;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">this</span>.name&nbsp;=&nbsp;name;
&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;Integer&nbsp;id;
&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">private</span>&nbsp;String&nbsp;name;
&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;忽略&nbsp;Getter、Setter&nbsp;方法</span>
}
</code></pre>
<p>再定义一个 advice 类，用于对拦截方法的调用之前和调用之后进行相关的业务处理，实现代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">import</span>&nbsp;org.springframework.aop.AfterReturningAdvice;
<span class="hljs-keyword">import</span>&nbsp;org.springframework.aop.MethodBeforeAdvice;

<span class="hljs-keyword">import</span>&nbsp;java.lang.reflect.Method;

<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">MyAdvice</span>&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">MethodBeforeAdvice</span>,&nbsp;<span class="hljs-title">AfterReturningAdvice</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">before</span><span class="hljs-params">(Method&nbsp;method,&nbsp;Object[]&nbsp;args,&nbsp;Object&nbsp;target)</span>&nbsp;<span class="hljs-keyword">throws</span>&nbsp;Throwable&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"准备执行方法:&nbsp;"</span>&nbsp;+&nbsp;method.getName());
&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Override</span>
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">afterReturning</span><span class="hljs-params">(Object&nbsp;returnValue,&nbsp;Method&nbsp;method,&nbsp;Object[]&nbsp;args,&nbsp;Object&nbsp;target)</span>&nbsp;<span class="hljs-keyword">throws</span>&nbsp;Throwable&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(method.getName()&nbsp;+&nbsp;<span class="hljs-string">"&nbsp;方法执行结束"</span>);
&nbsp;&nbsp;&nbsp;}
</code></pre>
<p>然后需要在 application.xml 文件中配置相应的拦截规则，配置如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-comment">&lt;!--&nbsp;定义&nbsp;advisor&nbsp;--&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">id</span>=<span class="hljs-string">"myAdvice"</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"org.springframework.advice.MyAdvice"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
<span class="hljs-comment">&lt;!--&nbsp;配置规则，拦截方法名称为&nbsp;find*&nbsp;--&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"org.springframework.aop.support.RegexpMethodPointcutAdvisor"</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">property</span>&nbsp;<span class="hljs-attr">name</span>=<span class="hljs-string">"advice"</span>&nbsp;<span class="hljs-attr">ref</span>=<span class="hljs-string">"myAdvice"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">property</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">property</span>&nbsp;<span class="hljs-attr">name</span>=<span class="hljs-string">"pattern"</span>&nbsp;<span class="hljs-attr">value</span>=<span class="hljs-string">"org.springframework.beans.*.find.*"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">property</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>

<span class="hljs-comment">&lt;!--&nbsp;定义&nbsp;DefaultAdvisorAutoProxyCreator&nbsp;使所有的&nbsp;advisor&nbsp;配置自动生效&nbsp;--&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
</code></pre>
<p>从以上配置中可以看出，我们需要配置一个拦截方法的规则，然后定义一个 DefaultAdvisorAutoProxyCreator 让所有的 advisor 配置自动生效。</p>
<p>最后，我们使用测试代码来完成调用：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">MyApplication</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">main</span><span class="hljs-params">(String[]&nbsp;args)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ApplicationContext&nbsp;context&nbsp;=
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">new</span>&nbsp;ClassPathXmlApplicationContext(<span class="hljs-string">"classpath*:application.xml"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Person&nbsp;person&nbsp;=&nbsp;context.getBean(<span class="hljs-string">"person"</span>,&nbsp;Person<span class="hljs-class">.<span class="hljs-keyword">class</span>)</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;person.findPerson();
&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p>以上程序的执行结果为：</p>
<pre><code data-language="java" class="lang-java">准备执行方法:&nbsp;findPerson
findPerson&nbsp;被执行
findPerson&nbsp;方法执行结束
</code></pre>
<p>可以看出 AOP 的拦截已经成功了。</p>
<p><strong>② 基于 @AspectJ 注解的方式</strong></p>
<p>首先需要在项目中添加 aspectjweaver 的 jar 包，配置如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-comment">&lt;!--&nbsp;https://mvnrepository.com/artifact/org.aspectj/aspectjweaver&nbsp;--&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.aspectj<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>aspectjweaver<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">version</span>&gt;</span>1.9.5<span class="hljs-tag">&lt;/<span class="hljs-name">version</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span>
</code></pre>
<p>此 jar 包来自于 AspectJ，因为 Spring 使用了 AspectJ 提供的一些注解，因此需要添加此 jar 包。之后，我们需要开启 @AspectJ 的注解，开启方式有两种。</p>
<p>可以在 application.xml 配置如下代码中开启 @AspectJ 的注解：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-tag">&lt;<span class="hljs-name">aop:aspectj-autoproxy</span>/&gt;</span>
</code></pre>
<p>也可以使用 @EnableAspectJAutoProxy 注解开启，代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-meta">@Configuration</span>
<span class="hljs-meta">@EnableAspectJAutoProxy</span>
<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">AppConfig</span>&nbsp;</span>{
}
</code></pre>
<p>之后我们需要声明拦截器的类和拦截方法，以及配置相应的拦截规则，代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">import</span>&nbsp;org.aspectj.lang.annotation.After;
<span class="hljs-keyword">import</span>&nbsp;org.aspectj.lang.annotation.Aspect;
<span class="hljs-keyword">import</span>&nbsp;org.aspectj.lang.annotation.Before;
<span class="hljs-keyword">import</span>&nbsp;org.aspectj.lang.annotation.Pointcut;

<span class="hljs-meta">@Aspect</span>
<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">MyAspectJ</span>&nbsp;</span>{

&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;配置拦截类&nbsp;Person</span>
&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Pointcut</span>(<span class="hljs-string">"execution(*&nbsp;org.springframework.beans.Person.*(..))"</span>)
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">pointCut</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@Before</span>(<span class="hljs-string">"pointCut()"</span>)
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">doBefore</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"执行&nbsp;doBefore&nbsp;方法"</span>);
&nbsp;&nbsp;&nbsp;}

&nbsp;&nbsp;&nbsp;<span class="hljs-meta">@After</span>(<span class="hljs-string">"pointCut()"</span>)
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">doAfter</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"执行&nbsp;doAfter&nbsp;方法"</span>);
</code></pre>
<p>然后我们只需要在 application.xml 配置中添加注解类，配置如下：</p>
<pre><code data-language="java" class="lang-java">&lt;bean&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span></span>=<span class="hljs-string">"org.springframework.advice.MyAspectJ"</span>/&gt;
</code></pre>
<p>紧接着，我们添加一个需要拦截的方法：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">package</span>&nbsp;org.springframework.beans;

<span class="hljs-comment">//&nbsp;需要拦截的&nbsp;Bean</span>
<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">Person</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;Person&nbsp;<span class="hljs-title">findPerson</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Person&nbsp;person&nbsp;=&nbsp;<span class="hljs-keyword">new</span>&nbsp;Person(<span class="hljs-number">1</span>,&nbsp;<span class="hljs-string">"JDK"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"执行&nbsp;findPerson&nbsp;方法"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">return</span>&nbsp;person;
&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;获取其他方法</span>
}
</code></pre>
<p>最后，我们开启测试代码：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">import</span>&nbsp;org.springframework.beans.Person;
<span class="hljs-keyword">import</span>&nbsp;org.springframework.context.ApplicationContext;
<span class="hljs-keyword">import</span>&nbsp;org.springframework.context.support.ClassPathXmlApplicationContext;

<span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">MyApplication</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">static</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">main</span><span class="hljs-params">(String[]&nbsp;args)</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ApplicationContext&nbsp;context&nbsp;=
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-keyword">new</span>&nbsp;ClassPathXmlApplicationContext(<span class="hljs-string">"classpath*:application.xml"</span>);
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Person&nbsp;person&nbsp;=&nbsp;context.getBean(<span class="hljs-string">"person"</span>,&nbsp;Person<span class="hljs-class">.<span class="hljs-keyword">class</span>)</span>;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;person.findPerson();
&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p>以上程序的执行结果为：</p>
<pre><code data-language="java" class="lang-java">执行&nbsp;doBefore&nbsp;方法
执行&nbsp;findPerson&nbsp;方法
执行&nbsp;doAfter&nbsp;方法
</code></pre>
<p>可以看出 AOP 拦截成功了。</p>
<p><strong>③ 基于 XML &lt;aop /&gt; 标签的方式</strong></p>
<p>基于 XML 的方式与基于注解的方式类似，只是无需使用注解，把相关信息配置到 application.xml 中即可，配置如下：</p>
<pre><code data-language="html" class="lang-html"><span class="hljs-comment">&lt;!--&nbsp;拦截处理类&nbsp;--&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">bean</span>&nbsp;<span class="hljs-attr">id</span>=<span class="hljs-string">"myPointcut"</span>&nbsp;<span class="hljs-attr">class</span>=<span class="hljs-string">"org.springframework.advice.MyPointcut"</span>&gt;</span><span class="hljs-tag">&lt;/<span class="hljs-name">bean</span>&gt;</span>
<span class="hljs-tag">&lt;<span class="hljs-name">aop:config</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">&lt;!--&nbsp;拦截规则配置&nbsp;--&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">aop:pointcut</span>&nbsp;<span class="hljs-attr">id</span>=<span class="hljs-string">"pointcutConfig"</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-attr">expression</span>=<span class="hljs-string">"execution(*&nbsp;org.springframework.beans.Person.*(..))"</span>/&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">&lt;!--&nbsp;拦截方法配置&nbsp;--&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">aop:aspect</span>&nbsp;<span class="hljs-attr">ref</span>=<span class="hljs-string">"myPointcut"</span>&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">aop:before</span>&nbsp;<span class="hljs-attr">method</span>=<span class="hljs-string">"doBefore"</span>&nbsp;<span class="hljs-attr">pointcut-ref</span>=<span class="hljs-string">"pointcutConfig"</span>/&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">aop:after</span>&nbsp;<span class="hljs-attr">method</span>=<span class="hljs-string">"doAfter"</span>&nbsp;<span class="hljs-attr">pointcut-ref</span>=<span class="hljs-string">"pointcutConfig"</span>/&gt;</span>
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-tag">&lt;/<span class="hljs-name">aop:aspect</span>&gt;</span>
<span class="hljs-tag">&lt;/<span class="hljs-name">aop:config</span>&gt;</span>
</code></pre>
<p>之后，添加一个普通的类来进行拦截业务的处理，实现代码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">MyPointcut</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">doBefore</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"执行&nbsp;doBefore&nbsp;方法"</span>);
&nbsp;&nbsp;&nbsp;}
&nbsp;&nbsp;&nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-keyword">void</span>&nbsp;<span class="hljs-title">doAfter</span><span class="hljs-params">()</span>&nbsp;</span>{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;System.out.println(<span class="hljs-string">"执行&nbsp;doAfter&nbsp;方法"</span>);
&nbsp;&nbsp;&nbsp;}
}
</code></pre>
<p>拦截的方法和测试代码与第二种注解的方式相同，这里就不在赘述。</p>
<p>最后执行程序，执行结果为：</p>
<pre><code data-language="java" class="lang-java">执行 doBefore 方法
执行 findPerson 方法
执行 doAfter 方法
</code></pre>
<p>可以看出 AOP 拦截成功了。</p>
<p>Spring AOP 的原理其实很简单，它其实就是一个动态代理，我们在调用 getBean() 方法的时候返回的其实是代理类的实例，而这个代理类在 Spring 中使用的是 JDK Proxy 或 CgLib 实现的，它的核心代码在 DefaultAopProxyFactory#createAopProxy(...) 中，源码如下：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-keyword">public</span>&nbsp;<span class="hljs-class"><span class="hljs-keyword">class</span>&nbsp;<span class="hljs-title">DefaultAopProxyFactory</span>&nbsp;<span class="hljs-keyword">implements</span>&nbsp;<span class="hljs-title">AopProxyFactory</span>,&nbsp;<span class="hljs-title">Serializable</span>&nbsp;</span>{

	<span class="hljs-meta">@Override</span>
	<span class="hljs-function"><span class="hljs-keyword">public</span>&nbsp;AopProxy&nbsp;<span class="hljs-title">createAopProxy</span><span class="hljs-params">(AdvisedSupport&nbsp;config)</span>&nbsp;<span class="hljs-keyword">throws</span>&nbsp;AopConfigException&nbsp;</span>{
		<span class="hljs-keyword">if</span>&nbsp;(config.isOptimize()&nbsp;||&nbsp;config.isProxyTargetClass()&nbsp;||&nbsp;hasNoUserSuppliedProxyInterfaces(config))&nbsp;{
			Class&lt;?&gt;&nbsp;targetClass&nbsp;=&nbsp;config.getTargetClass();
			<span class="hljs-keyword">if</span>&nbsp;(targetClass&nbsp;==&nbsp;<span class="hljs-keyword">null</span>)&nbsp;{
				<span class="hljs-keyword">throw</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;AopConfigException(<span class="hljs-string">"TargetSource&nbsp;cannot&nbsp;determine&nbsp;target&nbsp;class:&nbsp;"</span>&nbsp;+
						<span class="hljs-string">"Either&nbsp;an&nbsp;interface&nbsp;or&nbsp;a&nbsp;target&nbsp;is&nbsp;required&nbsp;for&nbsp;proxy&nbsp;creation."</span>);
			}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;判断目标类是否为接口</span>
			<span class="hljs-keyword">if</span>&nbsp;(targetClass.isInterface()&nbsp;||&nbsp;Proxy.isProxyClass(targetClass))&nbsp;{
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;是接口使用&nbsp;jdk&nbsp;的代理</span>
				<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;JdkDynamicAopProxy(config);
			}
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;其他情况使用&nbsp;CgLib&nbsp;代理</span>
			<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;ObjenesisCglibAopProxy(config);
		}
		<span class="hljs-keyword">else</span>&nbsp;{
			<span class="hljs-keyword">return</span>&nbsp;<span class="hljs-keyword">new</span>&nbsp;JdkDynamicAopProxy(config);
		}
	}
&nbsp;&nbsp;&nbsp;&nbsp;<span class="hljs-comment">//&nbsp;忽略其他代码</span>
}
</code></pre>
<h3>小结</h3>
<p>本课时讲了 IoC 和 DI 概念，以及 IoC 的优势和 IoC 注入的三种方式：构造方法注入、Setter 注入和接口注入，最后讲了 Spring AOP 的概念与它的三种配置方式：基于 Java API 的方式、基于 Java 注解的方式和基于 XML 标签的方式。</p>

---

### 精选评论

##### **明：
> 您好，王老师，在spring boot项目中通过注解来使用AOP的引入功能，当这样使用时<font color="#bbb529" face="宋体"><span style="font-size: 17.0667px;">@DeclareParents(value = "com.example.eatscent.until.SendEmail+",defaultImpl = emailInfoIntroductionimpl.class)</span></font><span style="font-size: 17.0667px; color: rgb(187, 181, 41); font-family: 宋体;">为什么会在项目启动的时候报错</span><div><div><div style=""><br></div><div style="font-size: 0.427rem;"><br></div></div></div>

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 这个要发一下报错信息，建议谷歌一把，就知道了哈。

##### *伟：
> 开发中，自定义注解结合AOP确实是太好用了，就是对底层理解不够深入，祝自己早日进入大厂😀😀

##### *涛：
> 依赖注入相对于IoC而言：被注入对象，依赖于IoC容器来配置依赖关系

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; IoC 是一种编程思想，而 DI（依赖注入）则是这种思想的具体实现，在刚开始理解的时候可以把二者认为是一回事。

##### **4804：
> IoC不是还有一种注解方式进行注入吗

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 是的，本文列举了比较常见的3种。

