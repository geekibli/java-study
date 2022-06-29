<p data-nodeid="13047">回顾一下 06 课时到 15 课时，我们分别了解缓冲、缓存、池化对象、大对象复用、并行计算、锁优化、NIO 等优化方法，它们对性能的提升往往是质的飞跃。</p>
<p data-nodeid="13048">但语言本身对性能也是有影响的，比如就有很多公司就因为语言的特性由 Java 切换到 Golang。对于 Java 语言来说，也有它的一套优化法则，这些细微的性能差异，经过多次调用和迭代，会产生越来越大的影响。</p>
<p data-nodeid="13049">本课时将集中讲解一些常用的代码优化法则，从而在编码中保持好的习惯，让代码保持最优状态。</p>
<h3 data-nodeid="13050">代码优化法则</h3>
<h4 data-nodeid="13051">1.使用局部变量可避免在堆上分配</h4>
<p data-nodeid="13052">由于堆资源是多线程共享的，是垃圾回收器工作的主要区域，过多的对象会造成 GC 压力。可以通过局部变量的方式，将变量在栈上分配。这种方式变量会随着方法执行的完毕而销毁，能够减轻 GC 的压力。</p>
<h4 data-nodeid="13053">2.减少变量的作用范围</h4>
<p data-nodeid="13054">注意变量的作用范围，尽量减少对象的创建。如下面的代码，变量 a 每次进入方法都会创建，可以将它移动到 if 语句内部。</p>
<pre class="lang-java" data-nodeid="17751"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">test1</span><span class="hljs-params">(String str)</span> </span>{
 &nbsp; &nbsp;<span class="hljs-keyword">final</span> <span class="hljs-keyword">int</span> a = <span class="hljs-number">100</span>;
 &nbsp; &nbsp;<span class="hljs-keyword">if</span> (!StringUtils.isEmpty(str)) {
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> b = a * a;
 &nbsp;  }
}
</code></pre>




<h4 data-nodeid="13056">3.访问静态变量直接使用类名</h4>
<p data-nodeid="13057">有的同学习惯使用对象访问静态变量，这种方式多了一步寻址操作，需要先找到变量对应的类，再找到类对应的变量，如下面的代码：</p>
<pre class="lang-java" data-nodeid="18376"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">StaticCall</span> </span>{
 &nbsp; &nbsp;<span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-keyword">int</span> A = <span class="hljs-number">1</span>;
​
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">void</span> <span class="hljs-title">test</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(<span class="hljs-keyword">this</span>.A);
 &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(StaticCall.A);
 &nbsp;  }
}
</code></pre>

<p data-nodeid="13059">对应的字节码为：</p>
<pre class="lang-dart" data-nodeid="22126"><code data-language="dart"><span class="hljs-keyword">void</span> test();
 &nbsp; &nbsp;descriptor: ()V
 &nbsp; &nbsp;flags:
 &nbsp; &nbsp;Code:
 &nbsp; &nbsp; &nbsp;stack=<span class="hljs-number">2</span>, locals=<span class="hljs-number">1</span>, args_size=<span class="hljs-number">1</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">0</span>: getstatic &nbsp; &nbsp; #<span class="hljs-number">2</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Field java/lang/System.out:Ljava/io/PrintStream;</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">3</span>: aload_0
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">4</span>: pop
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">5</span>: iconst_1
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">6</span>: invokevirtual #<span class="hljs-number">3</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Method java/io/PrintStream.println:(I)V</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">9</span>: getstatic &nbsp; &nbsp; #<span class="hljs-number">2</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Field java/lang/System.out:Ljava/io/PrintStream;</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">12</span>: iconst_1
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">13</span>: invokevirtual #<span class="hljs-number">3</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Method java/io/PrintStream.println:(I)V</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">16</span>: <span class="hljs-keyword">return</span>
 &nbsp; &nbsp; &nbsp;LineNumberTable:
 &nbsp; &nbsp; &nbsp; &nbsp;line <span class="hljs-number">5</span>: <span class="hljs-number">0</span>
 &nbsp; &nbsp; &nbsp; &nbsp;line <span class="hljs-number">6</span>: <span class="hljs-number">9</span>
 &nbsp; &nbsp; &nbsp; &nbsp;line <span class="hljs-number">7</span>: <span class="hljs-number">16</span>
</code></pre>






