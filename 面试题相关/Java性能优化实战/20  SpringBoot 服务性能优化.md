<p data-nodeid="1385" class="">在开始对 SpringBoot 服务进行性能优化之前，你需要做一些准备，把 SpringBoot 服务的一些数据暴露出来。比如，你的服务用到了缓存，就需要把缓存命中率这些数据进行收集；用到了数据库连接池，就需要把连接池的参数给暴露出来。</p>
<p data-nodeid="1386">我们这里采用的监控工具是 Prometheus，它是一个是时序数据库，能够存储我们的指标。SpringBoot 可以非常方便地接入到 Prometheus 中。</p>
<h3 data-nodeid="1387">SpringBoot 如何开启监控？</h3>
<p data-nodeid="1388">创建一个 SpringBoot 项目后，首先加入 maven 依赖。</p>
<pre class="lang-js" data-nodeid="1389"><code data-language="js">&lt;dependency&gt;
 &nbsp; &nbsp; <span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.springframework.boot<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span></span>
 &nbsp; &nbsp; <span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>spring-boot-starter-actuator<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span></span>
 &lt;/dependency&gt;
 <span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span>
 &nbsp; &nbsp; <span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>io.micrometer<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span>
 &nbsp; &nbsp; <span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>micrometer-registry-prometheus<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span>
 <span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span></span>
 <span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span>
 &nbsp; &nbsp; <span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>io.micrometer<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span>
 &nbsp; &nbsp; <span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>micrometer-core<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span>
 <span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span></span>
</code></pre>
<p data-nodeid="1390">然后，我们需要在 application.properties 配置文件中，开放相关的监控接口。</p>
<pre class="lang-js" data-nodeid="1391"><code data-language="js">management.endpoint.metrics.enabled=<span class="hljs-literal">true</span>
management.endpoints.web.exposure.include=*
management.endpoint.prometheus.enabled=<span class="hljs-literal">true</span>
management.metrics.export.prometheus.enabled=<span class="hljs-literal">true</span>
</code></pre>
<p data-nodeid="1392">启动之后，我们就可以通过访问<a href="http://localhost:8080/actuator/prometheus" data-nodeid="1532">监控接口</a>来获取监控数据。</p>
<p data-nodeid="1393"><img src="https://s0.lgstatic.com/i/image/M00/50/07/CgqCHl9htcuAAw51AAK0O_g_pbM862.png" alt="Drawing 0.png" data-nodeid="1536"></p>
<p data-nodeid="1394">想要监控业务数据也是比较简单的，你只需要注入一个 MeterRegistry 实例即可，下面是一段示例代码：</p>
<pre class="lang-js" data-nodeid="1395"><code data-language="js">@Autowired
MeterRegistry registry;

