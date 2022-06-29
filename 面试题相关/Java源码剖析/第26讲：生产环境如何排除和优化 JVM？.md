<p data-nodeid="1249" class="">通过前面几个课时的学习，相信你对 JVM 的理论及实践等相关知识有了一个大体的印象。而本课时将重点讲解 JVM 的排查与优化，这样就会对 JVM 的知识点有一个完整的认识，从而可以更好地应用于实际工作或者面试了。</p>
<p data-nodeid="1250">我们本课时的面试题是，生产环境如何排查问题？</p>
<h3 data-nodeid="1251">典型回答</h3>
<p data-nodeid="1252">如果是在生产环境中直接排查 JVM 的话，最简单的做法就是使用 JDK 自带的 6 个非常实用的命令行工具来排查。它们分别是：jps、jstat、jinfo、jmap、jhat 和 jstack，它们都位于 JDK 的 bin 目录下，可以使用命令行工具直接运行，其目录如下图所示：</p>
<p data-nodeid="1253"><img src="https://s0.lgstatic.com/i/image/M00/19/15/CgqCHl7Z6XeAXH4mAAQG2yKoYrQ797.png" alt="Drawing 0.png" data-nodeid="1418"></p>
<p data-nodeid="1254">接下来我们来看看这些工具的具体使用。</p>
<h4 data-nodeid="1255">1. jps（虚拟机进程状况工具）</h4>
<p data-nodeid="1256">jps（JVM Process Status tool，虚拟机进程状况工具）它的功能和 Linux 中的 ps 命令比较类似，用于列出正在运行的 JVM 的 LVMID（Local Virtual Machine IDentifier，本地虚拟机唯一 ID），以及 JVM 的执行主类、JVM 启动参数等信息。语法如下：</p>
<pre class="lang-java" data-nodeid="1257"><code data-language="java">jps [options] [hostid]
</code></pre>
<p data-nodeid="1258">常用的 options 选项：</p>
<ul data-nodeid="1259">
<li data-nodeid="1260">
<p data-nodeid="1261">-l：用于输出运行主类的全名，如果是 jar 包，则输出 jar 包的路径；</p>
</li>
<li data-nodeid="1262">
<p data-nodeid="1263">-q：用于输出 LVMID（Local Virtual Machine Identifier，虚拟机唯一 ID）；</p>
</li>
<li data-nodeid="1264">
<p data-nodeid="1265">-m：用于输出虚拟机启动时传递给主类 main() 方法的参数；</p>
</li>
<li data-nodeid="1266">
<p data-nodeid="1267">-v：用于输出启动时的 JVM 参数。</p>
</li>
</ul>
<p data-nodeid="1268">使用实例：</p>
<pre class="lang-powershell" data-nodeid="1269"><code data-language="powershell">➜&nbsp; jps <span class="hljs-literal">-l</span>
<span class="hljs-number">68848</span>
<span class="hljs-number">40085</span> org.jetbrains.jps.cmdline.Launcher
<span class="hljs-number">40086</span> com.example.optimize.NativeOptimize
<span class="hljs-number">40109</span> jdk.jcmd/sun.tools.jps.Jps
<span class="hljs-number">68879</span> org.jetbrains.idea.maven.server.RemoteMavenServer36
➜&nbsp; jps <span class="hljs-literal">-q</span>
<span class="hljs-number">40368</span>
<span class="hljs-number">68848</span>
<span class="hljs-number">40085</span>
<span class="hljs-number">40086</span>
<span class="hljs-number">68879</span>
➜&nbsp; jps <span class="hljs-literal">-m</span>
<span class="hljs-number">40400</span> Jps <span class="hljs-literal">-m</span>
<span class="hljs-number">68848</span>
<span class="hljs-number">40085</span> Launcher /Applications/IntelliJ IDEA2.app/Contents/lib/idea_rt.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/oro<span class="hljs-literal">-2</span>.<span class="hljs-number">0.8</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/resources_en.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/maven<span class="hljs-literal">-model</span><span class="hljs-literal">-3</span>.<span class="hljs-number">6.1</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/qdox<span class="hljs-literal">-2</span>.<span class="hljs-number">0</span><span class="hljs-literal">-M10</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/plexus<span class="hljs-literal">-component</span><span class="hljs-literal">-annotations</span><span class="hljs-literal">-1</span>.<span class="hljs-number">7.1</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/httpcore<span class="hljs-literal">-4</span>.<span class="hljs-number">4.13</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/maven<span class="hljs-literal">-resolver</span><span class="hljs-literal">-api</span><span class="hljs-literal">-1</span>.<span class="hljs-number">3.3</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/netty<span class="hljs-literal">-common</span><span class="hljs-literal">-4</span>.<span class="hljs-number">1.47</span>.Final.jar:/Applications/IntelliJ IDEA2.app/Contents/plugins/java/lib/maven<span class="hljs-literal">-resolver</span><span class="hljs-literal">-connector</span><span class="hljs-literal">-basic</span><span class="hljs-literal">-1</span>.<span class="hljs-number">3.3</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/maven<span class="hljs-literal">-artifact</span><span class="hljs-literal">-3</span>.<span class="hljs-number">6.1</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/plexus<span class="hljs-literal">-utils</span><span class="hljs-literal">-3</span>.<span class="hljs-number">2.0</span>.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/netty<span class="hljs-literal">-resolver</span><span class="hljs-literal">-4</span>.<span class="hljs-number">1.47</span>.Final.jar:/Applications/IntelliJ IDEA2.app/Contents/lib/guava<span class="hljs-literal">-28</span>.<span class="hljs-number">2</span>-
<span class="hljs-number">40086</span> NativeOptimize
<span class="hljs-number">68879</span> RemoteMavenServer36
➜&nbsp; jps <span class="hljs-literal">-v</span>
<span class="hljs-number">68848</span>&nbsp; <span class="hljs-literal">-Xms128m</span> <span class="hljs-literal">-Xmx2048m</span> <span class="hljs-literal">-XX</span>:ReservedCodeCacheSize=<span class="hljs-number">240</span>m <span class="hljs-literal">-XX</span>:+UseCompressedOops <span class="hljs-literal">-Dfile</span>.encoding=UTF<span class="hljs-literal">-8</span> <span class="hljs-literal">-XX</span>:+UseConcMarkSweepGC <span class="hljs-literal">-XX</span>:SoftRefLRUPolicyMSPerMB=<span class="hljs-number">50</span> <span class="hljs-literal">-ea</span> <span class="hljs-literal">-XX</span>:CICompilerCount=<span class="hljs-number">2</span> <span class="hljs-literal">-Dsun</span>.io.useCanonPrefixCache=false <span class="hljs-literal">-Djava</span>.net.preferIPv4Stack=true <span class="hljs-literal">-Djdk</span>.http.auth.tunneling.disabledSchemes=<span class="hljs-string">""</span> <span class="hljs-literal">-XX</span>:+HeapDumpOnOutOfMemoryError <span class="hljs-literal">-XX</span>:<span class="hljs-literal">-OmitStackTraceInFastThrow</span> <span class="hljs-literal">-Djdk</span>.attach.allowAttachSelf <span class="hljs-literal">-Dkotlinx</span>.coroutines.debug=off <span class="hljs-literal">-Djdk</span>.module.illegalAccess.silent=true <span class="hljs-literal">-Xverify</span>:none <span class="hljs-literal">-XX</span>:ErrorFile=/Users/admin/java_error_in_idea_%p.log <span class="hljs-literal">-XX</span>:HeapDumpPath=/Users/admin/java_error_in_idea.hprof <span class="hljs-literal">-javaagent</span>:/Users/admin/.jetbrains/jetbrains<span class="hljs-literal">-agent</span><span class="hljs-literal">-v3</span>.<span class="hljs-number">2.0</span>.de72.<span class="hljs-number">619</span> <span class="hljs-literal">-Djb</span>.vmOptionsFile=/Users/admin/Library/Application Support/JetBrains/IntelliJIdea2020.<span class="hljs-number">1</span>/idea.vmoptions <span class="hljs-literal">-Didea</span>.paths.selector=IntelliJIdea2020.<span class="hljs-number">1</span> <span class="hljs-literal">-Didea</span>.executable=idea <span class="hljs-literal">-Didea</span>.home.path=/Applications/IntelliJ IDEA2.app/Contents <span class="hljs-literal">-Didea</span>.vendor.name=JetBrains
<span class="hljs-number">40085</span> Launcher <span class="hljs-literal">-Xmx700m</span> <span class="hljs-literal">-Djava</span>.awt.headless=true <span class="hljs-literal">-Djava</span>.endorsed.dirs=<span class="hljs-string">""</span> <span class="hljs-literal">-Djdt</span>.compiler.useSingleThread=true <span class="hljs-literal">-Dpreload</span>.project.path=/Users/admin/github/blog<span class="hljs-literal">-example</span>/blog<span class="hljs-literal">-example</span> <span class="hljs-literal">-Dpreload</span>.config.path=/Users/admin/Library/Application Support/JetBrains/IntelliJIdea2020.<span class="hljs-number">1</span>/options <span class="hljs-literal">-Dcompile</span>.parallel=false <span class="hljs-literal">-Drebuild</span>.on.dependency.change=true <span class="hljs-literal">-Djava</span>.net.preferIPv4Stack=true <span class="hljs-literal">-Dio</span>.netty.initialSeedUniquifier=<span class="hljs-number">1366842080359982660</span> <span class="hljs-literal">-Dfile</span>.encoding=UTF<span class="hljs-literal">-8</span> <span class="hljs-literal">-Duser</span>.language=zh <span class="hljs-literal">-Duser</span>.country=CN <span class="hljs-literal">-Didea</span>.paths.selector=IntelliJIdea2020.<span class="hljs-number">1</span> <span class="hljs-literal">-Didea</span>.home.path=/Applications/IntelliJ IDEA2.app/Contents <span class="hljs-literal">-Didea</span>.config.path=/Users/admin/Library/Application Support/JetBrains/IntelliJIdea2020.<span class="hljs-number">1</span> <span class="hljs-literal">-Didea</span>.plugins.path=/Users/admin/Library/Application Support/JetBrains/IntelliJIdea2020.<span class="hljs-number">1</span>/plugins <span class="hljs-literal">-Djps</span>.log.dir=/Users/admin/Library/Logs/JetBrains/IntelliJIdea2020.<span class="hljs-number">1</span>/build<span class="hljs-literal">-log</span> <span class="hljs-literal">-Djps</span>.fallback.jdk.home=/Applications/IntelliJ IDEA2.app/Contents/jbr/Contents/Home <span class="hljs-literal">-Djps</span>.fallback.jdk.version=<span class="hljs-number">11.0</span>.<span class="hljs-number">6</span> <span class="hljs-literal">-Dio</span>.netty.noUnsafe=true <span class="hljs-literal">-Djava</span>.io.tmpdir=/Users/admin/Library/Caches/Je
<span class="hljs-number">40086</span> NativeOptimize <span class="hljs-literal">-Dfile</span>.encoding=UTF<span class="hljs-literal">-8</span>
<span class="hljs-number">40425</span> Jps <span class="hljs-literal">-Dapplication</span>.home=/Users/admin/Library/Java/JavaVirtualMachines/openjdk<span class="hljs-literal">-14</span>/Contents/Home <span class="hljs-literal">-Xms8m</span> <span class="hljs-literal">-Djdk</span>.module.main=jdk.jcmd
<span class="hljs-number">68879</span> RemoteMavenServer36 <span class="hljs-literal">-Djava</span>.awt.headless=true <span class="hljs-literal">-Dmaven</span>.defaultProjectBuilder.disableGlobalModelCache=true <span class="hljs-literal">-Xmx768m</span> <span class="hljs-literal">-Didea</span>.maven.embedder.version=<span class="hljs-number">3.6</span>.<span class="hljs-number">1</span> <span class="hljs-literal">-Dmaven</span>.ext.class.path=/Applications/IntelliJ IDEA2.app/Contents/plugins/maven/lib/maven<span class="hljs-literal">-event</span><span class="hljs-literal">-listener</span>.jar <span class="hljs-literal">-Dfile</span>.encoding=UTF<span class="hljs-literal">-8</span>
</code></pre>
<h4 data-nodeid="1270">2. jstat（虚拟机统计信息监视工具）</h4>
<p data-nodeid="1271">jstat（JVM Statistics Monitoring Tool，虚拟机统计信息监视工具）用于监控虚拟机的运行状态信息。</p>
<p data-nodeid="1272">例如，我们用它来查询某个 Java 进程的垃圾收集情况，示例如下：</p>
<pre class="lang-java" data-nodeid="1273"><code data-language="java">➜&nbsp; jstat -gc <span class="hljs-number">43704</span>
&nbsp;S0C&nbsp; &nbsp; S1C&nbsp; &nbsp; S0U&nbsp; &nbsp; S1U&nbsp; &nbsp; &nbsp; EC&nbsp; &nbsp; &nbsp; &nbsp;EU&nbsp; &nbsp; &nbsp; &nbsp; OC&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;OU&nbsp; &nbsp; &nbsp; &nbsp;MC&nbsp; &nbsp; &nbsp;MU&nbsp; &nbsp; CCSC&nbsp; &nbsp;CCSU&nbsp; &nbsp;YGC&nbsp; &nbsp; &nbsp;YGCT&nbsp; &nbsp; FGC&nbsp; &nbsp; FGCT&nbsp; &nbsp; CGC&nbsp; &nbsp; CGCT&nbsp; &nbsp; &nbsp;GCT
<span class="hljs-number">10752.0</span> <span class="hljs-number">10752.0</span>&nbsp; <span class="hljs-number">0.0</span>&nbsp; &nbsp; <span class="hljs-number">0.0</span>&nbsp; &nbsp;<span class="hljs-number">65536.0</span>&nbsp; &nbsp;<span class="hljs-number">5243.4</span>&nbsp; &nbsp;<span class="hljs-number">175104.0</span>&nbsp; &nbsp; &nbsp;<span class="hljs-number">0.0</span>&nbsp; &nbsp; &nbsp;<span class="hljs-number">4480.0</span> <span class="hljs-number">774.0</span>&nbsp; <span class="hljs-number">384.0</span>&nbsp; &nbsp;<span class="hljs-number">75.8</span>&nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-number">0</span>&nbsp; &nbsp; <span class="hljs-number">0.000</span>&nbsp; &nbsp;<span class="hljs-number">0</span>&nbsp; &nbsp; &nbsp; <span class="hljs-number">0.000</span>&nbsp; &nbsp;-&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; -&nbsp; &nbsp; <span class="hljs-number">0.000</span>
</code></pre>
<p data-nodeid="1274">参数说明如下表所示：</p>
<table data-nodeid="2896">
<thead data-nodeid="2897">
<tr data-nodeid="2898">
<th align="center" data-nodeid="2900"><strong data-nodeid="2947">参数</strong></th>
<th align="center" data-nodeid="2901"><strong data-nodeid="2951">说明</strong></th>
</tr>
</thead>
<tbody data-nodeid="2904">
<tr data-nodeid="2905">
<td align="center" data-nodeid="2906">S0C</td>
<td align="center" data-nodeid="2907">年轻代中第一个存活区的大小</td>
</tr>
<tr data-nodeid="2908">
<td align="center" data-nodeid="2909">S1C</td>
<td align="center" data-nodeid="2910">年轻代中第二个存活区的大小</td>
</tr>
<tr data-nodeid="2911">
<td align="center" data-nodeid="2912">S0U</td>
<td align="center" data-nodeid="2913">年轻代中第一个存活区已使用的空间（字节）</td>
</tr>
<tr data-nodeid="2914">
<td align="center" data-nodeid="2915">S1U</td>
<td align="center" data-nodeid="2916">年轻代中第二个存活区已使用的空间（字节）</td>
</tr>
<tr data-nodeid="2917">
<td align="center" data-nodeid="2918">EC</td>
<td align="center" data-nodeid="2919" class="te-preview-highlight">Eden 区大小</td>
</tr>
<tr data-nodeid="2920">
<td align="center" data-nodeid="2921">EU</td>
<td align="center" data-nodeid="2922">年轻代中 Eden 区已使用的空间（字节）</td>
</tr>
<tr data-nodeid="2923">
<td align="center" data-nodeid="2924">OC</td>
<td align="center" data-nodeid="2925">老年代大小</td>
</tr>
<tr data-nodeid="2926">
<td align="center" data-nodeid="2927">OU</td>
<td align="center" data-nodeid="2928">老年代已使用的空间（字节）</td>
</tr>
<tr data-nodeid="2929">
<td align="center" data-nodeid="2930">YGC</td>
<td align="center" data-nodeid="2931">从应用程序启动到采样时 young gc 的次数</td>
</tr>
<tr data-nodeid="2932">
<td align="center" data-nodeid="2933">YGCT</td>
<td align="center" data-nodeid="2934">从应用程序启动到采样时 young gc 的所用的时间（s）</td>
</tr>
<tr data-nodeid="2935">
<td align="center" data-nodeid="2936">FGC</td>
<td align="center" data-nodeid="2937">从应用程序启动到采样时 full gc 的次数</td>
</tr>
<tr data-nodeid="2938">
<td align="center" data-nodeid="2939">FGCT</td>
<td align="center" data-nodeid="2940">从应用程序启动到采样时 full gc 的所用的时间</td>
</tr>
<tr data-nodeid="2941">
<td align="center" data-nodeid="2942">GCT</td>
<td align="center" data-nodeid="2943">从应用程序启动到采样时整个 gc 所用的时间</td>
</tr>
</tbody>
</table>