<p data-nodeid="13061">可以看到使用 this 的方式多了一个步骤。</p>
<h4 data-nodeid="13062">4.字符串拼接使用 StringBuilder</h4>
<p data-nodeid="13063">字符串拼接，使用 StringBuilder 或者 StringBuffer，不要使用 + 号。比如下面这段代码，在循环中拼接了字符串。</p>
<pre class="lang-java" data-nodeid="22751"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> String <span class="hljs-title">test</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp;String str = <span class="hljs-string">"-1"</span>;
 &nbsp; &nbsp;<span class="hljs-keyword">for</span> (<span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>; i &lt; <span class="hljs-number">10</span>; i++) {
 &nbsp; &nbsp; &nbsp; &nbsp;str += i;
 &nbsp;  }
 &nbsp; &nbsp;<span class="hljs-keyword">return</span> str;
}
</code></pre>

<p data-nodeid="13065">从下面对应的字节码内容可以看出，它在每个循环里都创建了一个 StringBuilder 对象。所以，我们在平常的编码中，显式地创建一次即可。</p>
<pre class="lang-dart" data-nodeid="27751"><code data-language="dart"> <span class="hljs-number">5</span>: iload_2
 <span class="hljs-number">6</span>: bipush &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">10</span>
 <span class="hljs-number">8</span>: if_icmpge &nbsp; &nbsp; <span class="hljs-number">36</span>
<span class="hljs-number">11</span>: <span class="hljs-keyword">new</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; #<span class="hljs-number">3</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// class java/lang/StringBuilder</span>
<span class="hljs-number">14</span>: dup
<span class="hljs-number">15</span>: invokespecial #<span class="hljs-number">4</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Method java/lang/StringBuilder."&lt;init&gt;":()V</span>
<span class="hljs-number">18</span>: aload_1
<span class="hljs-number">19</span>: invokevirtual #<span class="hljs-number">5</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;</span>
<span class="hljs-number">22</span>: iload_2
<span class="hljs-number">23</span>: invokevirtual #<span class="hljs-number">6</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;</span>
<span class="hljs-number">26</span>: invokevirtual #<span class="hljs-number">7</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Method java/lang/StringBuilder.toString:()Ljava/lang/String;</span>
<span class="hljs-number">29</span>: astore_1
<span class="hljs-number">30</span>: iinc &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">2</span>, <span class="hljs-number">1</span>
<span class="hljs-number">33</span>: goto &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">5</span>
</code></pre>








<h4 data-nodeid="13067">5.重写对象的 HashCode，不要简单地返回固定值</h4>
<p data-nodeid="13068">在代码 review 的时候，我发现有开发重写 HashCode 和 Equals 方法时，会把 HashCode 的值返回固定的 0，而这样做是不恰当的。</p>
<p data-nodeid="13069">当这些对象存入 HashMap 时，性能就会非常低，因为 HashMap 是通过 HashCode 定位到 Hash 槽，有冲突的时候，才会使用链表或者红黑树组织节点。固定地返回 0，相当于把 Hash 寻址功能给废除了。</p>
<h4 data-nodeid="13070">6.HashMap 等集合初始化的时候，指定初始值大小</h4>
<p data-nodeid="28376" class="">这个原则参见 <strong data-nodeid="28382">“10 | 案例分析：大对象复用的目标和注意点”</strong>，这样的对象有很多，比如 ArrayList，StringBuilder 等，通过指定初始值大小可减少扩容造成的性能损耗。</p>

<h4 data-nodeid="13072">7.遍历 Map 的时候，使用 EntrySet 方法</h4>
<p data-nodeid="13073">使用 EntrySet 方法，可以直接返回 set 对象，直接拿来用即可；而使用 KeySet 方法，获得的是key 的集合，需要再进行一次 get 操作，多了一个操作步骤。所以更推荐使用 EntrySet 方式遍历 Map。</p>
<h4 data-nodeid="13074">8.不要在多线程下使用同一个 Random</h4>
<p data-nodeid="13075">Random 类的 seed 会在并发访问的情况下发生竞争，造成性能降低，建议在多线程环境下使用 ThreadLocalRandom 类。</p>
<p data-nodeid="29009" class="">在 Linux 上，通过加入 JVM 配置 <strong data-nodeid="29015">-Djava.security.egd=file:/dev/./urandom</strong>，使用 urandom 随机生成器，在进行随机数获取时，速度会更快。</p>

