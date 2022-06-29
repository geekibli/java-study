<p data-nodeid="7181">Nginx 是后端工程师和运维工程师，以及前端工程师必须要掌握的必备技能，尤其在分布式系统应用越来越广泛的今天，Nginx 已经占据了 Web 服务器的大壁江山，并且还在不断地增长，比如国内的 BATJ、网易、新浪等公司都可以看到它的身影。</p>
<p data-nodeid="7182">我们本课时的面试题是，Nginx 的负载均衡模式有哪些？它的实现原理是什么？</p>
<h3 data-nodeid="7183">典型回答</h3>
<p data-nodeid="7184">在正式开始之前，我们先来了解一下什么是 Nginx？</p>
<p data-nodeid="8668" class="">Nginx 是一款开源的高性能轻量级 <strong data-nodeid="8686">Web 服务器</strong>（也叫 <strong data-nodeid="8687">HTTP 服务器</strong>），它主要提供的功能是：<strong data-nodeid="8688">反向代理、负载均衡</strong>和<strong data-nodeid="8689">HTTP 缓存</strong>。它于 2004 年首次公开发布，2011 年成立同名公司以提供支持，2019 年 3 月被 F5 Networks 以 6.7 亿美元收购。</p>

<p data-nodeid="9522">之所以需要使用负载均衡是因为，如果我们使用的是一台服务器，那么在高峰期时很多用户就需要排队等待系统响应，因为一台服务器能处理的并发数是固定的。例如，一个 Tomcat 在默认情况下只能开启 150 个线程（Tomcat 8.5.x 版本）来处理并发任务，如果并发数超过了最大线程数，那么新来的请求就只能排队等待处理了，如下图所示：</p>
<p data-nodeid="9523" class=""><img src="https://s0.lgstatic.com/i/image/M00/2A/5F/Ciqc1F78TvOALCJrAABv_aXOv8c313.png" alt="image (1).png" data-nodeid="9531"></p>


<p data-nodeid="10380">然而如果有负载均衡的话，我们就可以将所有的请求分配到不同的服务器上。假如 1 台服务器可以处理 2000 个请求，那么 5 台服务器就可以处理 10000 个请求了，这样就大大提高了系统处理业务的能力，如下图所示：</p>
<p data-nodeid="10381" class=""><img src="https://s0.lgstatic.com/i/image/M00/2A/5F/Ciqc1F78TvuAMPOiAAHBUceQ_F8585.png" alt="5.png" data-nodeid="10385"></p>


<p data-nodeid="7190">知道了负载均衡的好处之后，我们来看下 Nginx 负载均衡的功能。</p>
<p data-nodeid="7191">Nginx 主要的负载均衡策略（内置的负载均衡）有以下四种：</p>
<ul data-nodeid="7192">
<li data-nodeid="7193">
<p data-nodeid="7194">轮询策略（默认负载均衡策略）</p>
</li>
<li data-nodeid="7195">
<p data-nodeid="7196">最少连接数负载均衡策略</p>
</li>
<li data-nodeid="7197">
<p data-nodeid="7198">ip-hash 负载均衡策略</p>
</li>
<li data-nodeid="7199">
<p data-nodeid="7200">权重负载均衡策略</p>
</li>
</ul>
<h4 data-nodeid="7201">1. 轮询策略</h4>
<p data-nodeid="7202">轮询负载策略是指每次将请求按顺序轮流发送至相应的服务器上，它的配置示例如下所示：</p>
<pre class="lang-java" data-nodeid="11029"><code data-language="java">http {
    upstream myapp1 {
        server srv1.example.com;
        server srv2.example.com;
        server srv3.example.com;
    }
    server {
        listen <span class="hljs-number">80</span>;
        location / {
            proxy_pass http:<span class="hljs-comment">//myapp1;</span>
        }
    }
}
</code></pre>


<p data-nodeid="7204">在以上实例中，当我们使用“ip:80/”访问时，请求就会轮询的发送至上面配置的三台服务器上。<br>
Nginx 可以实现 HTTP、HTTPS、FastCGI、uwsgi、SCGI、memcached 和 gRPC 的负载均衡。</p>
<h4 data-nodeid="7205">2. 最少连接数负载均衡</h4>
<p data-nodeid="7206">此策略是指每次将请求分发到当前连接数最少的服务器上，也就是 Nginx 会将请求试图转发给相对空闲的服务器以实现负载平衡，它的配置示例如下：</p>
<pre class="lang-java" data-nodeid="27546"><code data-language="java">upstream myapp1 {
    least_conn;
    server srv1.example.com;
    server srv2.example.com;
    server srv3.example.com;
}
</code></pre>







































