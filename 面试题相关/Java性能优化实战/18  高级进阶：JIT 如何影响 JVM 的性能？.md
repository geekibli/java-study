<p data-nodeid="66335" class="">我们在上一课时，我们了解到 Java 虚拟机栈，其实是一个双层的栈，如下图所示，第一层就是针对 method 的栈帧，第二层是针对字节码指令的操作数栈。</p>
<p data-nodeid="66336"><img src="https://s0.lgstatic.com/i/image/M00/4C/B4/CgqCHl9YevCARjawAAC9Bvf_IoE321.png" alt="Drawing 0.png" data-nodeid="66425"></p>
<div data-nodeid="66337"><p style="text-align:center">Java 虚拟机栈图</p></div>
<p data-nodeid="66338">栈帧的创建是需要耗费资源的，尤其是对于 Java 中常见的 getter、setter 方法来说，这些代码通常只有一行，每次都创建栈帧的话就太浪费了。</p>
<p data-nodeid="66339">另外，Java 虚拟机栈对代码的执行，采用的是字节码解释的方式，考虑到下面这段代码，变量 a 声明之后，就再也不被使用，要是按照字节码指令解释执行的话，就要做很多无用功。</p>
<pre class="lang-java" data-nodeid="66340"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">A</span></span>{
 &nbsp; &nbsp;<span class="hljs-keyword">int</span> attr = <span class="hljs-number">0</span>;
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">test</span><span class="hljs-params">()</span></span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> a = attr;
 &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(<span class="hljs-string">"ok"</span>);
 &nbsp;  }
}
</code></pre>
<p data-nodeid="66341">下面是这段代码的字节码指令，我们能够看到 aload_0，getfield ，istore_1 这三个无用的字节码指令操作。</p>
<pre class="lang-dart" data-nodeid="66342"><code data-language="dart"> public <span class="hljs-keyword">void</span> test();
 &nbsp; &nbsp;descriptor: ()V
 &nbsp; &nbsp;flags: ACC_PUBLIC
 &nbsp; &nbsp;Code:
 &nbsp; &nbsp; &nbsp;stack=<span class="hljs-number">2</span>, locals=<span class="hljs-number">2</span>, args_size=<span class="hljs-number">1</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">0</span>: aload_0
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">1</span>: getfield &nbsp; &nbsp; &nbsp;#<span class="hljs-number">2</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Field attr:I</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">4</span>: istore_1
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">5</span>: getstatic &nbsp; &nbsp; #<span class="hljs-number">3</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Field java/lang/System.out:Ljava/io/PrintStream;</span>
 &nbsp; &nbsp; &nbsp; &nbsp; <span class="hljs-number">8</span>: ldc &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; #<span class="hljs-number">4</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// String ok</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">10</span>: invokevirtual #<span class="hljs-number">5</span> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// Method java/io/PrintStream.println:(Ljava/lang/String;)V</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">13</span>: <span class="hljs-keyword">return</span>
 &nbsp; &nbsp; &nbsp;LineNumberTable:
 &nbsp; &nbsp; &nbsp; &nbsp;line <span class="hljs-number">4</span>: <span class="hljs-number">0</span>
 &nbsp; &nbsp; &nbsp; &nbsp;line <span class="hljs-number">5</span>: <span class="hljs-number">5</span>
 &nbsp; &nbsp; &nbsp; &nbsp;line <span class="hljs-number">6</span>: <span class="hljs-number">13</span>