<h4 data-nodeid="13077">9.自增推荐使用 LongAddr</h4>
<p data-nodeid="13078">自增运算可以通过 synchronized 和 volatile 的组合，或者也可以使用原子类（比如 AtomicLong）。</p>
<p data-nodeid="13079">后者的速度比前者要高一些，AtomicLong 使用 CAS 进行比较替换，在线程多的情况下会造成过多无效自旋，所以可以使用 LongAdder 替换 AtomicLong 进行进一步的性能提升。</p>
<h4 data-nodeid="13080">10.不要使用异常控制程序流程</h4>
<p data-nodeid="13081">异常，是用来了解并解决程序中遇到的各种不正常的情况，它的实现方式比较昂贵，比平常的条件判断语句效率要低很多。</p>
<p data-nodeid="13082">这是因为异常在字节码层面，需要生成一个如下所示的异常表（Exception table），多了很多判断步骤。</p>
<pre class="lang-java" data-nodeid="29644"><code data-language="java">Exception table:
 &nbsp; &nbsp;from &nbsp; &nbsp;to &nbsp;target type
 &nbsp; &nbsp;<span class="hljs-number">7</span> &nbsp; &nbsp;<span class="hljs-number">17</span> &nbsp; &nbsp;<span class="hljs-number">20</span> &nbsp; any
 &nbsp; &nbsp;<span class="hljs-number">20</span> &nbsp; &nbsp;<span class="hljs-number">23</span> &nbsp; &nbsp;<span class="hljs-number">20</span> &nbsp; any
</code></pre>

<p data-nodeid="13084">所以，尽量不要使用异常控制程序流程。</p>
<h4 data-nodeid="13085">11.不要在循环中使用 try catch</h4>
<p data-nodeid="13086">道理与上面类似，很多文章介绍，不要把异常处理放在循环里，而应该把它放在最外层，但实际测试情况表明这两种方式性能相差并不大。</p>
<p data-nodeid="13087">既然性能没什么差别，那么就推荐根据业务的需求进行编码。比如，循环遇到异常时，不允许中断，也就是允许在发生异常的时候能够继续运行下去，那么异常就只能在 for 循环里进行处理。</p>
<h4 data-nodeid="13088">12.不要捕捉 RuntimeException</h4>
<p data-nodeid="13089">Java 异常分为两种，一种是可以通过预检查机制避免的 RuntimeException；另外一种就是普通异常。</p>
<p data-nodeid="13090">其中，RuntimeException 不应该通过 catch 语句去捕捉，而应该使用编码手段进行规避。</p>
<p data-nodeid="13091">如下面的代码，list 可能会出现数组越界异常。是否越界是可以通过代码提前判断的，而不是等到发生异常时去捕捉。提前判断这种方式，代码会更优雅，效率也更高。</p>
<pre class="lang-java" data-nodeid="30273"><code data-language="java"><span class="hljs-comment">//BAD</span>
<span class="hljs-function"><span class="hljs-keyword">public</span> String <span class="hljs-title">test1</span><span class="hljs-params">(List&lt;String&gt; list, <span class="hljs-keyword">int</span> index)</span> </span>{
 &nbsp; &nbsp;<span class="hljs-keyword">try</span> {
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> list.get(index);
 &nbsp;  } <span class="hljs-keyword">catch</span> (IndexOutOfBoundsException ex) {
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> <span class="hljs-keyword">null</span>;
 &nbsp;  }
}
​
<span class="hljs-comment">//GOOD</span>
<span class="hljs-function"><span class="hljs-keyword">public</span> String <span class="hljs-title">test2</span><span class="hljs-params">(List&lt;String&gt; list, <span class="hljs-keyword">int</span> index)</span> </span>{
 &nbsp; &nbsp;<span class="hljs-keyword">if</span> (index &gt;= list.size() || index &lt; <span class="hljs-number">0</span>) {
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> <span class="hljs-keyword">null</span>;
 &nbsp;  }
 &nbsp; &nbsp;<span class="hljs-keyword">return</span> list.get(index);
}
</code></pre>

<h4 data-nodeid="13093">13.合理使用 PreparedStatement</h4>
<p data-nodeid="13094">PreparedStatement 使用预编译对 SQL 的执行进行提速，大多数数据库都会努力对这些能够复用的查询语句进行预编译优化，并能够将这些编译结果缓存起来。</p>
<p data-nodeid="13095">这样等到下次用到的时候，就可以很快进行执行，也就少了一步对 SQL 的解析动作。</p>
<p data-nodeid="13096">PreparedStatement 还能提高程序的安全性，能够有效防止 SQL 注入。</p>
<p data-nodeid="13097">但如果你的程序每次 SQL 都会变化，不得不手工拼接一些数据，那么 PreparedStatement 就失去了它的作用，反而使用普通的 Statement 速度会更快一些。</p>
<h4 data-nodeid="13098">14.日志打印的注意事项</h4>
<p data-nodeid="13099">我们在“06 | 案例分析：缓冲区如何让代码加速”中了解了 logback 的异步日志，日志打印还有一些其他要注意的事情。</p>
<p data-nodeid="13100">我们平常会使用 debug 输出一些调试信息，然后在线上关掉它。如下代码：</p>
<pre class="lang-java" data-nodeid="30902"><code data-language="java">logger.debug(<span class="hljs-string">"xjjdog:"</span>+ topic + <span class="hljs-string">"  is  awesome"</span>  );
</code></pre>