@GetMapping(<span class="hljs-string">"/test"</span>)
@ResponseBody
public <span class="hljs-built_in">String</span> test() {
 &nbsp; &nbsp;registry.counter(<span class="hljs-string">"test"</span>,
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-string">"from"</span>, <span class="hljs-string">"127.0.0.1"</span>,
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-string">"method"</span>, <span class="hljs-string">"test"</span>
 &nbsp;  ).increment();

 &nbsp; &nbsp;<span class="hljs-keyword">return</span> <span class="hljs-string">"ok"</span>;
}
</code></pre>
<p data-nodeid="1396">从监控连接中，我们可以找到刚刚添加的监控信息。</p>
<pre class="lang-js" data-nodeid="1397"><code data-language="js">test_total{<span class="hljs-keyword">from</span>=<span class="hljs-string">"127.0.0.1"</span>,method=<span class="hljs-string">"test"</span>,} <span class="hljs-number">5.0</span>
</code></pre>
<p data-nodeid="1398">这里简单介绍一下流行的<strong data-nodeid="1544">Prometheus 监控体系</strong>，Prometheus 使用拉的方式获取监控数据，这个暴露数据的过程可以交给功能更加齐全的 telegraf 组件。</p>
<p data-nodeid="1399"><img src="https://s0.lgstatic.com/i/image/M00/50/07/CgqCHl9htdiAO89HAAK1NRYCNZE604.png" alt="Drawing 1.png" data-nodeid="1547"></p>
<p data-nodeid="1400">如上图，我们通常使用 Grafana 进行监控数据的展示，使用 AlertManager 组件进行提前预警。这一部分的搭建工作不是我们的重点，感兴趣的同学可自行研究。</p>
<p data-nodeid="1401">下图便是一张典型的监控图，可以看到 Redis 的缓存命中率等情况。</p>
<p data-nodeid="1402"><img src="https://s0.lgstatic.com/i/image/M00/4F/FB/Ciqc1F9htd-AXIKHAANYYdIDl6g753.png" alt="Drawing 2.png" data-nodeid="1552"></p>
<h3 data-nodeid="1403">Java 生成火焰图</h3>
<p data-nodeid="1404"><strong data-nodeid="1558">火焰图</strong>是用来分析程序运行瓶颈的工具。</p>
<p data-nodeid="1405">火焰图也可以用来分析 Java 应用。可以从 <a href="https://github.com/jvm-profiling-tools/async-profiler" data-nodeid="1562">github</a> 上下载 async-profiler 的压缩包进行相关操作。比如，我们把它解压到 /root/ 目录，然后以 javaagent 的方式来启动 Java 应用，命令行如下：</p>
<pre class="lang-js" data-nodeid="1406"><code data-language="js">java -agentpath:<span class="hljs-regexp">/root/</span>build/libasyncProfiler.so=start,svg,file=profile.svg -jar spring-petclinic<span class="hljs-number">-2.3</span><span class="hljs-number">.1</span>.BUILD-SNAPSHOT.jar
</code></pre>
<p data-nodeid="1407">运行一段时间后，停止进程，可以看到在当前目录下，生成了 profile.svg 文件，这个文件是可以用浏览器打开的。<br>
如下图所示，纵向，表示的是调用栈的深度；横向，表明的是消耗的时间。所以格子的宽度越大，越说明它可能是一个瓶颈。一层层向下浏览，即可找到需要优化的目标。</p>
<p data-nodeid="1408"><img src="https://s0.lgstatic.com/i/image/M00/4F/FC/Ciqc1F9htfOAN3G1AEK7W4TM0AU264.gif" alt="2020-08-21 17-07-35.2020-08-21 17_12_29.gif" data-nodeid="1573"></p>
<h3 data-nodeid="1409">优化思路</h3>
<p data-nodeid="1410">对一个普通的 Web 服务来说，我们来看一下，要访问到具体的数据，都要经历哪些主要的环节？</p>
<p data-nodeid="1411">如下图，在浏览器中输入相应的域名，需要通过 DNS 解析到具体的 IP 地址上，为了保证高可用，我们的服务一般都会部署多份，然后使用 Nginx 做反向代理和负载均衡。</p>
<p data-nodeid="1412"><img src="https://s0.lgstatic.com/i/image/M00/4F/FC/Ciqc1F9htgCAcdwGAAIVQmXnOPo885.png" alt="Drawing 4.png" data-nodeid="1579"></p>
<p data-nodeid="1413">Nginx 根据资源的特性，会承担一部分动静分离的功能。其中，动态功能部分，会进入我们的SpringBoot 服务。</p>
<p data-nodeid="1414">SpringBoot 默认使用内嵌的 tomcat 作为 Web 容器，使用典型的 MVC 模式，最终访问到我们的数据。</p>
<h3 data-nodeid="1415">HTTP 优化</h3>
<p data-nodeid="1416">下面我们举例来看一下，哪些动作能够加快网页的获取。为了描述方便，我们仅讨论 HTTP1.1 协议的。</p>
<p data-nodeid="1417"><strong data-nodeid="1587">1.使用 CDN 加速文件获取</strong></p>
<p data-nodeid="1418">比较大的文件，尽量使用 CDN（Content Delivery Network）分发，甚至是一些常用的前端脚本、样式、图片等，都可以放到 CDN 上。CDN 通常能够加快这些文件的获取，网页加载也更加迅速。</p>
<p data-nodeid="1419"><strong data-nodeid="1592">2.合理设置 Cache-Control 值</strong></p>
<p data-nodeid="1420">浏览器会判断 HTTP 头 Cache-Control 的内容，用来决定是否使用浏览器缓存，这在管理一些静态文件的时候，非常有用，相同作用的头信息还有 Expires。Cache-Control 表示多久之后过期；Expires 则表示什么时候过期。</p>
<p data-nodeid="1421">这个参数可以在 Nginx 的配置文件中进行设置。</p>
<pre class="lang-dart" data-nodeid="1422"><code data-language="dart">location ~* ^.+\.(ico|gif|jpg|jpeg|png)$ { 
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;# 缓存<span class="hljs-number">1</span>年
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;add_header Cache-Control: no-cache, max-age=<span class="hljs-number">31536000</span>;
}
</code></pre>
<p data-nodeid="1423"><strong data-nodeid="1598">3.减少单页面请求域名的数量</strong></p>
<p data-nodeid="1424">减少每个页面请求的域名数量，尽量保证在 4 个之内。这是因为，浏览器每次访问后端的资源，都需要先查询一次 DNS，然后找到 DNS 对应的 IP 地址，再进行真正的调用。</p>
<p data-nodeid="1425">DNS 有多层缓存，比如浏览器会缓存一份、本地主机会缓存、ISP 服务商缓存等。从 DNS 到 IP 地址的转变，通常会花费 20-120ms 的时间。减少域名的数量，可加快资源的获取。</p>
<p data-nodeid="1426"><strong data-nodeid="1604">4.开启 gzip</strong></p>
<p data-nodeid="1427">开启 gzip，可以先把内容压缩后，浏览器再进行解压。由于减少了传输的大小，会减少带宽的使用，提高传输效率。</p>
<p data-nodeid="1428">在 nginx 中可以很容易地开启，配置如下：</p>
<pre class="lang-js" data-nodeid="1429"><code data-language="js">gzip on;
gzip_min_length <span class="hljs-number">1</span>k;
gzip_buffers <span class="hljs-number">4</span> <span class="hljs-number">16</span>k;
gzip_comp_level <span class="hljs-number">6</span>;
gzip_http_version <span class="hljs-number">1.1</span>;
gzip_types text/plain application/javascript text/css;
</code></pre>
<p data-nodeid="1430"><strong data-nodeid="1610">5.对资源进行压缩</strong></p>
<p data-nodeid="1431">对 JavaScript 和 CSS，甚至是 HTML 进行压缩。道理类似，现在流行的前后端分离模式，一般都是对这些资源进行压缩的。</p>
<p data-nodeid="1432"><strong data-nodeid="1615">6.使用 keepalive</strong></p>
<p data-nodeid="1433">由于连接的创建和关闭，都需要耗费资源。用户访问我们的服务后，后续也会有更多的互动，所以保持长连接可以显著减少网络交互，提高性能。</p>
<p data-nodeid="1434">nginx 默认开启了对客户端的 keep avlide 支持，你可以通过下面两个参数来调整它的行为。</p>
<pre class="lang-js" data-nodeid="1435"><code data-language="js">http {
 &nbsp; &nbsp;keepalive_timeout &nbsp;<span class="hljs-number">120</span>s <span class="hljs-number">120</span>s;
 &nbsp; &nbsp;keepalive_requests <span class="hljs-number">10000</span>;
}
</code></pre>
<p data-nodeid="1436">nginx 与后端 upstream 的长连接，需要手工开启，参考配置如下：</p>
<pre class="lang-js" data-nodeid="1437"><code data-language="js">location ~ /{ 
 &nbsp; &nbsp; &nbsp; proxy_pass http:<span class="hljs-comment">//backend;</span>
 &nbsp; &nbsp; &nbsp; proxy_http_version <span class="hljs-number">1.1</span>;
 &nbsp; &nbsp; &nbsp; proxy_set_header Connection <span class="hljs-string">""</span>;
}
</code></pre>
<h3 data-nodeid="1438">自定义 Web 容器</h3>
<p data-nodeid="1439">如果你的项目并发量比较高，想要修改最大线程数、最大连接数等配置信息，可以通过自定义Web 容器的方式，代码如下所示。</p>
<pre class="lang-js" data-nodeid="1440"><code data-language="js">@SpringBootApplication(proxyBeanMethods = <span class="hljs-literal">false</span>)
public <span class="hljs-class"><span class="hljs-keyword">class</span> <span class="hljs-title">App</span> <span class="hljs-title">implements</span> <span class="hljs-title">WebServerFactoryCustomizer</span>&lt;<span class="hljs-title">ConfigurableServletWebServerFactory</span>&gt; </span>{
    public <span class="hljs-keyword">static</span> <span class="hljs-keyword">void</span> main(<span class="hljs-built_in">String</span>[] args) {
        SpringApplication.run(PetClinicApplication.class, args);
    }
    @Override
    public <span class="hljs-keyword">void</span> customize(ConfigurableServletWebServerFactory factory) {
        TomcatServletWebServerFactory f = (TomcatServletWebServerFactory) factory;
 &nbsp; &nbsp; &nbsp; &nbsp;f.setProtocol(<span class="hljs-string">"org.apache.coyote.http11.Http11Nio2Protocol"</span>);

        f.addConnectorCustomizers(c -&gt; {
            Http11NioProtocol protocol = (Http11NioProtocol) c.getProtocolHandler();
            protocol.setMaxConnections(<span class="hljs-number">200</span>);
            protocol.setMaxThreads(<span class="hljs-number">200</span>);
            protocol.setSelectorTimeout(<span class="hljs-number">3000</span>);
            protocol.setSessionTimeout(<span class="hljs-number">3000</span>);
            protocol.setConnectionTimeout(<span class="hljs-number">3000</span>);
        });
    }
}
</code></pre>
<p data-nodeid="1441">注意上面的代码，我们设置了它的协议为 org.apache.coyote.http11.Http11Nio2Protocol，意思就是开启了 Nio2。这个参数在 Tomcat 8.0之后才有，开启之后会增加一部分性能。<br>
对比如下（测试项目代码见 <a href="https://gitee.com/xjjdog/tuning-lagou-res/tree/master/tuning-020/spring-petclinic-main" data-nodeid="1626">spring-petclinic-main</a>）：</p>
<p data-nodeid="1442">默认。</p>
<pre class="lang-dart" data-nodeid="1443"><code data-language="dart">[root<span class="hljs-meta">@localhost</span> wrk2-master]# ./wrk -t2 -c100 -d30s -R2000 http:<span class="hljs-comment">//172.16.1.57:8080/owners?lastName=</span>
Running <span class="hljs-number">30</span>s test @ http:<span class="hljs-comment">//172.16.1.57:8080/owners?lastName=</span>
 &nbsp;<span class="hljs-number">2</span> threads and <span class="hljs-number">100</span> connections
  Thread calibration: mean lat.: <span class="hljs-number">4588.131</span>ms, rate sampling interval: <span class="hljs-number">16277</span>ms
  Thread calibration: mean lat.: <span class="hljs-number">4647.927</span>ms, rate sampling interval: <span class="hljs-number">16285</span>ms
  Thread Stats &nbsp; Avg &nbsp; &nbsp;  Stdev &nbsp; &nbsp; Max &nbsp; +/- Stdev
 &nbsp;  Latency &nbsp; &nbsp;<span class="hljs-number">16.49</span>s &nbsp; &nbsp; <span class="hljs-number">4.98</span>s &nbsp; <span class="hljs-number">27.34</span>s &nbsp; &nbsp;<span class="hljs-number">63.90</span>%
 &nbsp;  Req/Sec &nbsp; <span class="hljs-number">106.50</span> &nbsp; &nbsp; &nbsp;<span class="hljs-number">1.50</span> &nbsp; <span class="hljs-number">108.00</span> &nbsp; &nbsp;<span class="hljs-number">100.00</span>%
 &nbsp;<span class="hljs-number">6471</span> requests <span class="hljs-keyword">in</span> <span class="hljs-number">30.03</span>s, <span class="hljs-number">39.31</span>MB read
  Socket errors: connect <span class="hljs-number">0</span>, read <span class="hljs-number">0</span>, write <span class="hljs-number">0</span>, timeout <span class="hljs-number">60</span>
