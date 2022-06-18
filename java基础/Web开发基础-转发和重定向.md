# 转发和重定向


1. 重定向时**浏览器**发送请求并收到响应请求之后再一次想新地址发送请求，转发时**服务器**收到请求后为了完成响应转到另一个资源。
2. 重定向中有两次请求对象，不共享数据；转发之产生一次请求对象且在组件之间共享数据
3. 重定向后地址栏会改变而转发不会改变
4. 重定向的新地址可以是任意地址，转发必须时同一个应用内的某个资源


## 获取servlet的转发和响应重定向的方式

![](https://oscimg.oschina.net/oscnet/up-05de0c3e52276d6b1e1842b6b02199a2c65.png)

转发的方法：
1. 通过HttpServletRequest的getRequestDispatcher()方法获得
2. 通过ServletContext的getRequestDispatcher()方法获得重定向的方法，httpServletRespinse的sendRedirect()方法

![](https://oscimg.oschina.net/oscnet/up-3d3cb8a5d6509a74c9d8209473985501f52.png)

重定向的方法：
httpServletResponse的sendRedirect()方法