<p data-nodeid="31531">程序每次运行到这里，都会构造一个字符串，不管你是否把日志级别调试到 INFO 还是 WARN，这样效率就会很低。</p>
<p data-nodeid="31532">可以在每次打印之前都使用 isDebugEnabled 方法判断一下日志级别，代码如下：</p>

<pre class="lang-java" data-nodeid="32163"><code data-language="java"><span class="hljs-keyword">if</span>(logger.isDebugEnabled()) { 
 &nbsp; &nbsp;logger.debug(<span class="hljs-string">"xjjdog:"</span>+ topic + <span class="hljs-string">"  is  awesome"</span>  );
}
</code></pre>

<p data-nodeid="13104">使用占位符的方式，也可以达到相同的效果，就不用手动添加 isDebugEnabled 方法了，代码也优雅得多。</p>
<pre class="lang-java" data-nodeid="32792"><code data-language="java">logger.debug(<span class="hljs-string">"xjjdog:{}  is  awesome"</span>  ,topic);
</code></pre>

<p data-nodeid="13106">对于业务系统来说，日志对系统的性能影响非常大，不需要的日志，尽量不要打印，避免占用 I/O 资源。</p>
<h4 data-nodeid="13107">15.减少事务的作用范围</h4>
<p data-nodeid="34684" class="">如果的程序使用了事务，那一定要注意事务的作用范围，尽量以最快的速度完成事务操作。这是因为，事务的隔离性是使用锁实现的，可以类比使用 <strong data-nodeid="34690">“13 | 案例分析：多线程锁的优化”</strong> 中的多线程锁进行优化。</p>


<pre class="lang-java" data-nodeid="33421"><code data-language="java"><span class="hljs-meta">@Transactional</span> 
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">test</span><span class="hljs-params">(String id)</span></span>{
 &nbsp; &nbsp;String value = rpc.getValue(id); <span class="hljs-comment">//高耗时</span>
 &nbsp; &nbsp;testDao.update(sql,value);
}
</code></pre>

<p data-nodeid="13110">如上面的代码，由于 rpc 服务耗时高且不稳定，就应该把它移出到事务之外，改造如下：</p>
<pre class="lang-java" data-nodeid="35321"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">test</span><span class="hljs-params">(String id)</span></span>{
 &nbsp; &nbsp;String value = rpc.getValue(id); <span class="hljs-comment">//高耗时</span>
 &nbsp; &nbsp;testDao(value);
}
<span class="hljs-meta">@Transactional</span> 
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">testDao</span><span class="hljs-params">(String value)</span></span>{
 &nbsp; &nbsp;testDao.update(value);
}
</code></pre>

<p data-nodeid="13112"><strong data-nodeid="13262">这里有一点需要注意的地方，由于 SpringAOP 的原因，@Transactional 注解只能用到 public 方法上，如果用到 private 方法上，将会被忽略，这也是面试经常问的考点之一。</strong></p>
<h4 data-nodeid="13113">16.使用位移操作替代乘除法</h4>
<p data-nodeid="13114">计算机是使用二进制表示的，位移操作会极大地提高性能。</p>
<ul data-nodeid="36594">
<li data-nodeid="36595">
<p data-nodeid="36596">&lt;&lt; 左移相当于乘以 2；</p>
</li>
<li data-nodeid="36597">
<p data-nodeid="36598" class="">&gt;&gt; 右移相当于除以 2；</p>
</li>
<li data-nodeid="36599">
<p data-nodeid="36600">&gt;&gt;&gt; 无符号右移相当于除以 2，但它会忽略符号位，空位都以 0 补齐。</p>
</li>
</ul>

<pre class="lang-java" data-nodeid="37232"><code data-language="java"><span class="hljs-keyword">int</span> a = <span class="hljs-number">2</span>;
<span class="hljs-keyword">int</span> b = (a++) &lt;&lt; (++a) + (++a);
System.out.println(b);
</code></pre>