Requests/sec: &nbsp; &nbsp;<span class="hljs-number">215.51</span>
Transfer/sec: &nbsp; &nbsp; &nbsp;<span class="hljs-number">1.31</span>MB
</code></pre>
<p data-nodeid="1444">Nio2。</p>
<pre class="lang-dart" data-nodeid="1445"><code data-language="dart">[root<span class="hljs-meta">@localhost</span> wrk2-master]# ./wrk -t2 -c100 -d30s -R2000 http:<span class="hljs-comment">//172.16.1.57:8080/owners?lastName=</span>
Running <span class="hljs-number">30</span>s test @ http:<span class="hljs-comment">//172.16.1.57:8080/owners?lastName=</span>
 &nbsp;<span class="hljs-number">2</span> threads and <span class="hljs-number">100</span> connections
  Thread calibration: mean lat.: <span class="hljs-number">4358.805</span>ms, rate sampling interval: <span class="hljs-number">15835</span>ms
  Thread calibration: mean lat.: <span class="hljs-number">4622.087</span>ms, rate sampling interval: <span class="hljs-number">16293</span>ms
  Thread Stats &nbsp; Avg &nbsp; &nbsp;  Stdev &nbsp; &nbsp; Max &nbsp; +/- Stdev
 &nbsp;  Latency &nbsp; &nbsp;<span class="hljs-number">17.47</span>s &nbsp; &nbsp; <span class="hljs-number">4.98</span>s &nbsp; <span class="hljs-number">26.90</span>s &nbsp; &nbsp;<span class="hljs-number">57.69</span>%
 &nbsp;  Req/Sec &nbsp; <span class="hljs-number">125.50</span> &nbsp; &nbsp; &nbsp;<span class="hljs-number">2.50</span> &nbsp; <span class="hljs-number">128.00</span> &nbsp; &nbsp;<span class="hljs-number">100.00</span>%
 &nbsp;<span class="hljs-number">7469</span> requests <span class="hljs-keyword">in</span> <span class="hljs-number">30.04</span>s, <span class="hljs-number">45.38</span>MB read
  Socket errors: connect <span class="hljs-number">0</span>, read <span class="hljs-number">0</span>, write <span class="hljs-number">0</span>, timeout <span class="hljs-number">4</span>