<blockquote data-nodeid="2268">
<p data-nodeid="2269" class="">注意：年轻代的 Eden 区满了会触发 young gc，老年代满了会触发 old gc。full gc 指的是清除整个堆，包括 young 区 和 old 区。</p>
</blockquote>

<p data-nodeid="1326">jstat 常用的查询参数有：</p>
<ul data-nodeid="1327">
<li data-nodeid="1328">
<p data-nodeid="1329">-class，查询类加载器信息；</p>
</li>
<li data-nodeid="1330">
<p data-nodeid="1331">-compiler，JIT 相关信息；</p>
</li>
<li data-nodeid="1332">
<p data-nodeid="1333">-gc，GC 堆状态；</p>
</li>
<li data-nodeid="1334">
<p data-nodeid="1335">-gcnew，新生代统计信息；</p>
</li>
<li data-nodeid="1336">
<p data-nodeid="1337">-gcutil，GC 堆统计汇总信息。</p>
</li>
</ul>
<h4 data-nodeid="1338">3. jinfo（查询虚拟机参数配置工具）</h4>
<p data-nodeid="1339">jinfo（Configuration Info for Java）用于查看和调整虚拟机各项参数。语法如下：</p>
<pre class="lang-java" data-nodeid="1340"><code data-language="java">jinfo &lt;option&gt; &lt;pid&gt;
</code></pre>
<p data-nodeid="1341">查看 JVM 参数示例如下：</p>
<pre class="lang-powershell" data-nodeid="1342"><code data-language="powershell">➜&nbsp; jinfo <span class="hljs-literal">-flags</span> <span class="hljs-number">45129</span>
VM Flags:
<span class="hljs-literal">-XX</span>:CICompilerCount=<span class="hljs-number">3</span> <span class="hljs-literal">-XX</span>:InitialHeapSize=<span class="hljs-number">268435456</span> <span class="hljs-literal">-XX</span>:MaxHeapSize=<span class="hljs-number">4294967296</span> <span class="hljs-literal">-XX</span>:MaxNewSize=<span class="hljs-number">1431306240</span> <span class="hljs-literal">-XX</span>:MinHeapDeltaBytes=<span class="hljs-number">524288</span> <span class="hljs-literal">-XX</span>:NewSize=<span class="hljs-number">89128960</span> <span class="hljs-literal">-XX</span>:OldSize=<span class="hljs-number">179306496</span> <span class="hljs-literal">-XX</span>:+UseCompressedClassPointers <span class="hljs-literal">-XX</span>:+UseCompressedOops <span class="hljs-literal">-XX</span>:+UseFastUnorderedTimeStamps <span class="hljs-literal">-XX</span>:+UseParallelGC
</code></pre>
<p data-nodeid="1343">其中 45129 是使用 jps 查询的 LVMID。<br>
我们可以通过 jinfo -flag [+/-]name 来修改虚拟机的参数值，比如下面的示例：</p>
<pre class="lang-powershell" data-nodeid="1344"><code data-language="powershell">➜&nbsp; jinfo <span class="hljs-literal">-flag</span> PrintGC <span class="hljs-number">45129</span> <span class="hljs-comment"># 查询是否开启 GC 打印</span>
<span class="hljs-literal">-XX</span>:<span class="hljs-literal">-PrintGC</span>
➜&nbsp; jinfo <span class="hljs-literal">-flag</span> +PrintGC <span class="hljs-number">45129</span> <span class="hljs-comment"># 开启 GC 打印</span>
➜&nbsp; jinfo <span class="hljs-literal">-flag</span> PrintGC <span class="hljs-number">45129</span> <span class="hljs-comment"># 查询是否开启 GC 打印</span>
<span class="hljs-literal">-XX</span>:+PrintGC
➜&nbsp; jinfo <span class="hljs-literal">-flag</span> <span class="hljs-literal">-PrintGC</span> <span class="hljs-number">45129</span> <span class="hljs-comment"># 关闭 GC 打印</span>
➜&nbsp; jinfo <span class="hljs-literal">-flag</span> PrintGC <span class="hljs-number">45129</span> <span class="hljs-comment"># 查询是否开启 GC 打印</span>
<span class="hljs-literal">-XX</span>:<span class="hljs-literal">-PrintGC</span>
</code></pre>
<h4 data-nodeid="1345">4. jmap（堆快照生成工具）</h4>
<p data-nodeid="1346">jmap（Memory Map for Java）用于查询堆的快照信息。</p>
<p data-nodeid="1347">查询堆信息示例如下：</p>
<pre class="lang-powershell" data-nodeid="1348"><code data-language="powershell">➜&nbsp; jmap <span class="hljs-literal">-heap</span> <span class="hljs-number">45129</span>
Attaching to <span class="hljs-keyword">process</span> ID <span class="hljs-number">45129</span>, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is <span class="hljs-number">25.101</span><span class="hljs-literal">-b13</span>
<span class="hljs-keyword">using</span> thread-local object allocation.
Parallel <span class="hljs-built_in">GC</span> with <span class="hljs-number">6</span> thread(s)
Heap Configuration:
&nbsp; &nbsp;MinHeapFreeRatio&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;= <span class="hljs-number">0</span>
&nbsp; &nbsp;MaxHeapFreeRatio&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;= <span class="hljs-number">100</span>
&nbsp; &nbsp;MaxHeapSize&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = <span class="hljs-number">4294967296</span> (<span class="hljs-number">4096.0</span>MB)
&nbsp; &nbsp;NewSize&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = <span class="hljs-number">89128960</span> (<span class="hljs-number">85.0</span>MB)
&nbsp; &nbsp;MaxNewSize&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;= <span class="hljs-number">1431306240</span> (<span class="hljs-number">1365.0</span>MB)
&nbsp; &nbsp;OldSize&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = <span class="hljs-number">179306496</span> (<span class="hljs-number">171.0</span>MB)
&nbsp; &nbsp;NewRatio&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;= <span class="hljs-number">2</span>
&nbsp; &nbsp;SurvivorRatio&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = <span class="hljs-number">8</span>
&nbsp; &nbsp;MetaspaceSize&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = <span class="hljs-number">21807104</span> (<span class="hljs-number">20.796875</span>MB)
&nbsp; &nbsp;CompressedClassSpaceSize = <span class="hljs-number">1073741824</span> (<span class="hljs-number">1024.0</span>MB)
&nbsp; &nbsp;MaxMetaspaceSize&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;= <span class="hljs-number">17592186044415</span> MB
&nbsp; &nbsp;G1HeapRegionSize&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;= <span class="hljs-number">0</span> (<span class="hljs-number">0.0</span>MB)
Heap Usage:
<span class="hljs-built_in">PS</span> Young Generation
Eden Space:
&nbsp; &nbsp;capacity = <span class="hljs-number">67108864</span> (<span class="hljs-number">64.0</span>MB)
&nbsp; &nbsp;used&nbsp; &nbsp; &nbsp;= <span class="hljs-number">5369232</span> (<span class="hljs-number">5.1204986572265625</span>MB)
&nbsp; &nbsp;free&nbsp; &nbsp; &nbsp;= <span class="hljs-number">61739632</span> (<span class="hljs-number">58.87950134277344</span>MB)
&nbsp; &nbsp;<span class="hljs-number">8.000779151916504</span>% used
From Space:
&nbsp; &nbsp;capacity = <span class="hljs-number">11010048</span> (<span class="hljs-number">10.5</span>MB)
&nbsp; &nbsp;used&nbsp; &nbsp; &nbsp;= <span class="hljs-number">0</span> (<span class="hljs-number">0.0</span>MB)
&nbsp; &nbsp;free&nbsp; &nbsp; &nbsp;= <span class="hljs-number">11010048</span> (<span class="hljs-number">10.5</span>MB)
&nbsp; &nbsp;<span class="hljs-number">0.0</span>% used
To Space:
&nbsp; &nbsp;capacity = <span class="hljs-number">11010048</span> (<span class="hljs-number">10.5</span>MB)
&nbsp; &nbsp;used&nbsp; &nbsp; &nbsp;= <span class="hljs-number">0</span> (<span class="hljs-number">0.0</span>MB)
&nbsp; &nbsp;free&nbsp; &nbsp; &nbsp;= <span class="hljs-number">11010048</span> (<span class="hljs-number">10.5</span>MB)
&nbsp; &nbsp;<span class="hljs-number">0.0</span>% used
<span class="hljs-built_in">PS</span> Old Generation
&nbsp; &nbsp;capacity = <span class="hljs-number">179306496</span> (<span class="hljs-number">171.0</span>MB)
&nbsp; &nbsp;used&nbsp; &nbsp; &nbsp;= <span class="hljs-number">0</span> (<span class="hljs-number">0.0</span>MB)
&nbsp; &nbsp;free&nbsp; &nbsp; &nbsp;= <span class="hljs-number">179306496</span> (<span class="hljs-number">171.0</span>MB)
&nbsp; &nbsp;<span class="hljs-number">0.0</span>% used