<p data-nodeid="13128">注意：位移操作的优先级非常低，所以上面的代码，输出是 1024。</p>
<h4 data-nodeid="13129">17.不要打印大集合或者使用大集合的 toString 方法</h4>
<p data-nodeid="13130">有的开发喜欢将集合作为字符串输出到日志文件中，这个习惯是非常不好的。</p>
<p data-nodeid="13131">拿 ArrayList 来说，它需要遍历所有的元素来迭代生成字符串。在集合中元素非常多的情况下，这不仅会占用大量的内存空间，执行效率也非常慢。我曾经就遇到过这种批量打印方式造成系统性能直线下降的实际案例。</p>
<p data-nodeid="13132">下面这段代码，就是 ArrayList 的 toString 方法。它需要生成一个迭代器，然后把所有的元素内容拼接成一个字符串，非常浪费空间。</p>
<pre class="lang-java" data-nodeid="37857"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> String <span class="hljs-title">toString</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp;Iterator&lt;E&gt; it = iterator();
 &nbsp; &nbsp;<span class="hljs-keyword">if</span> (! it.hasNext())
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> <span class="hljs-string">"[]"</span>;
​
 &nbsp; &nbsp;StringBuilder sb = <span class="hljs-keyword">new</span> StringBuilder();
 &nbsp; &nbsp;sb.append(<span class="hljs-string">'['</span>);
 &nbsp; &nbsp;<span class="hljs-keyword">for</span> (;;) {
 &nbsp; &nbsp; &nbsp; &nbsp;E e = it.next();
 &nbsp; &nbsp; &nbsp; &nbsp;sb.append(e == <span class="hljs-keyword">this</span> ? <span class="hljs-string">"(this Collection)"</span> : e);
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">if</span> (! it.hasNext())
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> sb.append(<span class="hljs-string">']'</span>).toString();
 &nbsp; &nbsp; &nbsp; &nbsp;sb.append(<span class="hljs-string">','</span>).append(<span class="hljs-string">' '</span>);
 &nbsp;  }
}
</code></pre>

<h4 data-nodeid="13134">18.程序中少用反射</h4>
<p data-nodeid="13135">反射的功能很强大，但它是通过解析字节码实现的，性能就不是很理想。</p>
<p data-nodeid="38482" class="">现实中有很多对反射的优化方法，比如把反射执行的过程（比如 Method）缓存起来，使用复用来加快反射速度。</p>

<p data-nodeid="13137">Java 7.0 之后，加入了新的包 java.lang.invoke，同时加入了新的 JVM 字节码指令 invokedynamic，用来支持从 JVM 层面，直接通过字符串对目标方法进行调用。</p>
<p data-nodeid="13138">如果你对性能有非常苛刻的要求，则使用 invoke 包下的 MethodHandle 对代码进行着重优化,但它的编程不如反射方便，在平常的编码中，反射依然是首选。</p>
<p data-nodeid="13139">下面是一个使用 MethodHandle 编写的代码实现类。它可以完成一些动态语言的特性，通过方法名称和传入的对象主体，进行不同的调用，而 Bike 和 Man 类，可以是没有任何关系的。</p>
<pre class="lang-java" data-nodeid="39108"><code data-language="java"><span class="hljs-keyword">import</span> java.lang.invoke.MethodHandle;
<span class="hljs-keyword">import</span> java.lang.invoke.MethodHandles;
<span class="hljs-keyword">import</span> java.lang.invoke.MethodType;
​
<span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">MethodHandleDemo</span> </span>{
 &nbsp; &nbsp;<span class="hljs-keyword">static</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Bike</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function">String <span class="hljs-title">sound</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> <span class="hljs-string">"ding ding"</span>;
 &nbsp; &nbsp; &nbsp;  }
 &nbsp;  }
​
 &nbsp; &nbsp;<span class="hljs-keyword">static</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Animal</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function">String <span class="hljs-title">sound</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> <span class="hljs-string">"wow wow"</span>;
 &nbsp; &nbsp; &nbsp;  }
 &nbsp;  }
​
​
 &nbsp; &nbsp;<span class="hljs-keyword">static</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Man</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Animal</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-meta">@Override</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-function">String <span class="hljs-title">sound</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> <span class="hljs-string">"hou hou"</span>;
 &nbsp; &nbsp; &nbsp;  }
 &nbsp;  }
​
​
 &nbsp; &nbsp;<span class="hljs-function">String <span class="hljs-title">sound</span><span class="hljs-params">(Object o)</span> <span class="hljs-keyword">throws</span> Throwable </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;MethodHandles.Lookup lookup = MethodHandles.lookup();
 &nbsp; &nbsp; &nbsp; &nbsp;MethodType methodType = MethodType.methodType(String.class);
 &nbsp; &nbsp; &nbsp; &nbsp;MethodHandle methodHandle = lookup.findVirtual(o.getClass(), "sound", methodType);
​
 &nbsp; &nbsp; &nbsp; &nbsp;String obj = (String) methodHandle.invoke(o);
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> obj;
 &nbsp;  }
​
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> <span class="hljs-keyword">throws</span> Throwable </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;String str = <span class="hljs-keyword">new</span> MethodHandleDemo().sound(<span class="hljs-keyword">new</span> Bike());
 &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(str);
 &nbsp; &nbsp; &nbsp; &nbsp;str = <span class="hljs-keyword">new</span> MethodHandleDemo().sound(<span class="hljs-keyword">new</span> Animal());
 &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(str);
 &nbsp; &nbsp; &nbsp; &nbsp;str = <span class="hljs-keyword">new</span> MethodHandleDemo().sound(<span class="hljs-keyword">new</span> Man());
 &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(str);
 &nbsp;  }
}
</code></pre>