</code></pre>
<p data-nodeid="66343">另外，我们了解到垃圾回收器回收的目标区域主要是堆，堆上创建的对象越多，GC 的压力就越大。要是能把一些变量，直接在栈上分配，那 GC 的压力就会小一些。</p>
<p data-nodeid="66344">其实，我们说的这几个优化的可能性，JVM 已经通过 JIT 编译器（Just In Time Compiler）去做了，JIT 最主要的目标是把解释执行变成编译执行。</p>
<p data-nodeid="66345">为了提高热点代码的执行效率，在运行时，虚拟机将会把这些代码编译成与本地平台相关的机器码，并进行各种层次的优化，这就是 JIT 编译器的功能。</p>
<p data-nodeid="66346"><img src="https://s0.lgstatic.com/i/image/M00/4C/B4/CgqCHl9YeyCAWROAAABdUyAOP5E893.png" alt="Drawing 2.png" data-nodeid="66438"></p>
<p data-nodeid="66347">如上图，JVM 会将调用次数很高，或者在 for 循环里频繁被使用的代码，编译成机器码，然后缓存在 CodeCache 区域里，下次调用相同方法的时候，就可以直接使用。</p>
<p data-nodeid="66348">那 JIT 编译都有哪些手段呢？接下来我们详细介绍。</p>
<h3 data-nodeid="66349">方法内联</h3>
<p data-nodeid="66350">在 <strong data-nodeid="66449">“05 | 工具实践：基准测试 JMH，精确测量方法性能”</strong> 提到 JMH 的时候，我们就了解到 CompilerControl 注解可以控制 JIT 编译器的一些行为。</p>
<p data-nodeid="66351">其中，有一个模式叫作<strong data-nodeid="66455">inline</strong>，就是内联的意思，它会把一些短小的方法体，直接纳入目标方法的作用范围之内，就像是直接在代码块中追加代码。这样，就少了一次方法调用，执行速度就能够得到提升，这就是方法内联的概念。</p>
<p data-nodeid="66352">可以使用 -XX:-Inline 参数来禁用方法内联，如果想要更细粒度的控制，可以使用 CompileCommand 参数，例如：</p>
<pre class="lang-js" data-nodeid="66353"><code data-language="js">-XX:CompileCommand=exclude,java/lang/<span class="hljs-built_in">String</span>.indexOf
</code></pre>
<p data-nodeid="66354">JMH 就是使用这个参数来实现的自定义编译特性。<br>
在 JDK 的源码里，也有很多被 <strong data-nodeid="66470">@ForceInline</strong>注解的方法，这些方法，会在执行的时候被强制进行内联；而被**@DontInline**注解的方法，则始终不会被内联。</p>
<p data-nodeid="66355">我们从 <strong data-nodeid="66478">“05 | 工具实践：基准测试 JMH，精确测量方法性能”</strong> 获取第 16 个代码示例，来看一下 JIT 这些优化的效果，主要代码块如下：</p>
<pre class="lang-java" data-nodeid="66356"><code data-language="java"><span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">target_blank</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp;<span class="hljs-comment">// this method was intentionally left blank</span>
}

<span class="hljs-meta">@CompilerControl(CompilerControl.Mode.DONT_INLINE)</span>
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">target_dontInline</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp;<span class="hljs-comment">// this method was intentionally left blank</span>
}

<span class="hljs-meta">@CompilerControl(CompilerControl.Mode.INLINE)</span>
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">target_inline</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp;<span class="hljs-comment">// this method was intentionally left blank</span>
}

