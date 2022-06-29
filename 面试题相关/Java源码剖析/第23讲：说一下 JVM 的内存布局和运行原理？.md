<p>JVM（Java Virtual Machine，Java 虚拟机）顾名思义就是用来执行 Java 程序的“虚拟主机”，实际的工作是将编译的 class 代码（字节码）翻译成底层操作系统可以运行的机器码并且进行调用执行，这也是 Java 程序能够“<strong>一次编写，到处运行</strong>”的原因（因为它会根据特定的操作系统生成对应的操作指令）。JVM 的功能很强大，像 Java 对象的创建、使用和销毁，还有垃圾回收以及某些高级的性能优化，例如，热点代码检测等功能都是在 JVM 中进行的。因为 JVM 是 Java 程序能够运行的根本，因此掌握 JVM 也已经成了一个合格 Java 程序员必备的技能。</p>
<p>我们本课时的面试题是，说一下 JVM 的内存布局和运行原理？</p>
<h3>典型回答</h3>
<p>JVM 的种类有很多，比如 HotSpot 虚拟机，它是 Sun/OracleJDK 和 OpenJDK 中的默认 JVM，也是目前使用范围最广的 JVM。我们常说的 JVM 其实泛指的是 HotSpot 虚拟机，还有曾经与 HotSpot 齐名为“三大商业 JVM”的 JRockit 和 IBM J9 虚拟机。但无论是什么类型的虚拟机都必须遵守 Oracle 官方发布的《Java虚拟机规范》，它是 Java 领域最权威最重要的著作之一，用于规范 JVM 的一些具体“行为”。</p>
<p>同样对于 JVM 的内存布局也一样，根据《Java虚拟机规范》的规定，JVM 的内存布局分为以下几个部分：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/12/FE/CgqCHl7ORYeAWLv7AABpB4-Dxgc707.png" alt="image (8).png"></p>
<p>以上 5 个内存区域的主要用途如下。</p>
<h4>1. 堆</h4>
<p><strong>堆（Java Heap）</strong> 也叫 Java 堆或者是 GC 堆，它是一个线程共享的内存区域，也是 JVM 中占用内存最大的一块区域，Java 中所有的对象都存储在这里。</p>
<p>《Java虚拟机规范》对 Java 堆的描述是：“所有的对象实例以及数组都应当在堆上分配”。但这在技术日益发展的今天已经有点不那么“准确”了，比如 JIT（Just In Time Compilation，即时编译&nbsp;）优化中的逃逸分析，使得变量可以直接在栈上被分配。</p>
<p>当对象或者是变量在方法中被创建之后，其指针可能被线程所引用，而这个对象就被称作<strong>指针逃逸</strong>或者是<strong>引用逃逸</strong>。</p>
<p>比如以下代码中的 sb 对象的逃逸：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> StringBuffer <span class="hljs-title">createString</span><span class="hljs-params">()</span> </span>{
    StringBuffer sb = <span class="hljs-keyword">new</span> StringBuffer();
    sb.append(<span class="hljs-string">"Java"</span>);
    <span class="hljs-keyword">return</span> sb;
}
</code></pre>
<p>sb 虽然是一个局部变量，但上述代码可以看出，它被直接 return 出去了，因此可能被赋值给了其他变量，并且被完全修改，于是此 sb 就逃逸到了方法外部。<br>
想要 sb 变量不逃逸也很简单，可以改为如下代码：</p>
<pre><code data-language="java" class="lang-java"><span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> String <span class="hljs-title">createString</span><span class="hljs-params">()</span> </span>{
    StringBuffer sb = <span class="hljs-keyword">new</span> StringBuffer();
    sb.append(<span class="hljs-string">"Java"</span>);
    <span class="hljs-keyword">return</span> sb.toString();
}
</code></pre>
<blockquote>
<p>小贴士：通过逃逸分析可以让变量或者是对象直接在栈上分配，从而极大地降低了垃圾回收的次数，以及堆分配对象的压力，进而提高了程序的整体运行效率。</p>
</blockquote>
<p>回到主题，堆大小的值可通过  -Xms 和 -Xmx 来设置（设置最小值和最大值），当堆超过最大值时就会抛出 OOM（OutOfMemoryError）异常。</p>
<h4>2. 方法区</h4>
<p><strong>方法区（Method Area）</strong> 也被称为非堆区，用于和“Java 堆”的概念进行区分，它也是线程共享的内存区域，用于存储已经被 JVM 加载的类型信息、常量、静态变量、代码缓存等数据。</p>
<p>说到方法区有人可能会联想到“永久代”，但对于《Java虚拟机规范》来说并没有规定这样一个区域，同样它也只是 HotSpot 中特有的一个概念。这是因为 HotSpot 技术团队把垃圾收集器的分代设计扩展到方法区之后才有的一个概念，可以理解为 HotSpot 技术团队只是用永久代来实现方法区而已，但这会导致一个致命的问题，这样设计更容易造成内存溢出。因为永久代有 -XX：MaxPermSize（方法区分配的最大内存）的上限，即使不设置也会有默认的大小。例如，32 位操作系统中的 4GB 内存限制等，并且这样设计导致了部分的方法在不同类型的 Java 虚拟机下的表现也不同，比如 String::intern() 方法。所以在 JDK 1.7 时 HotSpot 虚拟机已经把原本放在永久代的字符串常量池和静态变量等移出了方法区，并且在 JDK 1.8 中完全废弃了永久代的概念。</p>
<h4>3. 程序计数器</h4>
<p><strong>程序计数器（Program Counter Register）</strong> 线程独有一块很小的内存区域，保存当前线程所执行字节码的位置，包括正在执行的指令、跳转、分支、循环、异常处理等。</p>
<h4>4. 虚拟机栈</h4>
<p>虚拟机栈也叫 Java 虚拟机栈（Java Virtual Machine Stack），和程序计数器相同它也是线程独享的，用来描述 Java 方法的执行，在每个方法被执行时就会同步创建一个栈帧，用来存储局部变量表、操作栈、动态链接、方法出口等信息。当调用方法时执行入栈，而方法返回时执行出栈。</p>
<h4>5. 本地方法栈</h4>
<p>本地方法栈（Native Method Stacks）与虚拟机栈类似，它是线程独享的，并且作用也和虚拟机栈类似。只不过虚拟机栈是为虚拟机中执行的 Java 方法服务的，而本地方法栈则是为虚拟机使用到的本地（Native）方法服务。</p>
<blockquote>
<p>小贴士：需要注意的是《Java虚拟机规范》只规定了有这么几个区域，但没有规定 JVM 的具体实现细节，因此对于不同的 JVM 来说，实现也是不同的。例如，“永久代”是 HotSpot 中的一个概念，而对于 JRockit 来说就没有这个概念。所以很多人说的 JDK 1.8 把永久代转移到了元空间，这其实只是 HotSpot 的实现，而非《Java虚拟机规范》的规定。</p>
</blockquote>
<p>JVM 的执行流程是，首先先把 Java 代码（.java）转化成字节码（.class），然后通过类加载器将字节码加载到内存中，所谓的内存也就是我们上面介绍的运行时数据区，但字节码并不是可以直接交给操作系统执行的机器码，而是一套 JVM 的指令集。这个时候需要使用特定的命令解析器也就是我们俗称的**执行引擎（Execution Engine）**将字节码翻译成可以被底层操作系统执行的指令再去执行，这样就实现了整个 Java 程序的运行，这也是 JVM 的整体执行流程。</p>
<h3>考点分析</h3>
<p>JVM 的内存布局是一道必考的 Java 面试题，一般会作为 JVM 方面的第一道面试题出现，它也是中高级工程师必须掌握的一个知识点。和此知识点相关的面试题还有这些：类的加载分为几个阶段？每个阶段代表什么含义？加载了什么内容？</p>
<h3>知识扩展——类加载</h3>
<p>类的生命周期会经历以下 7 个阶段：</p>
<ol>
<li>加载阶段（Loading）</li>
<li>验证阶段（Verification）</li>
<li>准备阶段（Preparation）</li>
<li>解析阶段（Resolution）</li>
<li>初始化阶段（Initialization）</li>
<li>使用阶段（Using）</li>
<li>卸载阶段（Unloading）</li>
</ol>
<p>其中验证、准备、解析 3 个阶段统称为<strong>连接（Linking）</strong>，如下图所示：</p>
<p><img src="https://s0.lgstatic.com/i/image/M00/12/FF/CgqCHl7ORbmAQusYAABWJmj1sg8743.png" alt="image (9).png"></p>
<p>我们平常所说的 JVM 类加载通常指的就是前五个阶段：加载、验证、准备、解析、初始化等，接下来我们分别来看看。</p>
<h4>1. 加载阶段</h4>
<p>此阶段用于查到相应的类（通过类名进行查找）并将此类的字节流转换为方法区运行时的数据结构，然后再在内存中生成一个能代表此类的 java.lang.Class 对象，作为其他数据访问的入口。</p>
<blockquote>
<p>小贴士：需要注意的是加载阶段和连接阶段的部分动作有可能是交叉执行的，比如一部分字节码文件格式的验证，在加载阶段还未完成时就已经开始验证了。</p>
</blockquote>
<h4>2. 验证阶段</h4>
<p>此步骤主要是为了验证字节码的安全性，如果不做安全校验的话可能会载入非安全或有错误的字节码，从而导致系统崩溃，它是 JVM 自我保护的一项重要举措。</p>
<p>验证的主要动作大概有以下几个：</p>
<ul>
<li><strong>文件格式<strong><strong>校</strong></strong>验</strong>包括常量池中的常量类型、Class 文件的各个部分是否被删除或被追加了其他信息等；</li>
<li><strong>元数据<strong><strong>校</strong></strong>验</strong>包括父类正确性校验（检查父类是否有被 final 修饰）、抽象类校验等；</li>
<li><strong>字节码<strong><strong>校</strong></strong>验</strong>，此步骤最为关键和复杂，主要用于校验程序中的语义是否合法且符合逻辑；</li>
<li><strong>符号引用<strong><strong>校</strong></strong>验</strong>，对类自身以外比如常量池中的各种符号引用的信息进行匹配性校验。</li>
</ul>
<h4>3. 准备阶段</h4>
<p>此阶段是用来初始化并为类中定义的静态变量分配内存的，这些静态变量会被分配到方法区上。</p>
<p>HotSpot 虚拟机在 JDK 1.7 之前都在方法区，而 JDK 1.8 之后此变量会随着类对象一起存放到 Java 堆中。</p>
<h4>4. 解析阶段</h4>
<p>此阶段主要是用来解析类、接口、字段及方法的，解析时会把符号引用替换成直接引用。</p>
<p>所谓的符号引用是指以一组符号来描述所引用的目标，符号可以是任何形式的字面量，只要使用时能无歧义地定位到目标即可；而直接引用是可以直接指向目标的指针、相对偏移量或者是一个能间接定位到目标的句柄。</p>
<p>符号引用和直接引用有一个重要的区别：使用符号引用时被引用的目标不一定已经加载到内存中；而使用直接引用时，引用的目标必定已经存在虚拟机的内存中了。</p>
<h4>5. 初始化</h4>
<p>初始化阶段 JVM 就正式开始执行类中编写的 Java 业务代码了。到这一步骤之后，类的加载过程就算正式完成了。</p>
<h3>小结</h3>
<p>本课时讲了 JVM 的内存布局主要分为：堆、方法区、程序计数器、虚拟机栈和本地方法栈，并讲了 JVM 的执行流程，先把 Java 代码编译成字节码，再把字节码加载到运行时数据区；然后交给 JVM 引擎把字节码翻译为操作系统可以执行的指令进行执行；最后还讲了类加载的 5 个阶段：加载、验证、准备、解析和初始化。</p>

---

### 精选评论

##### bgg：
> 引用 是在哪个地方啊 方法区 还是虚拟机栈

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 同学，你好，你说的是符号引用么？符号引用与虚拟机的内存布局无关，引用的目标并不一定加载到内存中。

##### *杰：
> 老师好，问一个问题：1、加载阶段， 在内存中生成一个能代表此类的 java.lang.Class 对象；3、准备阶段， 用来初始化并为类中定义的静态变量分配内存的；如果按照如上所说，岂不是一个类的构造函数在一个类的静态代码块先执行？ 然而实际上静态代码块先于构造函数执行的， 如上所述是否有问题？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是先执行静态变量再执行构造方法的，Java 对象的创建过程分为初始化和实例化两个阶段哦。

##### *旭：
> 双亲委派是类加载中的某一个阶段的特性，还是只是一种机制？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 准确来说双亲委派属于类加载的一种手段，也就是实现类加载的方法（为了防止重复加载类）。

##### *光：
> 替换为直接引用的过程如果没加载过是不是找不到？会直接触发符号引用指向类的加载吗

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 替换的过程就是加载的过程哈。