Requests/sec: &nbsp; &nbsp;<span class="hljs-number">248.64</span>
Transfer/sec: &nbsp; &nbsp; &nbsp;<span class="hljs-number">1.51</span>MB
</code></pre>
<p data-nodeid="1446">你甚至可以将 tomcat 替换成 undertow。undertow 也是一个 Web 容器，更加轻量级一些，占用的内存更少，启动的守护进程也更少，更改方式如下：</p>
<pre class="lang-js" data-nodeid="1447"><code data-language="js">&lt;dependency&gt;
 &nbsp; &nbsp; &nbsp;<span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.springframework.boot<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span></span>
 &nbsp; &nbsp; &nbsp;<span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>spring-boot-starter-web<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span></span>
 &nbsp; &nbsp; &nbsp;<span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">exclusions</span>&gt;</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">exclusion</span>&gt;</span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.springframework.boot<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span>
 &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>spring-boot-starter-tomcat<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span>
 &nbsp; &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;/<span class="hljs-name">exclusion</span>&gt;</span>
 &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;/<span class="hljs-name">exclusions</span>&gt;</span></span>
 &nbsp; &nbsp;&lt;/dependency&gt;
 &nbsp; &nbsp;<span class="xml"><span class="hljs-tag">&lt;<span class="hljs-name">dependency</span>&gt;</span>
 &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">groupId</span>&gt;</span>org.springframework.boot<span class="hljs-tag">&lt;/<span class="hljs-name">groupId</span>&gt;</span>
 &nbsp; &nbsp; &nbsp;<span class="hljs-tag">&lt;<span class="hljs-name">artifactId</span>&gt;</span>spring-boot-starter-undertow<span class="hljs-tag">&lt;/<span class="hljs-name">artifactId</span>&gt;</span>
 &nbsp; &nbsp;<span class="hljs-tag">&lt;/<span class="hljs-name">dependency</span>&gt;</span></span>