<h4 data-nodeid="7208">3. 加权负载均衡</h4>
<p data-nodeid="7209">此配置方式是指每次会按照服务器配置的权重进行请求分发，权重高的服务器会收到更多的请求，这就相当于给 Nginx 在请求分发时加了一个参考的权重选项，并且这个权重值是可以人工配置的。因此我们就可以将硬件配置高，以及并发能力强的服务器的权重设置高一点，以更合理地利用服务器的资源，它配置示例如下：</p>
<pre class="lang-java" data-nodeid="27975"><code data-language="java">upstream myapp1 {
    server srv1.example.com weight=<span class="hljs-number">3</span>;
    server srv2.example.com;
    server srv3.example.com;
}
</code></pre>

<p data-nodeid="7211">以上配置表示，5 次请求中有 3 次请求会分发给 srv1，1 次请求会分发给 srv2，另外 1 次请求会分发给 srv3。</p>
<h4 data-nodeid="7212">4. ip-hash 负载均衡</h4>
<p data-nodeid="7213">以上三种负载均衡的配置策略都不能保证将每个客户端的请求固定的分配到一台服务器上。假如用户的登录信息是保存在单台服务器上的，而不是保存在类似于 Redis 这样的第三方中间件上时，如果不能将每个客户端的请求固定的分配到一台服务器上，就会导致用户的登录信息丢失。因此用户在每次请求服务器时都需要进行登录验证，这样显然是不合理的，也是不能被用户所接受的，所以在特殊情况下我们就需要使用 ip-hash 的负载均衡策略。</p>
<p data-nodeid="7214">ip-hash 负载均衡策略可以根据客户端的 IP，将其固定的分配到相应的服务器上，它的配置示例如下：</p>
<pre class="lang-java" data-nodeid="28404"><code data-language="java">upstream myapp1 {
    ip_hash;
    server srv1.example.com;
    server srv2.example.com;
    server srv3.example.com;
}
</code></pre>

<p data-nodeid="7216">Nginx 的实现原理是，首先客户端通过访问域名地址发出 HTTP 请求，访问的域名会被 DNS 服务器解析为 Nginx 的 IP 地址，然后将请求转发至 Nginx 服务器，Nginx 接收到请求之后会通过 URL 地址和负载均衡的配置，匹配到配置的代理服务器，然后将请求转发给代理服务器，代理服务器拿到请求之后将处理结果返回给 Nginx，Nginx 再将结果返回给客户端，这样就完成了一次正常的 HTTP 交互。</p>
<h3 data-nodeid="7217">考点分析</h3>
<p data-nodeid="7218">负载均衡和缓存功能是 Nginx 最常用的两个功能，这两个功能都属于高性能的调优手段，也和后端人员的关系比较密切，只有了解并会使用它们才能更好地调试和运行自己的项目，因此 Nginx 的相关知识几乎是面试中都会出现。</p>
<p data-nodeid="7219">和此知识点相关的面试题还有以下这些：</p>
<ul data-nodeid="7220">
<li data-nodeid="7221">
<p data-nodeid="7222">如果代理的服务器宕机了 Nginx 会如何处理？</p>
</li>
<li data-nodeid="7223">
<p data-nodeid="7224">Nginx 的缓存功能是如何使用的？</p>
</li>
</ul>
<h3 data-nodeid="7225">知识扩展</h3>
<h4 data-nodeid="7226">健康检测</h4>
<p data-nodeid="7227">被代理的服务器出现宕机的情况，如果被 Nginx 发现，那么 Nginx 就会将其自动标识为不可用，并且在一段时间内会禁止入站的请求访问到该服务器上。</p>
<p data-nodeid="7228">而这个发现服务器宕机的过程就是健康检测的功能了。Nginx 的健康检测分为两种类型，<strong data-nodeid="7315">主动检测和被动检测</strong>，默认的非商用 Nginx 采用的是被动检测。</p>
<p data-nodeid="7229">所谓的被动检测是指只有访问了该服务器之后发现服务器不可用了，才会将其标识为不可用，并且在一定时间内禁止请求分发到该服务器上，而不是主动以一定的频率去检查服务器是否可用。</p>
<p data-nodeid="7230">健康检测有两个重要参数 <strong data-nodeid="7330">max_fails</strong> 和 <strong data-nodeid="7331">fail_timeout</strong>。</p>
<p data-nodeid="7231">fail_timeout 定义了健康检查的执行时长，而 max_fails 表示服务不可用的最大尝试次数，当一定时间内（此时间由 fail_timeout 定义），发生了一定次数的服务器不响应的事件（此次数由 max_fails 定义），那么 Nginx 就会将该服务器标识为不可用的服务器，并且在一定时间内禁止请求分发到该服务器。默认情况下 max_fails 设置为 1，当它设置为 0 时表示禁用此服务器的运行状况检查，它的配置示例如下：</p>
<pre class="lang-java" data-nodeid="28833"><code data-language="java">upstream cluster{
    server srv1.example.com max_fails=<span class="hljs-number">2</span> fail_timeout=<span class="hljs-number">10</span>s;
    server srv2.example.com max_fails=<span class="hljs-number">2</span> fail_timeout=<span class="hljs-number">10</span>s;
}
</code></pre>