<span class="hljs-number">2158</span> interned Strings occupying <span class="hljs-number">152472</span> bytes.
</code></pre>
<p data-nodeid="1349">我们也可以直接生成堆快照文件，示例如下：</p>
<pre class="lang-powershell" data-nodeid="1350"><code data-language="powershell">➜&nbsp; jmap <span class="hljs-literal">-dump</span>:format=b,file=/Users/admin/Documents/<span class="hljs-number">2020</span>.dump <span class="hljs-number">47380</span>
Dumping heap to /Users/admin/Documents/<span class="hljs-number">2020</span>.dump ...
Heap dump file created
</code></pre>
<h4 data-nodeid="1351">5. jhat（堆快照分析功能）</h4>
<p data-nodeid="1352">jhat（JVM Heap Analysis Tool，堆快照分析工具）和 jmap 搭配使用，用于启动一个 web 站点来分析 jmap 生成的快照文件。</p>
<p data-nodeid="1353">执行示例如下：</p>
<pre class="lang-java" data-nodeid="1354"><code data-language="java">jhat /Users/admin/Documents/<span class="hljs-number">2020</span>.dump
Reading from /Users/admin/Documents/<span class="hljs-number">2020</span>.dump...
Dump file created Tue May <span class="hljs-number">26</span> <span class="hljs-number">16</span>:<span class="hljs-number">12</span>:<span class="hljs-number">41</span> CST <span class="hljs-number">2020</span>
Snapshot read, resolving...
Resolving <span class="hljs-number">17797</span> objects...
Chasing references, expect <span class="hljs-number">3</span> dots...
Eliminating duplicate references...
Snapshot resolved.
Started HTTP server on port <span class="hljs-number">7000</span>
Server is ready.
</code></pre>
<p data-nodeid="1355">上述信息表示 jhat 启动了一个 http 的服务器端口为 7000 的站点来展示信息，此时我们在浏览器中输入：<a href="http://localhost:7000/" data-nodeid="1503">http://localhost:7000/</a>，会看到如下图所示的信息：</p>
<p data-nodeid="1356"><img src="https://s0.lgstatic.com/i/image/M00/19/0A/Ciqc1F7Z6YyAZxEUAAHRgiBjrK8704.png" alt="Drawing 1.png" data-nodeid="1507"></p>
<h4 data-nodeid="1357">6.&nbsp;jstack（查询虚拟机当前的线程快照信息）</h4>
<p data-nodeid="1358">jstack（Stack Trace for Java）用于查看当前虚拟机的线程快照，用它可以排查线程的执行状况，例如排查死锁、死循环等问题。</p>
<p data-nodeid="1359">比如，我们先写一段死锁的代码：</p>
<pre class="lang-java" data-nodeid="1360"><code data-language="java"><span class="hljs-keyword">public</span> <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">NativeOptimize</span> </span>{
    <span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> Object obj1 = <span class="hljs-keyword">new</span> Object();
    <span class="hljs-keyword">private</span> <span class="hljs-keyword">static</span> Object obj2 = <span class="hljs-keyword">new</span> Object();
    <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> <span class="hljs-title">main</span><span class="hljs-params">(String[] args)</span> </span>{
        <span class="hljs-keyword">new</span> Thread(<span class="hljs-keyword">new</span> Runnable() {
            <span class="hljs-meta">@Override</span>
            <span class="hljs-function"><span class="hljs-keyword">public</span> <span class="hljs-keyword">void</span> <span class="hljs-title">run</span><span class="hljs-params">()</span> </span>{
                <span class="hljs-keyword">synchronized</span> (obj2) {
                    System.out.println(Thread.currentThread().getName() + <span class="hljs-string">"锁住 obj2"</span>);
                    <span class="hljs-keyword">try</span> {
                        Thread.sleep(<span class="hljs-number">1000</span>);
                    } <span class="hljs-keyword">catch</span> (InterruptedException e) {
                        e.printStackTrace();
                    }
                    <span class="hljs-keyword">synchronized</span> (obj1) {
                        <span class="hljs-comment">// 执行不到这里</span>
                        System.out.println(<span class="hljs-string">"1秒钟后，"</span> + Thread.currentThread().getName()
                                + <span class="hljs-string">"锁住 obj1"</span>);
                    }
                }
            }
        }).start();
        <span class="hljs-keyword">synchronized</span> (obj1) {
            System.out.println(Thread.currentThread().getName() + <span class="hljs-string">"锁住 obj1"</span>);
            <span class="hljs-keyword">try</span> {
                Thread.sleep(<span class="hljs-number">1000</span>);
            } <span class="hljs-keyword">catch</span> (InterruptedException e) {
                e.printStackTrace();
            }
            <span class="hljs-keyword">synchronized</span> (obj2) {
                <span class="hljs-comment">// 执行不到这里</span>
                System.out.println(<span class="hljs-string">"1秒钟后，"</span> + Thread.currentThread().getName()
                        + <span class="hljs-string">"锁住 obj2"</span>);
            }
        }
    }
}
</code></pre>
<p data-nodeid="1361">以上程序的执行结果如下：</p>
<pre class="lang-java" data-nodeid="1362"><code data-language="java">main：锁住 obj1
Thread-<span class="hljs-number">0</span>：锁住 obj2
</code></pre>
<p data-nodeid="1363">此时我们使用 jstack 工具打印一下当前线程的快照信息，结果如下：</p>
<pre class="lang-powershell" data-nodeid="1364"><code data-language="powershell">➜&nbsp; bin jstack <span class="hljs-literal">-l</span> <span class="hljs-number">50016</span>
<span class="hljs-number">2020</span><span class="hljs-literal">-05</span><span class="hljs-literal">-26</span> <span class="hljs-number">18</span>:<span class="hljs-number">01</span>:<span class="hljs-number">41</span>
Full thread dump Java HotSpot(TM) <span class="hljs-number">64</span><span class="hljs-literal">-Bit</span> Server VM (<span class="hljs-number">25.101</span><span class="hljs-literal">-b13</span> mixed mode):
<span class="hljs-string">"Attach Listener"</span> <span class="hljs-comment">#10 daemon prio=9 os_prio=31 tid=0x00007f8c00840800 nid=0x3c03 waiting on condition [0x0000000000000000]</span>
&nbsp; &nbsp;java.lang.Thread.State: RUNNABLE
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"Thread-0"</span> <span class="hljs-comment">#9 prio=5 os_prio=31 tid=0x00007f8c00840000 nid=0x3e03 waiting for monitor entry [0x00007000100c8000]</span>
&nbsp; &nbsp;java.lang.Thread.State: BLOCKED (on object monitor)
	at com.example.optimize.NativeOptimize<span class="hljs-variable">$1</span>.run(NativeOptimize.java:<span class="hljs-number">25</span>)
	- waiting to lock &lt;<span class="hljs-number">0</span>x000000076abb62d0&gt; (a java.lang.Object)
	- locked &lt;<span class="hljs-number">0</span>x000000076abb62e0&gt; (a java.lang.Object)
	at java.lang.Thread.run(Thread.java:<span class="hljs-number">745</span>)
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"Service Thread"</span> <span class="hljs-comment">#8 daemon prio=9 os_prio=31 tid=0x00007f8c01814800 nid=0x4103 runnable [0x0000000000000000]</span>
&nbsp; &nbsp;java.lang.Thread.State: RUNNABLE
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"C1 CompilerThread2"</span> <span class="hljs-comment">#7 daemon prio=9 os_prio=31 tid=0x00007f8c0283c800 nid=0x4303 waiting on condition [0x0000000000000000]</span>
&nbsp; &nbsp;java.lang.Thread.State: RUNNABLE
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"C2 CompilerThread1"</span> <span class="hljs-comment">#6 daemon prio=9 os_prio=31 tid=0x00007f8c0300a800 nid=0x4403 waiting on condition [0x0000000000000000]</span>
&nbsp; &nbsp;java.lang.Thread.State: RUNNABLE
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"C2 CompilerThread0"</span> <span class="hljs-comment">#5 daemon prio=9 os_prio=31 tid=0x00007f8c0283c000 nid=0x3603 waiting on condition [0x0000000000000000]</span>
&nbsp; &nbsp;java.lang.Thread.State: RUNNABLE
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"Signal Dispatcher"</span> <span class="hljs-comment">#4 daemon prio=9 os_prio=31 tid=0x00007f8c0283b000 nid=0x4603 runnable [0x0000000000000000]</span>
&nbsp; &nbsp;java.lang.Thread.State: RUNNABLE
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"Finalizer"</span> <span class="hljs-comment">#3 daemon prio=8 os_prio=31 tid=0x00007f8c03001000 nid=0x5003 in Object.wait() [0x000070000f8ad000]</span>
&nbsp; &nbsp;java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on &lt;<span class="hljs-number">0</span>x000000076ab08ee0&gt; (a java.lang.ref.ReferenceQueue<span class="hljs-variable">$Lock</span>)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:<span class="hljs-number">143</span>)
	- locked &lt;<span class="hljs-number">0</span>x000000076ab08ee0&gt; (a java.lang.ref.ReferenceQueue<span class="hljs-variable">$Lock</span>)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:<span class="hljs-number">164</span>)
	at java.lang.ref.Finalizer<span class="hljs-variable">$FinalizerThread</span>.run(Finalizer.java:<span class="hljs-number">209</span>)
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"Reference Handler"</span> <span class="hljs-comment">#2 daemon prio=10 os_prio=31 tid=0x00007f8c03000000 nid=0x2f03 in Object.wait() [0x000070000f7aa000]</span>
&nbsp; &nbsp;java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	- waiting on &lt;<span class="hljs-number">0</span>x000000076ab06b50&gt; (a java.lang.ref.Reference<span class="hljs-variable">$Lock</span>)
	at java.lang.Object.wait(Object.java:<span class="hljs-number">502</span>)
	at java.lang.ref.Reference.tryHandlePending(Reference.java:<span class="hljs-number">191</span>)
	- locked &lt;<span class="hljs-number">0</span>x000000076ab06b50&gt; (a java.lang.ref.Reference<span class="hljs-variable">$Lock</span>)
	at java.lang.ref.Reference<span class="hljs-variable">$ReferenceHandler</span>.run(Reference.java:<span class="hljs-number">153</span>)
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"main"</span> <span class="hljs-comment">#1 prio=5 os_prio=31 tid=0x00007f8c00802800 nid=0x1003 waiting for monitor entry [0x000070000ef92000]</span>
&nbsp; &nbsp;java.lang.Thread.State: BLOCKED (on object monitor)
	at com.example.optimize.NativeOptimize.main(NativeOptimize.java:<span class="hljs-number">41</span>)
	- waiting to lock &lt;<span class="hljs-number">0</span>x000000076abb62e0&gt; (a java.lang.Object)
	- locked &lt;<span class="hljs-number">0</span>x000000076abb62d0&gt; (a java.lang.Object)