<h4 data-nodeid="13141">19.正则表达式可以预先编译，加快速度</h4>
<p data-nodeid="13142">Java 的正则表达式需要先编译再使用。</p>
<p data-nodeid="13143">典型代码如下：</p>
<pre class="lang-dart" data-nodeid="42858"><code data-language="dart"><span class="hljs-built_in">Pattern</span> pattern = <span class="hljs-built_in">Pattern</span>.compile({pattern});
Matcher pattern = pattern.matcher({content});
</code></pre>






<p data-nodeid="13145">Pattern 编译非常耗时，它的 Matcher 方法是线程安全的，每次调用方法这个方法都会生成一个新的 Matcher 对象。所以，一般 Pattern 初始化一次即可，可以作为类的静态成员变量。</p>
<h3 data-nodeid="13146">案例分析</h3>
<h4 data-nodeid="13147">案例 1：正则表达式和状态机</h4>
<p data-nodeid="13148">正则表达式的执行效率是非常慢的，尤其是贪婪模式。</p>
<p data-nodeid="13149">下面介绍一个我在实际工作中对正则的一个优化，使用状态机完成字符串匹配。</p>
<p data-nodeid="13150">考虑到下面的一个 SQL 语句，它的语法类似于 NamedParameterJdbcTemplate，但我们对它做了增强。SQL 接收两个参数：smallId 和 firstName，当 firstName 为空的时候，处在 ##{} 之间的语句将被抹去。</p>
<pre class="lang-dart" data-nodeid="46608"><code data-language="dart">select * from USERS
where id&gt;:smallId
##{
 and FIRST_NAME like concat(<span class="hljs-string">'%'</span>,:firstName,<span class="hljs-string">'%'</span>) }
</code></pre>






<p data-nodeid="13152">可以看到，使用正则表达式可以很容易地实现这个功能。</p>
<pre class="lang-dart" data-nodeid="50358"><code data-language="dart">#\{(.*?:([a-zA-Z0<span class="hljs-number">-9</span>_]+).*?)\}
</code></pre>






<p data-nodeid="50983">通过定义上面这样一个正则匹配，使用 Pattern 的 group 功能便能提取到相应的字符串。我们把匹配到的字符串保存下来，最后使用 replace 函数，将它替换成空字符串即可。</p>
<p data-nodeid="50984">结果在实际使用的时候，发现正则的解析速度特别慢，尤其是在 SQL 非常大的时候，这种情况下，可以使用状态机去优化。我这里选用的是 ragel，你也可以使用类似 javacc 或者 antlr 之类的工具。它通过语法解析和简单的正则表达式，最终可以生成 Java 语法的代码。</p>

<p data-nodeid="13155">生成的代码一般是不可读的，我们只关注定义文件即可。如下定义文件代码所示，通过定义一批描述符和处理程序，使用一些中间数据结构对结果进行缓存，只需要对 SQL 扫描一遍，即可获取相应的结果。</p>
<pre class="lang-java" data-nodeid="51611"><code data-language="java">pairStart = <span class="hljs-string">'#{'</span>;
pairEnd = <span class="hljs-string">'}'</span>;
namedQueryStringFull = ( <span class="hljs-string">':'</span>alnum+)
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;&gt;buffer
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;%namedQueryStringFull
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  ;
pairBlock =
 &nbsp; &nbsp; &nbsp;  (pairStart
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;any*
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;namedQueryStringFull
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;any*
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;pairEnd)
 &nbsp; &nbsp; &nbsp; &nbsp;&gt;pairBlockBegin %pairBlockEnd
 &nbsp; &nbsp; &nbsp;  ;
main := any* pairBlock any*;
</code></pre>

<p data-nodeid="13157">把文件定义好之后，即可通过 ragel 命令生成 Java 语法的最终文件。</p>
<pre class="lang-yaml" data-nodeid="57861"><code data-language="yaml"><span class="hljs-string">ragel</span> <span class="hljs-string">-G2</span> <span class="hljs-string">-J</span> <span class="hljs-string">-o</span> <span class="hljs-string">P.java</span> <span class="hljs-string">P.rl</span>
</code></pre>










<p data-nodeid="59111">完整的代码有点复杂，我已经放到了<a href="https://gitee.com/xjjdog/tuning-lagou-res/tree/master/tuning-016/java/src/main/java/com/github/xjjdog/java/dsl" data-nodeid="59116">仓库</a>中，你可以实际分析一下。</p>
<p data-nodeid="59112">我们来看一下它的性能。从测试结果可以看到，ragel 模式的性能是 regex 模式的 3 倍还多，SQL 越长，效果越明显。</p>

