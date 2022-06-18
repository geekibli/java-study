# cookie 和 session的区别

![](https://oscimg.oschina.net/oscnet/up-e39e5d66b6a1fed0baff224083c963e164a.png)


## Cookie

![](https://oscimg.oschina.net/oscnet/up-96cd72ceb402f12e7d22d2f3d537aa8bc26.png)

## Session

![](https://oscimg.oschina.net/oscnet/up-bb31e2801fcef205dd803b0a867990406dd.png)


### Session实现原理

![](https://oscimg.oschina.net/oscnet/up-c3a48a576722d6497b78e80e276a63706f3.png)


Session功能实现需要使用cookie，需要cookie中存储sessionId。




## 总结

- cookie: 在客户端保存数据，不安全，只能保存字符串，且是少量数据
- session: 保存在服务端，安全，可以保存对象，数据无限制


## 如果cookie被用户禁用了怎么办？

可以使用URL地址重写是对客户端不支持Cookie的解决方案。

URL地址重写的原理是将改用户的session的id信息重写到url地址中，服务器能够解析重写后的url获取session的id，这样即使客户端不支持Cookie,也可以使用session来记录用户状态。


## 结合项目使用

- 判断用户是否登录过网站，一遍下次登录时能够直接登录，如果我们删除cookie，则每次登录必须重新填写登录的相关信息
- 还有一个“购物车“的原理，用户可能在一段时间内在同一家网站的不同页面选择不同的商品，可以将这些信息都写入cookie，在最后付款时从cookie中提取这些信息。