&nbsp; &nbsp;Locked ownable synchronizers:
	- None
<span class="hljs-string">"VM Thread"</span> os_prio=<span class="hljs-number">31</span> tid=<span class="hljs-number">0</span>x00007f8c01008800 nid=<span class="hljs-number">0</span>x2e03 runnable
<span class="hljs-string">"GC task thread#0 (ParallelGC)"</span> os_prio=<span class="hljs-number">31</span> tid=<span class="hljs-number">0</span>x00007f8c00803000 nid=<span class="hljs-number">0</span>x2007 runnable

<span class="hljs-string">"GC task thread#1 (ParallelGC)"</span> os_prio=<span class="hljs-number">31</span> tid=<span class="hljs-number">0</span>x00007f8c00006800 nid=<span class="hljs-number">0</span>x2403 runnable

<span class="hljs-string">"GC task thread#2 (ParallelGC)"</span> os_prio=<span class="hljs-number">31</span> tid=<span class="hljs-number">0</span>x00007f8c01800800 nid=<span class="hljs-number">0</span>x2303 runnable
<span class="hljs-string">"GC task thread#3 (ParallelGC)"</span> os_prio=<span class="hljs-number">31</span> tid=<span class="hljs-number">0</span>x00007f8c01801800 nid=<span class="hljs-number">0</span>x2a03 runnable
<span class="hljs-string">"GC task thread#4 (ParallelGC)"</span> os_prio=<span class="hljs-number">31</span> tid=<span class="hljs-number">0</span>x00007f8c01802000 nid=<span class="hljs-number">0</span>x5403 runnable
<span class="hljs-string">"GC task thread#5 (ParallelGC)"</span> os_prio=<span class="hljs-number">31</span> tid=<span class="hljs-number">0</span>x00007f8c01006800 nid=<span class="hljs-number">0</span>x2d03 runnable
<span class="hljs-string">"VM Periodic Task Thread"</span> os_prio=<span class="hljs-number">31</span> tid=<span class="hljs-number">0</span>x00007f8c00010800 nid=<span class="hljs-number">0</span>x3803 waiting on condition
JNI global references: <span class="hljs-number">6</span>
Found one Java<span class="hljs-literal">-level</span> deadlock:
=============================
<span class="hljs-string">"Thread-0"</span>:
&nbsp; waiting to lock monitor <span class="hljs-number">0</span>x00007f8c000102a8 (object <span class="hljs-number">0</span>x000000076abb62d0, a java.lang.Object),
&nbsp; which is held by <span class="hljs-string">"main"</span>
<span class="hljs-string">"main"</span>:
&nbsp; waiting to lock monitor <span class="hljs-number">0</span>x00007f8c0000ed58 (object <span class="hljs-number">0</span>x000000076abb62e0, a java.lang.Object),
&nbsp; which is held by <span class="hljs-string">"Thread-0"</span>