</code></pre>
<p data-nodeid="1448">其实，对于 tomcat 优化最为有效的，还是 JVM 参数的配置，你可以参考上一课时的内容进行调整。<br>
比如，使用下面的参数启动，QPS 由 248 上升到 308。</p>
<pre class="lang-js" data-nodeid="1449"><code data-language="js">-XX:+UseG1GC -Xmx2048m -Xms2048m -XX:+AlwaysPreTouch
</code></pre>
<h3 data-nodeid="1450">Skywalking</h3>
<p data-nodeid="1451">对于一个 web 服务来说，最缓慢的地方就在于数据库操作。所以，使用“07 | 案例分析：无处不在的缓存，高并发系统的法宝”和“08 | 案例分析：Redis 如何助力秒杀业务”提供的本地缓存和分布式缓存优化，能够获得最大的性能提升。</p>
<p data-nodeid="1452">对于如何定位到复杂分布式环境中的问题，我这里想要分享另外一个工具：Skywalking。</p>
<p data-nodeid="1453">Skywalking 是使用探针技术（JavaAgent）来实现的。通过在 Java 的启动参数中，加入 javaagent 的 Jar 包，即可将性能数据和调用链数据封装，并发送到 Skywalking 的服务器。</p>
<p data-nodeid="1454">下载相应的安装包（如果使用 ES 存储，需要下载专用的安装包），配置好存储之后，即可一键启动。</p>
<p data-nodeid="1455">将 agent 的压缩包，解压到相应的目录。</p>
<pre class="lang-js" data-nodeid="1456"><code data-language="js">tar xvf skywalking-agent.tar.gz &nbsp;-C /opt/
</code></pre>
<p data-nodeid="1457">在业务启动参数中加入 agent 的包。比如，原来的启动命令是：</p>
<pre class="lang-dart" data-nodeid="1458"><code data-language="dart">java &nbsp;-jar /opt/test-service/spring-boot-demo.jar &nbsp;--spring.profiles.active=dev
</code></pre>
<p data-nodeid="1459">改造后的启动命令是：</p>
<pre class="lang-js" data-nodeid="1460"><code data-language="js">java -javaagent:<span class="hljs-regexp">/opt/</span>skywalking-agent/skywalking-agent.jar -Dskywalking.agent.service_name=the-demo-name &nbsp;-jar /opt/test-service/spring-boot-demo.ja &nbsp;--spring.profiles.active=dev
</code></pre>
<p data-nodeid="1461">访问一些服务的链接，打开 Skywalking 的 UI，即可看到下图的界面。这些指标可以类比“01 | 理论分析：性能优化，有哪些衡量指标？需要注意什么？”提到的衡量指标去理解，我们就可以从图中找到响应比较慢 QPS 又比较高的接口，进行专项优化。</p>
<p data-nodeid="1462"><img src="https://s0.lgstatic.com/i/image/M00/4F/FD/Ciqc1F9htwyARKqMAAgxG3QYe8A553.png" alt="Drawing 5.png" data-nodeid="1651"></p>
<h3 data-nodeid="1463">各个层次的优化方向</h3>
<h4 data-nodeid="1464">1.Controller 层</h4>
<p data-nodeid="1465">controller 层用于接收前端的查询参数，然后构造查询结果。现在很多项目都采用前后端分离的架构，所以 controller 层的方法，一般会使用 @ResponseBody 注解，把查询的结果，解析成 JSON 数据返回（兼顾效率和可读性）。</p>
<p data-nodeid="1466">由于 controller 只是充当了一个类似功能组合和路由的角色，所以这部分对性能的影响就主要体现在数据集的大小上。如果结果集合非常大，JSON 解析组件就要花费较多的时间进行解析，</p>
<p data-nodeid="1467">大结果集不仅会影响解析时间，还会造成内存浪费。</p>
<p data-nodeid="1468">假如结果集在解析成 JSON 之前，占用的内存是 10MB，那么在解析过程中，有可能会使用 20M 或者更多的内存去做这个工作。</p>
<p data-nodeid="1469">我见过很多案例，由于返回对象的嵌套层次太深、引用了不该引用的对象（比如非常大的 byte[] 对象），造成了内存使用的飙升。</p>
<p data-nodeid="1470">所以，<strong data-nodeid="1667">对于一般的服务，保持结果集的精简，是非常有必要的</strong>，这也是 DTO（data transfer object）存在的必要。如果你的项目，返回的结果结构比较复杂，对结果集进行一次转换是非常有必要的。</p>
<h4 data-nodeid="1471">2.Service 层</h4>
<p data-nodeid="2425" class="">service 层用于处理具体的业务，大部分功能需求都是在这里完成的。service 层一般是使用单例模式，很少会保存状态，而且可以被 controller 复用。</p>