<p data-nodeid="7233">以上配置表示，如果 10s 内发生了两次服务不可用的情况就会将该服务器标识为不可用的状态。<br>
当服务器被标识为不可用时，只有达到了 fail_timeout 定义的时间后，才会进行再一次的健康请求检测。</p>
<p data-nodeid="7234">而主动健康检测的实现方案有两种，一种是使用商用的 Nginx Plus 来配置主动健康检测，另一种是使用开源的第三方模块 nginx_upstream_check_module 来实现主动健康检测。</p>
<p data-nodeid="7235">Nginx Plus 和 nginx_upstream_check_module 模块的主动健康检查配置大体都是一样的，它的配置示例如下：</p>
<pre class="lang-java" data-nodeid="29262"><code data-language="java">upstream backend {
    server srv1.example.com;
    server srv2.example.com;
    check interval=<span class="hljs-number">3000</span> rise=<span class="hljs-number">1</span> fall=<span class="hljs-number">3</span> timeout=<span class="hljs-number">2000</span> type=http;
    check_http_send <span class="hljs-string">"HEAD /status HTTP/1.0\r\n\r\n"</span>;
    check_http_expect_alive http_2xx http_3xx;
}
</code></pre>

<p data-nodeid="7237">其中，check_http_send 表示发送请求的内容，而 check_http_expect_alive 是服务器正常情况下的响应状态码，如果后端服务器的响应状态包含在此配置中，则说明是健康的状态。</p>
<h4 data-nodeid="7238">Nginx 缓存</h4>
<p data-nodeid="7239">我们可以开启 Nginx 的静态资源缓存，将一些不变的静态文件，比如图片、CSS、JS 等文件进行缓存，这样在客户端访问这些资源时就不用去访问服务器了，因此响应的速度就可以大幅提升，并且节省了宝贵的服务器资源。</p>
<p data-nodeid="7240">Nginx 开启缓存需要在 http 节点中配置 proxy_cache_path 信息，以及 server 节点中配置要缓存资源的后缀名，它的配置示例如下：</p>
<pre class="lang-dart te-preview-highlight" data-nodeid="32694"><code data-language="dart">http {
  <span class="hljs-comment">// 忽略其他的配置信息......</span>
  proxy_cache_path  /data/cache levels=<span class="hljs-number">1</span>:<span class="hljs-number">2</span> keys_zone=nuget-cache:<span class="hljs-number">20</span>m max_size=<span class="hljs-number">50</span>g inactive=<span class="hljs-number">1</span>d;
  include nginx_proxy.conf;
  server {
    listen  <span class="hljs-number">80</span>;
    server_name  srv1.example.com;    
    location ~ .*\.(gif|jpg|png|css|js)(.*) { # 要缓存的文件的后缀
      access_log off;
      add_header Cache-Control <span class="hljs-string">"public,max-age=24*3600"</span>;
      proxy_pass http:<span class="hljs-comment">//localhost:8080;</span>
    }
  }
}
</code></pre>








<p data-nodeid="7242">其中，proxy_cache_path 配置的是缓存的目录信息，以及缓存的保存时间 inactive，还有缓存的大小等信息；而“access_log off”表示关闭日志功能，proxy_pass 表示当第一次没有缓存时的请求地址，之后便会将访问到的资源缓存起来。</p>
<h3 data-nodeid="7243">小结</h3>
<p data-nodeid="7244">本课时我们介绍了 Nginx，并讲了 Nginx 的四种内置负载均衡的执行策略：轮询策略（默认负载均衡策略）、最少连接数负载均衡策略、ip-hash 负载均衡策略和权重负载均衡策略，其中 ip-hash 的负载均衡策略会将客户端的请求固定分发到一台服务器上。</p>
<p data-nodeid="7245">后面我们还介绍了 Nginx 的健康检测：主动健康检测和被动健康检测；最后我们还讲了 Nginx 的缓存功能，它可以帮我们更快的访问到静态资源。</p>
<p data-nodeid="7246">学完本课时后，相信你对 Nginx 已经有了一个大体的认识，其中面试中被问到最多的知识点是 Nginx 的四种负载均衡以及健康检查的相关内容。</p>

---

### 精选评论

##### *磊：
> nginx 还提供第三方模块">consistent_hash 支持一致性哈希负载均衡

##### **润：
> NGINX的缓存静态文件的功能已经基本被cdn取代了

 ###### &nbsp;&nbsp;&nbsp; 讲师回复：
> &nbsp;&nbsp;&nbsp; 对于大公司和大项目来说 CDN 是不错的选择，对于小型项目来说可以凑活的用 Nginx 的静态文件缓存。

##### **杰：
> 学到了