Java stack information <span class="hljs-keyword">for</span> the threads listed above:
===================================================
<span class="hljs-string">"Thread-0"</span>:
	at com.example.optimize.NativeOptimize<span class="hljs-variable">$1</span>.run(NativeOptimize.java:<span class="hljs-number">25</span>)
	- waiting to lock &lt;<span class="hljs-number">0</span>x000000076abb62d0&gt; (a java.lang.Object)
	- locked &lt;<span class="hljs-number">0</span>x000000076abb62e0&gt; (a java.lang.Object)
	at java.lang.Thread.run(Thread.java:<span class="hljs-number">745</span>)
<span class="hljs-string">"main"</span>:
	at com.example.optimize.NativeOptimize.main(NativeOptimize.java:<span class="hljs-number">41</span>)
	- waiting to lock &lt;<span class="hljs-number">0</span>x000000076abb62e0&gt; (a java.lang.Object)
	- locked &lt;<span class="hljs-number">0</span>x000000076abb62d0&gt; (a java.lang.Object)

Found <span class="hljs-number">1</span> deadlock.
</code></pre>
<p data-nodeid="1365">从上述信息可以看出使用 jstack ，可以很方便地排查出代码中出现“deadlock”（死锁）的问题。</p>
<h3 data-nodeid="1366">考点分析</h3>
<p data-nodeid="1367">Java 虚拟机的排查工具是一个合格程序员必备的技能，使用它我们可以很方便地定位出问题的所在，尤其在团队合作的今天，每个人各守一摊很容易出现隐藏的 bug（缺陷）。因此使用这些排查功能可以帮我们快速地定位并解决问题，所以它也是面试中常问的问题之一。</p>
<p data-nodeid="1368">和此知识点相关的面试题还有以下这些：</p>
<ul data-nodeid="1369">
<li data-nodeid="1370">
<p data-nodeid="1371">除了比较实用的命令行工具之外，有没有方便一点的排查工具？</p>
</li>
<li data-nodeid="1372">
<p data-nodeid="1373">JVM 常见的调优手段有哪些？</p>
</li>
</ul>
<h3 data-nodeid="1374">知识扩展</h3>
<h4 data-nodeid="1375">可视化排查工具</h4>
<p data-nodeid="1376">JVM 除了上面的 6 个基础命令行工具之外，还有两个重要的视图调试工具，即 JConsole 和 JVisualVM，它们相比于命令行工具使用更方便、操作更简单、结果展现也更直观。</p>
<p data-nodeid="1377">JConsole 和 JVisualVM 都位于 JDK 的 bin 目录下，JConsole（Java Monitoring and Management Console）是最早期的视图调试工具，其启动页面如下图所示：</p>
<p data-nodeid="1378"><img src="https://s0.lgstatic.com/i/image/M00/19/0A/Ciqc1F7Z6ZuAZOoBAAIE-PlP8mY751.png" alt="Drawing 2.png" data-nodeid="1525"></p>
<p data-nodeid="1379">可以看出我们可以用它来连接远程的服务器，或者是直接调试本机，这样就可以在不消耗生产环境的性能下，从本机启动 JConsole 来连接服务器。选择了调试的进程之后，运行界面如下图所示：</p>
<p data-nodeid="1380"><img src="https://s0.lgstatic.com/i/image/M00/19/16/CgqCHl7Z6aKAZfp0AAKE9E3ar6c820.png" alt="Drawing 3.png" data-nodeid="1529"></p>
<p data-nodeid="1381">从上图可以看出，使用 JConsole 可以监控线程、CPU、类、堆以及 VM 的相关信息，同样我们可以通过线程这一页的信息，发现之前我们故意写的死锁问题，如下图所示：</p>
<p data-nodeid="1382"><img src="https://s0.lgstatic.com/i/image/M00/19/16/CgqCHl7Z6amAF-LOAAKoeNJzszw795.png" alt="Drawing 4.png" data-nodeid="1533"></p>
<p data-nodeid="1383">可以看到 main（主线程）和 Thread-0 线程处于死锁状态。</p>
<p data-nodeid="1384">JVisualVM 的启动图如下图所示：</p>
<p data-nodeid="1385"><img src="https://s0.lgstatic.com/i/image/M00/19/16/CgqCHl7Z6a-AK0DMAAHTxm7JgYI402.png" alt="Drawing 5.png" data-nodeid="1538"></p>
<p data-nodeid="1386">由上图可知，JVisualVM 既可以调试本地也可以调试远程服务器，当我们选择了相关的进程之后，运行如下图所示：</p>
<p data-nodeid="1387"><img src="https://s0.lgstatic.com/i/image/M00/19/16/CgqCHl7Z6beAUJ6sAAMkaaTyA9U352.png" alt="Drawing 6.png" data-nodeid="1542"></p>
<p data-nodeid="1388">可以看出 JVisualVM 除了包含了 JConsole 的信息之外，还有更多的详细信息，并且更加智能。例如，线程死锁检查的这页内容如下图所示：</p>
<p data-nodeid="1389"><img src="https://s0.lgstatic.com/i/image/M00/19/0A/Ciqc1F7Z6b2Aa8CCAANpsufYncw124.png" alt="Drawing 7.png" data-nodeid="1546"></p>
<p data-nodeid="1390">可以看出 JVisualVM 会直接给你一个死锁的提示，而 JConsole 则需要程序员自己分析。</p>
<h4 data-nodeid="1391">JVM 调优</h4>
<p data-nodeid="1392">JVM 调优主要是根据实际的硬件配置信息重新设置 JVM 参数来进行调优的，例如，硬件的内存配置很高，但 JVM 因为是默认参数，所以最大内存和初始化堆内存很小，这样就不能更好地利用本地的硬件优势了。因此，需要调整这些参数，让 JVM 在固定的配置下发挥最大的价值。</p>
<p data-nodeid="1393">JVM 常见调优参数包含以下这些：</p>
<ul data-nodeid="1394">
<li data-nodeid="1395">
<p data-nodeid="1396">-Xmx，设置最大堆内存大小；</p>
</li>
<li data-nodeid="1397">
<p data-nodeid="1398">-Xms，设置初始堆内存大小；</p>
</li>
<li data-nodeid="1399">
<p data-nodeid="1400">-XX:MaxNewSize，设置新生代的最大内存；</p>
</li>
<li data-nodeid="1401">
<p data-nodeid="1402">-XX:MaxTenuringThreshold，设置新生代对象经过一定的次数晋升到老生代；</p>
</li>
<li data-nodeid="1403">
<p data-nodeid="1404">-XX:PretrnureSizeThreshold，设置大对象的值，超过这个值的对象会直接进入老生代；</p>
</li>
<li data-nodeid="1405">
<p data-nodeid="1406">-XX:NewRatio，设置分代垃圾回收器新生代和老生代内存占比；</p>
</li>
<li data-nodeid="1407">
<p data-nodeid="1408">-XX:SurvivorRatio，设置新生代 Eden、Form Survivor、To Survivor 占比。</p>
</li>
</ul>
<p data-nodeid="1409">我们要根据自己的业务场景和硬件配置来设置这些值。例如，当我们的业务场景会有很多大的临时对象产生时，因为这些大对象只有很短的生命周期，因此需要把“-XX:MaxNewSize”的值设置的尽量大一些，否则就会造成大量短生命周期的大对象进入老生代，从而很快消耗掉了老生代的内存，这样就会频繁地触发 full gc，从而影响了业务的正常运行。</p>
<h3 data-nodeid="1410">小结</h3>
<p data-nodeid="1411" class="">本课时我们讲了 JVM 排查的 6 个基本命令行工具：jps、jstat、jinfo、jmap、jhat、jstack，以及 2 个视图排查工具：JConsole 和 JVisualVM；同时还讲了 JVM 的常见调优参数，希望本课时的内容可以切实的帮助到你。</p>

---

### 精选评论

##### **帆：
> 拉钩网上最好的课程，没有之一， 没什么说的把，感谢！

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 我们会一直努力，给大家带来更多优质的课程，让我们一起进步~

##### *明：
> 老师，young gc，old gc,full gc 都会触发 stop the world吗？

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 是的，这几个 gc 都会触发 STW。

