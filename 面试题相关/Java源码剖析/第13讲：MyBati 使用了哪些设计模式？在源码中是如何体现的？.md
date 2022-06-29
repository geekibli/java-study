<p data-nodeid="769" class="">MyBatis 的前身是 IBatis，IBatis 是由 Internet 和 Abatis 组合而成，其目的是想当做互联网的篱笆墙，围绕着数据库提供持久化服务的一个框架，2010 年正式改名为 MyBatis。它是一款优秀的持久层框架，支持自定义 SQL、存储过程及高级映射。MyBatis 免除了几乎所有的 JDBC 代码以及设置参数和获取结果集的工作，还可以通过简单的 XML 或注解来配置和映射原始类型、接口和 Java POJO（Plain Ordinary Java Object，普通 Java 对象）为数据库中的记录。</p>
<p data-nodeid="770">关于 MyBatis 的介绍与使用，官方已经提供了比较详尽的中文参考文档，<a href="https://mybatis.org/mybatis-3/zh/getting-started.html" data-nodeid="865">可点击这里查看</a>，而本课时则以面试的角度出发，聊一聊不一样的知识点，它也是 MyBatis 比较热门的面试题之一，MyBatis 使用了哪些设计模式？在源码中是如何体现的？</p>
<blockquote data-nodeid="771">
<p data-nodeid="772">注意：本课时使用的 MyBatis 源码为 3.5.5。</p>
</blockquote>
<h3 data-nodeid="773">典型回答</h3>
<h4 data-nodeid="774">1.工厂模式</h4>
<p data-nodeid="775">工厂模式想必都比较熟悉，它是 Java 中最常用的设计模式之一。工厂模式就是提供一个工厂类，当有客户端需要调用的时候，只调用这个工厂类就可以得到自己想要的结果，从而无需关注某类的具体实现过程。这就好比你去餐馆吃饭，可以直接点菜，而不用考虑厨师是怎么做的。</p>
<p data-nodeid="776">工厂模式在 MyBatis 中的典型代表是 SqlSessionFactory。</p>
<p data-nodeid="777">SqlSession 是 MyBatis 中的重要 Java 接口，可以通过该接口来执行 SQL 命令、获取映射器示例和管理事务，而 SqlSessionFactory 正是用来产生 SqlSession 对象的，所以它在 MyBatis 中是比较核心的接口之一。</p>
<p data-nodeid="961" class="te-preview-highlight">工厂模式应用解析：SqlSessionFactory 是一个接口类，它的子类 DefaultSqlSessionFactory 有一个 openSession(ExecutorType execType) 的方法，其中使用了工厂模式，源码如下：</p>