<span class="hljs-meta">@CompilerControl(CompilerControl.Mode.EXCLUDE)</span>
<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">target_exclude</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp;<span class="hljs-comment">// this method was intentionally left blank</span>
}
</code></pre>
<p data-nodeid="66357">执行结果如下，可以看到不使用 JIT 编译和使用了 JIT 编译的性能差距达到了 100 多倍，使用了内联比不使用内联，速度快了 5 倍。</p>
<pre class="lang-java" data-nodeid="66358"><code data-language="java">Benchmark &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Mode &nbsp;Cnt &nbsp; Score &nbsp; Error &nbsp;Units
JMHSample_16_CompilerControl.baseline &nbsp; &nbsp;avgt &nbsp; &nbsp;<span class="hljs-number">3</span> &nbsp; <span class="hljs-number">0.485</span> ± <span class="hljs-number">1.492</span> &nbsp;ns/op
JMHSample_16_CompilerControl.blank &nbsp; &nbsp; &nbsp; avgt &nbsp; &nbsp;<span class="hljs-number">3</span> &nbsp; <span class="hljs-number">0.483</span> ± <span class="hljs-number">1.518</span> &nbsp;ns/op
JMHSample_16_CompilerControl.dontinline &nbsp;avgt &nbsp; &nbsp;<span class="hljs-number">3</span> &nbsp; <span class="hljs-number">1.934</span> ± <span class="hljs-number">3.112</span> &nbsp;ns/op
JMHSample_16_CompilerControl.exclude &nbsp; &nbsp; avgt &nbsp; &nbsp;<span class="hljs-number">3</span> &nbsp;<span class="hljs-number">57.603</span> ± <span class="hljs-number">4.435</span> &nbsp;ns/op
JMHSample_16_CompilerControl.inline &nbsp; &nbsp; &nbsp;avgt &nbsp; &nbsp;<span class="hljs-number">3</span> &nbsp; <span class="hljs-number">0.483</span> ± <span class="hljs-number">1.520</span> &nbsp;ns/op
</code></pre>
<p data-nodeid="66359"><img src="https://s0.lgstatic.com/i/image/M00/4C/B4/CgqCHl9Ye0KASdaPAAFd8UGlCRY151.png" alt="Drawing 4.png" data-nodeid="66482"></p>
<p data-nodeid="66360">JIT 编译之后的二进制代码，是放在 Code Cache 区域里的。这个区域的大小是固定的，而且一旦启动无法扩容。如果 Code Cache 满了，JVM 并不会报错，但会停止编译。所以编译执行就会退化为解释执行，性能就会降低。不仅如此，JIT 编译器会一直尝试去优化你的代码，造成 CPU 占用上升。</p>
<p data-nodeid="66361">通过参数 -XX:ReservedCodeCacheSize 可以指定 Code Cache 区域的大小，如果你通过监控发现空间达到了上限，就要适当的增加它的大小。</p>
<h3 data-nodeid="66362">编译层次</h3>
<p data-nodeid="66363">HotSpot 虚拟机包含多个即时编译器，有 C1，C2 和 Graal，JDK8 以后采用的是分层编译的模式。使用 jstack 命令获得的线程信息，经常能看到它们的身影。</p>
<pre class="lang-dart" data-nodeid="66364"><code data-language="dart"><span class="hljs-string">"C2 CompilerThread0"</span> #<span class="hljs-number">6</span> daemon prio=<span class="hljs-number">9</span> os_prio=<span class="hljs-number">31</span> cpu=<span class="hljs-number">830.41</span>ms elapsed=<span class="hljs-number">4252.14</span>s tid=<span class="hljs-number">0x00007ffaed023000</span> nid=<span class="hljs-number">0x5a03</span> waiting <span class="hljs-keyword">on</span> condition  [<span class="hljs-number">0x0000000000000000</span>]
 &nbsp; java.lang.Thread.State: RUNNABLE
 &nbsp; No compile task

<span class="hljs-string">"C1 CompilerThread0"</span> #<span class="hljs-number">8</span> daemon prio=<span class="hljs-number">9</span> os_prio=<span class="hljs-number">31</span> cpu=<span class="hljs-number">549.91</span>ms elapsed=<span class="hljs-number">4252.14</span>s tid=<span class="hljs-number">0x00007ffaed831800</span> nid=<span class="hljs-number">0x5c03</span> waiting <span class="hljs-keyword">on</span> condition  [<span class="hljs-number">0x0000000000000000</span>]
 &nbsp; java.lang.Thread.State: RUNNABLE
 &nbsp; No compile task