<pre class="lang-java" data-nodeid="58486"><code data-language="java">Benchmark &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Mode &nbsp;Cnt &nbsp; &nbsp;Score &nbsp; &nbsp; Error &nbsp; Units
RegexVsRagelBenchmark.ragel &nbsp;thrpt &nbsp; <span class="hljs-number">10</span> &nbsp;<span class="hljs-number">691.224</span> ± <span class="hljs-number">446.217</span> &nbsp;ops/ms
RegexVsRagelBenchmark.regex &nbsp;thrpt &nbsp; <span class="hljs-number">10</span> &nbsp;<span class="hljs-number">201.322</span> ± &nbsp;<span class="hljs-number">47.056</span> &nbsp;ops/ms
</code></pre>

<h4 data-nodeid="13161">案例 2：HikariCP 的字节码修改</h4>
<p data-nodeid="60373" class="">在 <strong data-nodeid="60379">“09 | 案例分析：池化对象的应用场景”</strong> 中，我们提到了 HikariCP 对字节码的修改，这个职责是由 JavassistProxyFactory 类来管理的。Javassist 是一个字节码类库，HikariCP 就是用它对字节码进行修改。</p>


<p data-nodeid="61624">如下图所示，这是工厂类的主要方法。</p>
<p data-nodeid="61625" class=""><img src="https://s0.lgstatic.com/i/image/M00/49/5B/CgqCHl9PEBqAUmucAABlRi1dKhM359.jpg" alt="15970255895249.jpg" data-nodeid="61629"></p>


<p data-nodeid="13165">它通过 generateProxyClass 生成代理类，主要是针对 Connection、Statement、ResultSet、DatabaseMetaData 等 jdbc 的核心接口。</p>
<p data-nodeid="13166">右键运行这个类，可以看到代码生成了一堆 Class 文件。</p>
<pre class="lang-dart" data-nodeid="66645"><code data-language="dart">Generating com.zaxxer.hikari.pool.HikariProxyConnection
Generating com.zaxxer.hikari.pool.HikariProxyStatement
Generating com.zaxxer.hikari.pool.HikariProxyResultSet
Generating com.zaxxer.hikari.pool.HikariProxyDatabaseMetaData
Generating com.zaxxer.hikari.pool.HikariProxyPreparedStatement
Generating com.zaxxer.hikari.pool.HikariProxyCallableStatement
Generating method bodies <span class="hljs-keyword">for</span> com.zaxxer.hikari.proxy.ProxyFactory
</code></pre>








<p data-nodeid="67892">对于这一部分的代码组织，使用了设计模式中的委托模式。我们发现 HikariCP 源码中的代理类，比如 ProxyConnection，都是 abstract 的，它的具体实例就是使用 javassist 生成的 class 文件。反编译这些生成的 class 文件，可以看到它实际上是通过调用父类中的委托对象进行处理的。</p>
<p data-nodeid="67893" class=""><img src="https://s0.lgstatic.com/i/image/M00/49/5B/CgqCHl9PECuAFz5zAAB0EwpHKE0091.jpg" alt="15970285875030.jpg" data-nodeid="67897"></p>


<p data-nodeid="13169">这么做有两个好处：</p>
<ul data-nodeid="13170">
<li data-nodeid="13171">
<p data-nodeid="13172">第一，在代码中只需要实现需要修改的 JDBC 接口方法，其他的交给代理类自动生成的代码，极大地减少了编码数量。</p>
</li>
<li data-nodeid="13173">
<p data-nodeid="13174">第二，出现问题时，可以通过 checkException 函数对错误进行统一处理。</p>
</li>
</ul>
<p data-nodeid="13175">另外，我们注意到 ProxyFactory 类中的方法，都是静态方法，而不是通过单例实现的。为什么这么做呢？这就涉及 JVM 底层的两个字节码指令：invokestatic 和 invokevirtual。</p>
<p data-nodeid="13176">下面是两种不同类型调用的字节码。</p>
<ul data-nodeid="13177">
<li data-nodeid="13178">
<p data-nodeid="13179"><strong data-nodeid="13328">invokevirtual</strong></p>
</li>
</ul>
<pre class="lang-dart" data-nodeid="72913"><code data-language="dart">public <span class="hljs-keyword">final</span> java.sql.PreparedStatement prepareStatement(java.lang.<span class="hljs-built_in">String</span>, java.lang.<span class="hljs-built_in">String</span>[]) throws java.sql.SQLException;
 &nbsp; &nbsp;flags: ACC_PRIVATE, ACC_FINAL
 &nbsp; &nbsp;Code:
 &nbsp; &nbsp; &nbsp;stack=<span class="hljs-number">5</span>, locals=<span class="hljs-number">3</span>, args_size=<span class="hljs-number">3</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">0</span>: getstatic &nbsp; &nbsp; #<span class="hljs-number">59</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-comment">// Field PROXY_FACTORY:Lcom/zaxxer/hikari/proxy/ProxyFactory;</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">3</span>: aload_0
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">4</span>: aload_0
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">5</span>: getfield &nbsp; &nbsp; &nbsp;#<span class="hljs-number">3</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Field delegate:Ljava/sql/Connection;</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">8</span>: aload_1
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">9</span>: aload_2
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">10</span>: invokeinterface #<span class="hljs-number">74</span>, &nbsp;<span class="hljs-number">3</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-comment">// InterfaceMethod java/sql/Connection.prepareStatement:(Ljava/lang/String;[Ljava/lang/String;)Ljava/sql/PreparedStatement;</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">15</span>: invokevirtual #<span class="hljs-number">69</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-comment">// Method com/zaxxer/hikari/proxy/ProxyFactory.getProxyPreparedStatement:(Lcom/zaxxer/hikari/proxy/ConnectionProxy;Ljava/sql/PreparedStatement;)Ljava/sql/PreparedStatement;</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">18</span>: <span class="hljs-keyword">return</span>
</code></pre>