<pre class="lang-java" data-nodeid="779"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">private</span> SqlSession <span class="hljs-title">openSessionFromDataSource</span><span class="hljs-params">(ExecutorType execType, TransactionIsolationLevel level, <span class="hljs-keyword">boolean</span> autoCommit)</span> </span>{
    Transaction tx = <span class="hljs-keyword">null</span>;
    <span class="hljs-keyword">try</span> {
        <span class="hljs-keyword">final</span> Environment environment = configuration.getEnvironment();
        <span class="hljs-keyword">final</span> TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
        tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);
        <span class="hljs-keyword">final</span> Executor executor = configuration.newExecutor(tx, execType);
        <span class="hljs-keyword">return</span> <span class="hljs-keyword">new</span> DefaultSqlSession(configuration, executor, autoCommit);
    } <span class="hljs-keyword">catch</span> (Exception e) {
        closeTransaction(tx); 
        <span class="hljs-keyword">throw</span> ExceptionFactory.wrapException(<span class="hljs-string">"Error opening session.  Cause: "</span> + e, e);
    } <span class="hljs-keyword">finally</span> {
        ErrorContext.instance().reset();
    }
}
</code></pre>
<p data-nodeid="780">从该方法我们可以看出它会 configuration.newExecutor(tx, execType) 读取对应的环境配置，而此方法的源码如下：</p>
<pre class="lang-java" data-nodeid="781"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> Executor <span class="hljs-title">newExecutor</span><span class="hljs-params">(Transaction transaction, ExecutorType executorType)</span> </span>{
    executorType = executorType == <span class="hljs-keyword">null</span> ? defaultExecutorType : executorType;
    executorType = executorType == <span class="hljs-keyword">null</span> ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    <span class="hljs-keyword">if</span> (ExecutorType.BATCH == executorType) {
        executor = <span class="hljs-keyword">new</span> BatchExecutor(<span class="hljs-keyword">this</span>, transaction);
    } <span class="hljs-keyword">else</span> <span class="hljs-keyword">if</span> (ExecutorType.REUSE == executorType) {
        executor = <span class="hljs-keyword">new</span> ReuseExecutor(<span class="hljs-keyword">this</span>, transaction);
    } <span class="hljs-keyword">else</span> {
        executor = <span class="hljs-keyword">new</span> SimpleExecutor(<span class="hljs-keyword">this</span>, transaction);
    }
    <span class="hljs-keyword">if</span> (cacheEnabled) {
        executor = <span class="hljs-keyword">new</span> CachingExecutor(executor);
    }
    executor = (Executor) interceptorChain.pluginAll(executor);
    <span class="hljs-keyword">return</span> executor;
}
</code></pre>
<p data-nodeid="782">可以看出 newExecutor() 方法为标准的工厂模式，它会根据传递 ExecutorType 值生成相应的对象然后进行返回。</p>
<h4 data-nodeid="783">2.建造者模式（Builder）</h4>
<p data-nodeid="784">建造者模式指的是将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示。也就是说建造者模式是通过多个模块一步步实现了对象的构建，相同的构建过程可以创建不同的产品。</p>
<p data-nodeid="785">例如，组装电脑，最终的产品就是一台主机，然而不同的人对它的要求是不同的，比如设计人员需要显卡配置高的；而影片爱好者则需要硬盘足够大的（能把视频都保存起来），但对于显卡却没有太大的要求，我们的装机人员根据每个人不同的要求，组装相应电脑的过程就是<strong data-nodeid="883">建造者模式</strong>。</p>
<p data-nodeid="786">建造者模式在 MyBatis 中的典型代表是 SqlSessionFactoryBuilder。</p>
<p data-nodeid="787">普通的对象都是通过 new 关键字直接创建的，但是如果创建对象需要的构造参数很多，且不能保证每个参数都是正确的或者不能一次性得到构建所需的所有参数，那么就需要将构建逻辑从对象本身抽离出来，让对象只关注功能，把构建交给构建类，这样可以简化对象的构建，也可以达到分步构建对象的目的，而 SqlSessionFactoryBuilder 的构建过程正是如此。</p>
<p data-nodeid="788">在 SqlSessionFactoryBuilder 中构建 SqlSessionFactory 对象的过程是这样的，首先需要通过 XMLConfigBuilder 对象读取并解析 XML 的配置文件，然后再将读取到的配置信息存入到 Configuration 类中，然后再通过 build 方法生成我们需要的 DefaultSqlSessionFactory 对象，实现源码如下（在 SqlSessionFactoryBuilder 类中）：</p>
<pre class="lang-java" data-nodeid="789"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> SqlSessionFactory <span class="hljs-title">build</span><span class="hljs-params">(InputStream inputStream, String environment, Properties properties)</span> </span>{
    <span class="hljs-keyword">try</span> {
        XMLConfigBuilder parser = <span class="hljs-keyword">new</span> XMLConfigBuilder(inputStream, environment, properties);
        <span class="hljs-keyword">return</span> build(parser.parse());
    } <span class="hljs-keyword">catch</span> (Exception e) {
        <span class="hljs-keyword">throw</span> ExceptionFactory.wrapException(<span class="hljs-string">"Error building SqlSession."</span>, e);
    } <span class="hljs-keyword">finally</span> {
        ErrorContext.instance().reset();
        <span class="hljs-keyword">try</span> {
        inputStream.close();
        } <span class="hljs-keyword">catch</span> (IOException e) {
        <span class="hljs-comment">// Intentionally ignore. Prefer previous error.</span>
        }
    }
}
<span class="hljs-function"><span class="hljs-keyword">public</span> SqlSessionFactory <span class="hljs-title">build</span><span class="hljs-params">(Configuration config)</span> </span>{
    <span class="hljs-keyword">return</span> <span class="hljs-keyword">new</span> DefaultSqlSessionFactory(config);
}
</code></pre>
<p data-nodeid="790">SqlSessionFactoryBuilder 类相当于一个建造工厂，先读取文件或者配置信息、再解析配置、然后通过反射生成对象，最后再把结果存入缓存，这样就一步步构建造出一个 SqlSessionFactory 对象。</p>
<h4 data-nodeid="791">3.单例模式</h4>
<p data-nodeid="792">单例模式（Singleton Pattern）是 Java 中最简单的设计模式之一，此模式保证某个类在运行期间，只有一个实例对外提供服务，而这个类被称为<strong data-nodeid="894">单例类</strong>。</p>
<p data-nodeid="793">单例模式也比较好理解，比如一个人一生当中只能有一个真实的身份证号，每个收费站的窗口都只能一辆车子一辆车子的经过，类似的场景都是属于<strong data-nodeid="900">单例模式</strong>。</p>
<p data-nodeid="794">单例模式在 MyBatis 中的典型代表是 ErrorContext。</p>
<p data-nodeid="795">ErrorContext 是线程级别的的单例，每个线程中有一个此对象的单例，用于记录该线程的执行环境的错误信息。</p>
<p data-nodeid="796">ErrorContext 的实现源码如下：</p>
<pre class="lang-java" data-nodeid="797"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">ErrorContext</span> </span>{
  <span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> String LINE_SEPARATOR = System.lineSeparator();
  <span class="hljs-comment">// 每个线程存储的容器</span>
  <span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> ThreadLocal&lt;ErrorContext&gt; LOCAL = ThreadLocal.withInitial(ErrorContext::<span class="hljs-keyword">new</span>);
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> ErrorContext <span class="hljs-title">instance</span><span class="hljs-params">()</span> </span>{
    <span class="hljs-keyword">return</span> LOCAL.get();
  }
  <span class="hljs-comment">// 忽略其他</span>
}
</code></pre>
<p data-nodeid="798">可以看出 ErrorContext 使用 private 修饰的 ThreadLocal 来保证每个线程拥有一个 ErrorContext 对象，在调用 instance() 方法时再从 ThreadLocal 中获取此单例对象。</p>
<h4 data-nodeid="799">4.适配器模式</h4>
<p data-nodeid="800">适配器模式是指将一个不兼容的接口转换成另一个可以兼容的接口，这样就可以使那些不兼容的类可以一起工作。</p>
<p data-nodeid="801">例如，最早之前我们用的耳机都是圆形的，而现在大多数的耳机和电源都统一成了方形的 typec 接口，那之前的圆形耳机就不能使用了，只能买一个适配器把圆形接口转化成方形的，如下图所示：</p>
<p data-nodeid="802"><img src="https://s0.lgstatic.com/i/image3/M01/13/8A/Ciqah16f6OqAIKAYAADKCLdmqIs159.png" alt="面试1.png" data-nodeid="910"></p>
<p data-nodeid="803">而这个转换头就相当于程序中的适配器模式，适配器模式在 MyBatis 中的典型代表是 Log。</p>
<p data-nodeid="804">MyBatis 中的日志模块适配了以下多种日志类型：</p>
<ul data-nodeid="805">
<li data-nodeid="806">
<p data-nodeid="807">SLF4J</p>
</li>
<li data-nodeid="808">
<p data-nodeid="809">Apache Commons Logging</p>
</li>
<li data-nodeid="810">
<p data-nodeid="811">Log4j 2</p>
</li>
<li data-nodeid="812">
<p data-nodeid="813">Log4j</p>
</li>
<li data-nodeid="814">
<p data-nodeid="815">JDK logging</p>
</li>
</ul>
<p data-nodeid="816">首先 MyBatis 定义了一个 Log 的接口，用于统一和规范接口的行为，源码如下：</p>
<pre class="lang-java" data-nodeid="817"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">interface</span> <span class="hljs-title">Log</span> </span>{
  <span class="hljs-function"><span class="hljs-keyword">boolean</span> <span class="hljs-title">isDebugEnabled</span><span class="hljs-params">()</span></span>;
  <span class="hljs-function"><span class="hljs-keyword">boolean</span> <span class="hljs-title">isTraceEnabled</span><span class="hljs-params">()</span></span>;
  <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">error</span><span class="hljs-params">(String s, Throwable e)</span></span>;
  <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">error</span><span class="hljs-params">(String s)</span></span>;
  <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">debug</span><span class="hljs-params">(String s)</span></span>;
  <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">trace</span><span class="hljs-params">(String s)</span></span>;
  <span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">warn</span><span class="hljs-params">(String s)</span></span>;
}
</code></pre>
<p data-nodeid="818">然后 MyBatis 定义了多个适配接口，例如 Log4j2 实现源码如下：</p>
<pre class="lang-java" data-nodeid="819"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Log4j2Impl</span> <span class="hljs-keyword">implements</span> <span class="hljs-title">Log</span> </span>{
  <span class="hljs-keyword">private</span> <span class="hljs-keyword">final</span> Log log;
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-title">Log4j2Impl</span><span class="hljs-params">(String clazz)</span> </span>{
    Logger logger = LogManager.getLogger(clazz);
    <span class="hljs-keyword">if</span> (logger <span class="hljs-keyword">instanceof</span> AbstractLogger) {
      log = <span class="hljs-keyword">new</span> Log4j2AbstractLoggerImpl((AbstractLogger) logger);
    } <span class="hljs-keyword">else</span> {
      log = <span class="hljs-keyword">new</span> Log4j2LoggerImpl(logger);
    }
  }
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">isDebugEnabled</span><span class="hljs-params">()</span> </span>{
    <span class="hljs-keyword">return</span> log.isDebugEnabled();
  }
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">isTraceEnabled</span><span class="hljs-params">()</span> </span>{
    <span class="hljs-keyword">return</span> log.isTraceEnabled();
  }
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">error</span><span class="hljs-params">(String s, Throwable e)</span> </span>{
    log.error(s, e);
  }
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">error</span><span class="hljs-params">(String s)</span> </span>{
    log.error(s);
  }
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">debug</span><span class="hljs-params">(String s)</span> </span>{
    log.debug(s);
  }
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">trace</span><span class="hljs-params">(String s)</span> </span>{
    log.trace(s);
  }
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">warn</span><span class="hljs-params">(String s)</span> </span>{
    log.warn(s);
  }
}
</code></pre>
<p data-nodeid="820">这样当你项目中添加了 Log4j2 时，MyBatis 就可以直接使用它打印 MyBatis 的日志信息了。Log 的所有子类如下图所示：</p>
<p data-nodeid="821"><img src="https://s0.lgstatic.com/i/image3/M01/13/7A/Ciqah16f2v2AeeNMAAFjYUNVZ4Q221.png" alt="面试2.png" data-nodeid="923"></p>
<h4 data-nodeid="822">5.代理模式</h4>
<p data-nodeid="823">代理模式指的是给某一个对象提供一个代理对象，并由代理对象控制原对象的调用。</p>
<p data-nodeid="824">代理模式在生活中也比较常见，比如我们常见的超市、小卖店其实都是一个个“代理”，他们的最上游是一个个生产厂家，他们这些代理负责把厂家生产出来的产品卖出去。</p>
<p data-nodeid="825">代理模式在 MyBatis 中的典型代表是 MapperProxyFactory。</p>
<p data-nodeid="826">MapperProxyFactory 的 newInstance() 方法就是生成一个具体的代理来实现功能的，源码如下：</p>
<pre class="lang-java" data-nodeid="827"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">MapperProxyFactory</span>&lt;<span class="hljs-title">T</span>&gt; </span>{
  <span class="hljs-keyword">private</span> <span class="hljs-keyword">final</span> Class&lt;T&gt; mapperInterface;
  <span class="hljs-keyword">private</span> <span class="hljs-keyword">final</span> Map&lt;Method, MapperMethodInvoker&gt; methodCache = <span class="hljs-keyword">new</span> ConcurrentHashMap&lt;&gt;();
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-title">MapperProxyFactory</span><span class="hljs-params">(Class&lt;T&gt; mapperInterface)</span> </span>{
    <span class="hljs-keyword">this</span>.mapperInterface = mapperInterface;
  }
  <span class="hljs-function"><span class="hljs-keyword">public</span> Class&lt;T&gt; <span class="hljs-title">getMapperInterface</span><span class="hljs-params">()</span> </span>{
    <span class="hljs-keyword">return</span> mapperInterface;
  }
  <span class="hljs-function"><span class="hljs-keyword">public</span> Map&lt;Method, MapperMethodInvoker&gt; <span class="hljs-title">getMethodCache</span><span class="hljs-params">()</span> </span>{
    <span class="hljs-keyword">return</span> methodCache;
  }
  <span class="hljs-comment">// 创建代理类</span>
  <span class="hljs-meta">@SuppressWarnings("unchecked")</span>
  <span class="hljs-function"><span class="hljs-keyword">protected</span> T <span class="hljs-title">newInstance</span><span class="hljs-params">(MapperProxy&lt;T&gt; mapperProxy)</span> </span>{
    <span class="hljs-keyword">return</span> (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), <span class="hljs-keyword">new</span> Class[] { mapperInterface }, mapperProxy);
  }
  <span class="hljs-function"><span class="hljs-keyword">public</span> T <span class="hljs-title">newInstance</span><span class="hljs-params">(SqlSession sqlSession)</span> </span>{
    <span class="hljs-keyword">final</span> MapperProxy&lt;T&gt; mapperProxy = <span class="hljs-keyword">new</span> MapperProxy&lt;&gt;(sqlSession, mapperInterface, methodCache);
    <span class="hljs-keyword">return</span> newInstance(mapperProxy);
  }
}
</code></pre>
<h4 data-nodeid="828">6.模板方法模式</h4>
<p data-nodeid="829">模板方法模式是最常用的设计模式之一，它是指定义一个操作算法的骨架，而将一些步骤的实现延迟到子类中去实现，使得子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤。此模式是基于继承的思想实现代码复用的。</p>
<p data-nodeid="830">例如，我们喝茶的一般步骤都是这样的：</p>
<ul data-nodeid="831">
<li data-nodeid="832">
<p data-nodeid="833">把热水烧开</p>
</li>
<li data-nodeid="834">
<p data-nodeid="835">把茶叶放入壶中</p>
</li>
<li data-nodeid="836">
<p data-nodeid="837">等待一分钟左右</p>
</li>
<li data-nodeid="838">
<p data-nodeid="839">把茶倒入杯子中</p>
</li>
<li data-nodeid="840">
<p data-nodeid="841">喝茶</p>
</li>
</ul>
<p data-nodeid="842">整个过程都是固定的，唯一变的就是泡入茶叶种类的不同，比如今天喝的是绿茶，明天可能喝的是红茶，那么我们就可以把流程定义为一个模板，而把茶叶的种类延伸到子类中去实现，这就是模板方法的实现思路。</p>
<p data-nodeid="843">模板方法在 MyBatis 中的典型代表是 BaseExecutor。</p>
<p data-nodeid="844">在 MyBatis 中 BaseExecutor 实现了大部分 SQL 执行的逻辑，然后再把几个方法交给子类来实现，它的继承关系如下图所示：</p>
<p data-nodeid="845"><img src="https://s0.lgstatic.com/i/image3/M01/06/4C/CgoCgV6f2wyAULMBAABm_m36wXw649.png" alt="面试3.png" data-nodeid="942"></p>
<p data-nodeid="846">比如 doUpdate() 就是交给子类自己去实现的，它在 BaseExecutor 中的定义如下：</p>
<pre class="lang-java" data-nodeid="847"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">protected</span> <span class="hljs-keyword">abstract</span> <span class="hljs-keyword">int</span> <span class="hljs-title">doUpdate</span><span class="hljs-params">(MappedStatement ms, Object parameter)</span> <span class="hljs-keyword">throws</span> SQLException</span>;
</code></pre>
<p data-nodeid="848">在 SimpleExecutor 中的实现如下：</p>
<pre class="lang-java" data-nodeid="849"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">SimpleExecutor</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">BaseExecutor</span> </span>{
  <span class="hljs-comment">// 构造方法</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-title">SimpleExecutor</span><span class="hljs-params">(Configuration configuration, Transaction transaction)</span> </span>{
    <span class="hljs-keyword">super</span>(configuration, transaction);
  }
  <span class="hljs-comment">// 更新方法</span>
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">int</span> <span class="hljs-title">doUpdate</span><span class="hljs-params">(MappedStatement ms, Object parameter)</span> <span class="hljs-keyword">throws</span> SQLException </span>{
    Statement stmt = <span class="hljs-keyword">null</span>;
    <span class="hljs-keyword">try</span> {
      Configuration configuration = ms.getConfiguration();
      StatementHandler handler = configuration.newStatementHandler(<span class="hljs-keyword">this</span>, ms, parameter, RowBounds.DEFAULT, <span class="hljs-keyword">null</span>, <span class="hljs-keyword">null</span>);
      stmt = prepareStatement(handler, ms.getStatementLog());
      <span class="hljs-keyword">return</span> handler.update(stmt);
    } <span class="hljs-keyword">finally</span> {
      closeStatement(stmt);
    }
  }
  <span class="hljs-comment">// 忽略其他代码...</span>
}
</code></pre>
<p data-nodeid="850">可以看出 SimpleExecutor 每次使用完 Statement 对象之后，都会把它关闭掉，而 ReuseExecutor 中的实现源码如下：</p>
<pre class="lang-java" data-nodeid="851"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">ReuseExecutor</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">BaseExecutor</span> </span>{
  <span class="hljs-keyword">private</span> <span class="hljs-keyword">final</span> Map&lt;String, Statement&gt; statementMap = <span class="hljs-keyword">new</span> HashMap&lt;&gt;();
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-title">ReuseExecutor</span><span class="hljs-params">(Configuration configuration, Transaction transaction)</span> </span>{
    <span class="hljs-keyword">super</span>(configuration, transaction);
  }
  <span class="hljs-comment">// 更新方法</span>
  <span class="hljs-meta">@Override</span>
  <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">int</span> <span class="hljs-title">doUpdate</span><span class="hljs-params">(MappedStatement ms, Object parameter)</span> <span class="hljs-keyword">throws</span> SQLException </span>{
    Configuration configuration = ms.getConfiguration();
    StatementHandler handler = configuration.newStatementHandler(<span class="hljs-keyword">this</span>, ms, parameter, RowBounds.DEFAULT, <span class="hljs-keyword">null</span>, <span class="hljs-keyword">null</span>);
    Statement stmt = prepareStatement(handler, ms.getStatementLog());
    <span class="hljs-keyword">return</span> handler.update(stmt);
  }
  <span class="hljs-comment">// 忽略其他代码...</span>
}
</code></pre>
<p data-nodeid="852">可以看出，ReuseExecutor 每次使用完 Statement 对象之后不会把它关闭掉。</p>
<h4 data-nodeid="853">7.装饰器模式</h4>
<p data-nodeid="854">装饰器模式允许向一个现有的对象添加新的功能，同时又不改变其结构，这种类型的设计模式属于结构型模式，它是作为现有类的一个包装。</p>
<p data-nodeid="855">装饰器模式在生活中很常见，比如装修房子，我们在不改变房子结构的同时，给房子添加了很多的点缀；比如安装了天然气报警器，增加了热水器等附加的功能都属于装饰器模式。</p>
<p data-nodeid="856">装饰器模式在 MyBatis 中的典型代表是 Cache。</p>
<p data-nodeid="857">Cache 除了有数据存储和缓存的基本功能外（由 PerpetualCache 永久缓存实现），还有其他附加的 Cache 类，比如先进先出的 FifoCache、最近最少使用的 LruCache、防止多线程并发访问的 SynchronizedCache 等众多附加功能的缓存类，Cache 所有实现子类如下图所示：</p>
<p data-nodeid="858"><img src="https://s0.lgstatic.com/i/image3/M01/06/4C/CgoCgV6f2yOAeRJoAACY-E2QwcM337.png" alt="面试4.png" data-nodeid="954"></p>
<h3 data-nodeid="859">小结</h3>
<p data-nodeid="860" class="">本课时我们重点讲了 MyBatis 源码中的几个主要设计模式，即工厂模式、建造者模式、单例模式、适配器模式、代理模式、模板方法模式等，希望本课时的内容能起到抛砖引玉的作用，对你理解设计模式和 MyBatis 提供一些帮助，如果想要阅读全部的 MyBatis 源码可以访问：<a href="https://github.com/mybatis/mybatis-3" data-nodeid="959">https://github.com/mybatis/mybatis-3</a> 。</p>

---

### 精选评论

##### **戈多：
> 这一节课讲的很生动形象，解释的也很清楚，一直在看！

##### **靖：
> 这篇文章刷新了我对设计模式和mybatis的认识，原来设计模式是这样用的，以前一直没有理解到位，感谢老师🙏🙏

##### *波：
> 这里的ErrorContext，使用private static修饰变量，目的是为了不让他在外部被被调用，哪怕是new出来的对象也不可以访问该变量，只能在内部进行调用，是吗？

##### *涛：
> 代理模式和装饰器模式好类似呀，都是全都继承相同接口，有什么具体的区别吗？谢谢老师回答

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 两种设计模式的侧重点不同一样，装饰模式主要是强调对类中代码的拓展，而代理模式则偏向于委托类的访问限制。