</code></pre>
<p data-nodeid="66365">使用额外线程进行即时编译，可以不用阻塞解释执行的逻辑。JIT 通常会在触发之后就在后台运行，编译完成之后就将相应的字节码替换为编译后的代码。<br>
JIT 编译方式有两种：一种是编译方法，另一种是编译循环。</p>
<p data-nodeid="66366">分层编译将 Java 虚拟机的执行状态分为了五个层次。</p>
<ul data-nodeid="66367">
<li data-nodeid="66368">
<p data-nodeid="66369">字节码的解释执行;</p>
</li>
<li data-nodeid="66370">
<p data-nodeid="66371">执行不带 profiling 的 C1 代码;</p>
</li>
<li data-nodeid="66372">
<p data-nodeid="66373">执行仅带方法调用次数以及循环执行次数 profiling 的 C1 代码;</p>
</li>
<li data-nodeid="66374">
<p data-nodeid="66375">执行带所有 profiling 的 C1 代码;</p>
</li>
<li data-nodeid="66376">
<p data-nodeid="66377">执行 C2 代码。</p>
</li>
</ul>
<p data-nodeid="66378">其中，Profiling 指的是运行时的程序的执行状态数据，比如循环调用的次数、方法调用的次数、分支跳转次数、类型转换次数等。比如 JDK 中的 hprof 工具，就是一种 profiler，说白了就是一些中间的统计数据。</p>
<p data-nodeid="66379">在不启用分层编译的情况下，当方法的调用次数和循环回边的次数的总和，超过由参数 -XX:CompileThreshold 指定的阈值时，便会触发即时编译；但当启用分层编译时，这个参数将会失效，会采用一套动态调整进行调整。</p>
<h3 data-nodeid="66380">逃逸分析</h3>
<p data-nodeid="66381"><strong data-nodeid="66502">下面着重讲解一下逃逸分析，这个知识点在面试的时候经常会被问到。</strong></p>
<p data-nodeid="66382">我们先回顾一下上一课时留下的问题：我们常说的对象，除了基本数据类型，一定是在堆上分配的吗？</p>
<p data-nodeid="66383">答案是否定的，通过逃逸分析，JVM 能够分析出一个新的对象的使用范围，从而决定是否要将这个对象分配到堆上。逃逸分析现在是 JVM 的默认行为，可以通过参数 -XX:-DoEscapeAnalysis 关掉它。</p>
<p data-nodeid="66384">那什么样的对象算是逃逸的呢？可以看一下下面的两种典型情况。</p>
<p data-nodeid="66385">如代码所示，对象被赋值给成员变量或者静态变量，可能被外部使用，变量就发生了逃逸。</p>
<pre class="lang-java" data-nodeid="66386"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">EscapeAttr</span> </span>{
 &nbsp; &nbsp;Object attr;
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">test</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;attr = <span class="hljs-keyword">new</span> Object();
 &nbsp;  }
}
</code></pre>
<p data-nodeid="66387">再看下面这段代码，对象通过 return 语句返回。由于程序并不能确定这个对象后续会不会被使用，外部的线程能够访问到这个结果，对象也发生了逃逸。</p>
<pre class="lang-java" data-nodeid="66388"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">EscapeReturn</span> </span>{
 &nbsp; &nbsp;Object attr;
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> Object <span class="hljs-title">test</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;Object obj = <span class="hljs-keyword">new</span> Object();
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> obj;
 &nbsp;  }
}
</code></pre>
<p data-nodeid="66389">那逃逸分析有什么好处呢？<br>
<strong data-nodeid="66515">1. 栈上分配</strong></p>
<p data-nodeid="66390">如果一个对象在子程序中被分配，指向该对象的指针永远不会逃逸，对象有可能会被优化为栈分配。栈分配可以快速地在栈帧上创建和销毁对象，不用再分配到堆空间，可以有效地减少 GC 的压力。</p>
<p data-nodeid="66391"><strong data-nodeid="66522">2. 分离对象或标量替换</strong></p>
<p data-nodeid="66392">但对象结构通常都比较复杂，如何将对象保存在栈上呢？</p>
<p data-nodeid="66393">JIT 可以将对象打散，全部替换为一个个小的局部变量，这个打散的过程，就叫作标量替换（标量就是不能被进一步分割的变量，比如 int、long 等基本类型）。也就是说，标量替换后的对象，全部变成了局部变量，可以方便地进行栈上分配，而无须改动其他的代码。</p>
<p data-nodeid="66394">从上面的描述我们可以看到，并不是所有的对象或者数组，都会在堆上分配。由于JIT的存在，如果发现某些对象没有逃逸出方法，那么就有可能被优化成栈分配。</p>
<p data-nodeid="66395"><strong data-nodeid="66529">3.同步消除</strong></p>
<p data-nodeid="66396">如果一个对象被发现只能从一个线程被访问到，那么对于这个对象的操作可以不考虑同步。</p>
<p data-nodeid="66397">注意这是针对 synchronized 来说的，JUC 中的 Lock 并不能被消除。</p>
<p data-nodeid="66398">要开启同步消除，需要加上 -XX:+EliminateLocks 参数。由于这个参数依赖逃逸分析，所以同时要打开 -XX:+DoEscapeAnalysis 选项。</p>
<p data-nodeid="66399">比如下面这段代码，JIT 判断对象锁只能被一个线程访问，就可以去掉这个同步的影响。</p>
<pre class="lang-java" data-nodeid="66400"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">SyncEliminate</span> </span>{
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">test</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">synchronized</span> (<span class="hljs-keyword">new</span> Object()) {
 &nbsp; &nbsp; &nbsp;  }
 &nbsp;  }
}
</code></pre>
<p data-nodeid="66401">仓库中也有一个 StringBuffer 和 StringBuilder 的 JMH 测试对比，可以看到在开启了锁消除的情况下，它们的效率相差并不大。</p>
<pre class="lang-java" data-nodeid="66402"><code data-language="java">Benchmark &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  Mode  Cnt &nbsp; &nbsp; &nbsp; Score &nbsp; &nbsp; &nbsp; Error &nbsp; Units
BuilderVsBufferBenchmark.buffer &nbsp; thrpt &nbsp; <span class="hljs-number">10</span> &nbsp; <span class="hljs-number">90085.927</span> ± <span class="hljs-number">95174.289</span>  ops/ms
BuilderVsBufferBenchmark.builder  thrpt &nbsp; <span class="hljs-number">10</span> &nbsp;<span class="hljs-number">103280.200</span> ± <span class="hljs-number">76172.538</span>  ops/ms
</code></pre>
<h3 data-nodeid="66403">JITWatch</h3>
<p data-nodeid="66404">可以使用 jitwatch 工具来观测 JIT 的一些行为。</p>
<pre class="lang-java" data-nodeid="66405"><code data-language="java">https:<span class="hljs-comment">//github.com/AdoptOpenJDK/jitwatch</span>
</code></pre>
<p data-nodeid="66406">在代码的启动参数里加入 LogCompilation 等参数开启记录，将生成一个 jitdemo.log 文件。</p>
<pre class="lang-yaml" data-nodeid="66407"><code data-language="yaml"> <span class="hljs-string">-XX:+UnlockDiagnosticVMOptions</span> <span class="hljs-string">-XX:+TraceClassLoading</span> &nbsp;<span class="hljs-string">-XX:+PrintAssembly</span> <span class="hljs-string">-XX:+LogCompilation</span> <span class="hljs-string">-XX:LogFile=jitdemo.log</span>
</code></pre>
<p data-nodeid="66408">使用 jitwatch 工具，可打开这个文件，看到详细的编译结果。</p>
<p data-nodeid="66409"><img src="https://s0.lgstatic.com/i/image/M00/4C/A9/Ciqc1F9Ye36AMYIDAAcn4iSavAY997.png" alt="Drawing 5.png" data-nodeid="66541"></p>
<p data-nodeid="66410">下面是一段测试代码：</p>
<pre class="lang-java" data-nodeid="66411"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">SimpleInliningTest</span> </span>{
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-title">SimpleInliningTest</span><span class="hljs-params">()</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">int</span> sum = <span class="hljs-number">0</span>;
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// 1_000_000 is F4240 in hex</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">for</span> (<span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>; i &lt; <span class="hljs-number">1_000_000</span>; i++) {
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;sum = <span class="hljs-keyword">this</span>.add(sum, <span class="hljs-number">99</span>);
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-comment">// 63 hex</span>
 &nbsp; &nbsp; &nbsp;  }
 &nbsp; &nbsp; &nbsp; &nbsp;System.out.println(<span class="hljs-string">"Sum:"</span> + sum);
 &nbsp;  }

 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">int</span> <span class="hljs-title">add</span><span class="hljs-params">(<span class="hljs-keyword">int</span> a, <span class="hljs-keyword">int</span> b)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">return</span> a + b;
 &nbsp;  }
 &nbsp; &nbsp;<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> </span>{
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-keyword">new</span> SimpleInliningTest();
 &nbsp;  }
}
</code></pre>
<p data-nodeid="66412">从执行后的结果可以看到，热点 for 循环已经使用 JIT 进行了编译，而里面应用的 add 方法，也已经被内联。</p>
<p data-nodeid="66413"><img src="https://s0.lgstatic.com/i/image/M00/4C/B5/CgqCHl9Ye4eAaVmXAATwlN8rh4w940.png" alt="Drawing 6.png" data-nodeid="66546"></p>
<h3 data-nodeid="66414">小结</h3>
<p data-nodeid="66415">JIT 是现代 JVM 主要的优化点，能够显著地提升程序的执行效率。从解释执行到最高层次的 C2，一个数量级的性能提升也是有可能的。但即时编译的过程是非常缓慢的，既耗时间也费空间，所以这些优化操作会和解释执行同时进行。</p>
<p data-nodeid="66416">值得注意的是，JIT 在某些情况下还会出现逆优化。比如一些热部署方式触发的 redefineClass，就会造成 JIT 编译结果的失效，相关的内联代码也需要重新生成。</p>
<p data-nodeid="66417">JIT 优化并不见得每次都有用，比如下面这段代码，编译后执行，会发生死循环。但如果你在启动的时候，加上 <strong data-nodeid="66555">-Djava.compiler=NONE</strong> 参数，禁用 JIT，它就能够执行下去。</p>
<pre class="lang-java" data-nodeid="66418"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">Demo</span> </span>{
	<span class="hljs-keyword">static</span> <span class="hljs-keyword">final</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">TestThread</span> <span class="hljs-keyword">extends</span> <span class="hljs-title">Thread</span> </span>{
		<span class="hljs-keyword">boolean</span> stop = <span class="hljs-keyword">false</span>;
		<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">boolean</span> <span class="hljs-title">isStop</span><span class="hljs-params">()</span> </span>{
			<span class="hljs-keyword">return</span> stop;
		}
		<span class="hljs-meta">@Override</span>
		<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">run</span><span class="hljs-params">()</span> </span>{
			<span class="hljs-keyword">try</span> {
				Thread.sleep(<span class="hljs-number">100</span>);
			} <span class="hljs-keyword">catch</span> (Exception ex) {
				ex.printStackTrace();
			}
			stop = <span class="hljs-keyword">true</span>;
			System.out.println(<span class="hljs-string">"END"</span>);
		}
	}
	<span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> </span>{
		<span class="hljs-keyword">int</span> i = <span class="hljs-number">0</span>;
		TestThread test = <span class="hljs-keyword">new</span> TestThread();
		test.start();
		<span class="hljs-keyword">while</span>(!test.isStop()){
			System.out.println(<span class="hljs-string">"--"</span>);
			i++;
		}
	}
}
</code></pre>
<p data-nodeid="66419"><strong data-nodeid="66559">我们主要看了方法内联、逃逸分析等概念，了解到一些方法在被优化后，对象并不一定是在堆上分配的，它可能在被标量替换后，直接在栈上分配。这几个知识点也是在面试中经常被问到的。</strong></p>
<p data-nodeid="67242" class="">JIT 的这些优化一般都是在后台进程默默地去做了，我们不需要关注太多。Code Cache 的容量达到上限，会影响程序执行的效率，但除非你有特别多的代码，默认的 240M 一般来说，足够用了。</p>

---

### 精选评论

##### Zorrrrrro：
> 死循环那个是经过怎样优化产生的？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 大体是因为未加volatile关键字造成了指令重排，happens-before原则不能保证。具体原因很复杂，可以参见这里：https://www.zhihu.com/question/263528143

##### **王者：
> 那个是使用jit,哪个没用,哪个是内联,哪个是没有使用内联,看的晕

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 你是指JITWatch这个工具么？在Inling report里面这些信息是有显示的。靠分析二进制代码会比较费劲，建议直接使用工具