<ul data-nodeid="13181">
<li data-nodeid="13182">
<p data-nodeid="13183"><strong data-nodeid="13332">invokestatic</strong></p>
</li>
</ul>
<pre class="lang-dart" data-nodeid="77929"><code data-language="dart">private <span class="hljs-keyword">final</span> java.sql.PreparedStatement prepareStatement(java.lang.<span class="hljs-built_in">String</span>, java.lang.<span class="hljs-built_in">String</span>[]) throws java.sql.SQLException;
 &nbsp; &nbsp;flags: ACC_PRIVATE, ACC_FINAL
 &nbsp; &nbsp;Code:
 &nbsp; &nbsp; &nbsp;stack=<span class="hljs-number">4</span>, locals=<span class="hljs-number">3</span>, args_size=<span class="hljs-number">3</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">0</span>: aload_0
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">1</span>: aload_0
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">2</span>: getfield &nbsp; &nbsp; &nbsp;#<span class="hljs-number">3</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Field delegate:Ljava/sql/Connection;</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">5</span>: aload_1
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">6</span>: aload_2
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">7</span>: invokeinterface #<span class="hljs-number">72</span>, &nbsp;<span class="hljs-number">3</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-comment">// InterfaceMethod java/sql/Connection.prepareStatement:(Ljava/lang/String;[Ljava/lang/String;)Ljava/sql/PreparedStatement;</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">12</span>: invokestatic &nbsp;#<span class="hljs-number">67</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-comment">// Method com/zaxxer/hikari/proxy/ProxyFactory.getProxyPreparedStatement:(Lcom/zaxxer/hikari/proxy/ConnectionProxy;Ljava/sql/PreparedStatement;)Ljava/sql/PreparedStatement;</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">15</span>: areturn
</code></pre>








<p data-nodeid="78556">大多数普通方法调用，使用的是<strong data-nodeid="78563">invokevirtual</strong>指令，属于虚方法调用。</p>
<p data-nodeid="78557">很多时候，JVM 需要根据调用者的动态类型，来确定调用的目标方法，这就是动态绑定的过程；相对比，<strong data-nodeid="78569">invokestatic</strong>指令，就属于静态绑定过程，能够直接识别目标方法，效率会高那么一点点。</p>

<p data-nodeid="13186">虽然 HikariCP 的这些优化有点吹毛求疵，但我们能够从中看到 HikariCP 这些追求性能极致的编码技巧。</p>
<h3 data-nodeid="13187">小结</h3>
<p data-nodeid="13188">此外，学习 Java 规范，你还可以细读<a href="https://github.com/alibaba/p3c/blob/master/Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C%EF%BC%88%E5%B5%A9%E5%B1%B1%E7%89%88%EF%BC%89.pdf" data-nodeid="13351">《阿里巴巴 Java 开发规范》</a>，里面也有很多有意义的建议。</p>
<p data-nodeid="13189">其实语言层面的性能优化，都是在各个资源之间的权衡（比如开发时间、代码复杂度、扩展性等）。这些法则也不是一成不变的教条，这就要求我们在编码中选择合适的工具，根据实际的工作场景进行灵活变动。</p>
<p data-nodeid="14617" class="">接下来，我们将进入“模块四：JVM 优化”，下一课时我将讲解 <strong data-nodeid="14623">“17 | 高级进阶：JVM 如何完成垃圾回收？”</strong> ，带你向高级进阶。</p>

---

### 精选评论

##### *众：
> 老师讲的很好