<p data-nodeid="1473">service 层的代码组织，对代码的可读性、性能影响都比较大。我们常说的设计模式，大多数都是针对 service 层来说的。</p>
<p data-nodeid="1474">service 层会频繁使用更底层的资源，通过组合的方式获取我们所需要的数据，大多数可以通过我们前面课时提供的优化思路进行优化。</p>
<p data-nodeid="1475">这里要着重提到的一点，就是分布式事务。</p>
<p data-nodeid="1476"><img src="https://s0.lgstatic.com/i/image/M00/50/08/CgqCHl9htvaAf1S-AAKlZCq3SXg275.png" alt="Drawing 6.png" data-nodeid="1675"></p>
<p data-nodeid="1477">如上图，四个操作分散在三个不同的资源中。要想达到一致性，需要三个不同的资源 MySQL、MQ、ElasticSearch 进行统一协调。它们底层的协议，以及实现方式，都是不一样的，那就无法通过 Spring 提供的 Transaction 注解来解决，需要借助外部的组件来完成。</p>
<p data-nodeid="1478">很多人都体验过，加入了一些保证一致性的代码，一压测，性能掉的惊掉下巴。分布式事务是性能杀手，因为它要使用额外的步骤去保证一致性，常用的方法有：两阶段提交方案、TCC、本地消息表、MQ 事务消息、分布式事务中间件等。</p>
<p data-nodeid="1479"><img src="https://s0.lgstatic.com/i/image/M00/4F/FD/Ciqc1F9htx6ADeh6AAFoqvxy4eM753.png" alt="Drawing 7.png" data-nodeid="1680"></p>
<p data-nodeid="1480">如上图，分布式事务要在改造成本、性能、时效等方面进行综合考虑。有一个介于分布式事务和非事务之间的名词，叫作<strong data-nodeid="1686">柔性事务</strong>。柔性事务的理念是将业务逻辑和互斥操作，从资源层上移至业务层面。</p>
<p data-nodeid="1481"><strong data-nodeid="1690">关于传统事务和柔性事务，我们来简单比较一下。</strong></p>
<p data-nodeid="1482"><strong data-nodeid="1694">ACID</strong></p>
<p data-nodeid="1483">关系数据库, 最大的特点就是事务处理, 即满足 ACID。</p>
<ul data-nodeid="1484">
<li data-nodeid="1485">
<p data-nodeid="1486">原子性（Atomicity）：事务中的操作要么都做，要么都不做。</p>
</li>
<li data-nodeid="1487">
<p data-nodeid="1488">一致性（Consistency）：系统必须始终处在强一致状态下。</p>
</li>
<li data-nodeid="1489">
<p data-nodeid="1490">隔离性（Isolation）：一个事务的执行不能被其他事务所干扰。</p>
</li>
<li data-nodeid="1491">
<p data-nodeid="1492">持久性（Durability）：一个已提交的事务对数据库中数据的改变是永久性的。</p>
</li>
</ul>
<p data-nodeid="1493"><strong data-nodeid="1703">BASE</strong></p>
<p data-nodeid="1494">BASE 方法通过牺牲一致性和孤立性来提高可用性和系统性能。</p>
<p data-nodeid="1495">BASE 为 Basically Available、Soft-state、Eventually consistent 三者的缩写，其中 BASE 分别代表：</p>
<ul data-nodeid="1496">
<li data-nodeid="1497">
<p data-nodeid="1498">基本可用（Basically Available）：系统能够基本运行、一直提供服务。</p>
</li>
<li data-nodeid="1499">
<p data-nodeid="1500">软状态（Soft-state）：系统不要求一直保持强一致状态。</p>
</li>
<li data-nodeid="1501">
<p data-nodeid="1502">最终一致性（Eventual consistency）：系统需要在某一时刻后达到一致性要求。</p>
</li>
</ul>
<p data-nodeid="1503">互联网业务，推荐使用补偿事务，完成最终一致性。比如，通过一系列的定时任务，完成对数据的修复。</p>
<h4 data-nodeid="1504">3.Dao 层</h4>
<p data-nodeid="1505">经过合理的数据缓存，我们都会尽量避免请求穿透到 Dao 层。除非你对 ORM 本身提供的缓存特性特别的熟悉；否则，都推荐你使用更加通用的方式去缓存数据。</p>
<p data-nodeid="1506">Dao 层，主要在于对 ORM 框架的使用上。比如，在 JPA 中，如果加了一对多或者多对多的映射关系，而又没有开启懒加载，级联查询的时候就容易造成深层次的检索，造成了内存开销大、执行缓慢的后果。</p>
<p data-nodeid="1507">在一些数据量比较大的业务中，多采用分库分表的方式。在这些分库分表组件中，很多简单的查询语句，都会被重新解析后分散到各个节点进行运算，最后进行结果合并。</p>
<p data-nodeid="1508">举个例子，select count(*) from a 这句简单的 count 语句，就可能将请求路由到十几张表中去运算，最后在协调节点进行统计，执行效率是可想而知的。目前，分库分表中间件，比较有代表性的是驱动层的 ShardingJdbc 和代理层的 MyCat，它们都有这样的问题。这些组件提供给使用者的视图是一致的，但我们在编码的时候，一定要注意这些区别。</p>
<h3 data-nodeid="1509">小结</h3>
<p data-nodeid="1510">下面我们来总结一下。</p>
<p data-nodeid="1511">本课时，我们简单看了一下 SpringBoot 常见的优化思路，然后介绍了三个新的性能分析工具。</p>
<ul data-nodeid="1512">
<li data-nodeid="1513">
<p data-nodeid="1514">一个是监控系统 Prometheus，可以看到一些具体的指标大小；</p>
</li>
<li data-nodeid="1515">
<p data-nodeid="1516">一个是火焰图，可以看到具体的代码热点；</p>
</li>
<li data-nodeid="1517">
<p data-nodeid="1518">一个是 Skywalking，可以分析分布式环境中的调用链。</p>
</li>
</ul>
<p data-nodeid="1519">SpringBoot 自身的 Web 容器是 Tomcat，那我们就可以通过对 Tomcat 的调优来获取性能提升。当然，对于服务上层的负载均衡 Nginx，我们也提供了一系列的优化思路。</p>
<p data-nodeid="1520">最后，我们看了在经典的 MVC 架构下，Controller、Service、Dao 的一些优化方向，并着重看了 Service 层的分布式事务问题。</p>
<p data-nodeid="1521">SpringBoot 作为一个广泛应用的服务框架，在性能优化方面已经做了很多工作，选用了很多高速组件。比如，数据库连接池默认使用 hikaricp，Redis 缓存框架默认使用 lettuce，本地缓存提供 caffeine 等。对于一个普通的数据库交互的 Web 服务来说，缓存是最主要的优化手段。</p>
<p data-nodeid="1522">但细节决定成败，05-19 课时的内容对性能优化都有借鉴意义。下一课时（也就是咱们专栏的最后一课时），我将从问题发现、目标制定、优化方式上进行整体性的总结。</p>
<p data-nodeid="1523" class=""><a href="https://wj.qq.com/s2/7200077/1134/" data-nodeid="1730">课程评价入口，挑选 5 名小伙伴赠送小礼品~</a></p>

---

### 精选评论

##### *斌：
> 视频中，service层默认是单例模式（prototype），单例应该是singleton，在springboot中，@Component， @Service，@Controller默认都是单例的，prototype是每次调用都会new一个新的对象。

 ###### &nbsp;&nbsp;&nbsp; 编辑回复：
> &nbsp;&nbsp;&nbsp; 感谢指错，已更正

