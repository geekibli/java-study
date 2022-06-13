---
title: Spring加载配置文件原理
toc: true
date: 2021-07-28 17:33:48
tags: Spring
categories: [Spring Family , Spring Framework]
---


# Spring如何加载配置文件到应用程序

## 加载Xml文件配置，获取对象
> xml文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
    
    <bean id="user" class="com.ibli.javaBase.reflection.User">
        <property name="age" value="12"/>
        <property name="name" value="gaolei"/>
        <property name="sex" value="male"/>
    </bean>
    
</beans>
```

> 测试类
```java
public class IocDemo {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("spring-ioc.xml");
        User user = (User) ac.getBean("user");
        System.out.println(user);
    }
}
```

## Spring 加载Xml文件流程

> 首先猜想一下宏观的流程

<img src="https://oscimg.oschina.net/oscnet/up-6aaea943c32e32e44e672c1850975bd40ae.png">

我们可以大体猜想流程是什么样的，如下👇

<img src="https://oscimg.oschina.net/oscnet/up-992d3c9c618a021852b873f08b756824901.png">

接下来debug源码看一下具体流程：

> ClassPathXmlApplicationContext调用refresh方法

```java
public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent) throws BeansException {
        super(parent);
        this.setConfigLocations(configLocations);
        if (refresh) {
            // Spring 启动入口
            this.refresh();
        }
    }
```
   Spring 启动入口 this.refresh(); 👆


> 调用AbstractRefreshableApplicationContext下的refreshBeanFactory

org.springframework.context.support.AbstractRefreshableApplicationContext#refreshBeanFactory
```java
 protected final void refreshBeanFactory() throws BeansException {
        if (this.hasBeanFactory()) {
            this.destroyBeans();
            this.closeBeanFactory();
        }

        try {
            DefaultListableBeanFactory beanFactory = this.createBeanFactory();
            beanFactory.setSerializationId(this.getId());
            this.customizeBeanFactory(beanFactory);
            // 从这里进入下一步 👇
            this.loadBeanDefinitions(beanFactory);
            synchronized(this.beanFactoryMonitor) {
                this.beanFactory = beanFactory;
            }
        } catch (IOException var5) {
            throw new ApplicationContextException("I/O error parsing bean definition source for " + this.getDisplayName(), var5);
        }
    }

```
<font color=red>关键方法是this.loadBeanDefinitions(beanFactory);</font>

>  找到XmlBeanDefinitionReader 这是读取配置的关键所在

关键对象 <font color=red>XmlBeanDefinitionReader</font> 这个在 「梳理Spring启动脉络」中提到了，Spring提供的抽象接口！
```java
 protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.setEnvironment(this.getEnvironment());
        beanDefinitionReader.setResourceLoader(this);
        beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
        // 初始化beanDefinitionReader对象
        this.initBeanDefinitionReader(beanDefinitionReader);
        // 加载配置文件 获得BeanDefinitions
        this.loadBeanDefinitions(beanDefinitionReader);
    }
```

> 继续调用 loadBeanDefinitions 这个有很多重载方法，一直点下去就行！

```java
protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
        Resource[] configResources = this.getConfigResources();
        if (configResources != null) {
            reader.loadBeanDefinitions(configResources);
        }
       
        String[] configLocations = this.getConfigLocations();
         //spring-ioc.xml
        if (configLocations != null) {
            reader.loadBeanDefinitions(configLocations);
        }
    }

```
configLocations 就是我们Xml配置文件的路径

> 接下来一直调用loadBeanDefinitions方法 直到这一步 👇

org.springframework.beans.factory.xml.XmlBeanDefinitionReader#loadBeanDefinitions(org.springframework.core.io.support.EncodedResource)

```java
try {
                InputStream inputStream = encodedResource.getResource().getInputStream();
                Throwable var4 = null;

                try {
                    InputSource inputSource = new InputSource(inputStream);
                    if (encodedResource.getEncoding() != null) {
                        inputSource.setEncoding(encodedResource.getEncoding());
                    }

                    var6 = this.doLoadBeanDefinitions(inputSource, encodedResource.getResource());
                } catch (Throwable var24) {
                    var4 = var24;
                    throw var24;
                } finally {
                    if (inputStream != null) {
                        if (var4 != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable var23) {
                                var4.addSuppressed(var23);
                            }
                        } else {
                            inputStream.close();
                        }
                    }

                }
            }
```

这里看到 <font color=red >nputStream</font> 很明显，这里是通过IO流读取制定位置的文件的 !


>  获取到文件输入流之后，将输入流转换成Document文件去解析

protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeanDefinitionStoreException 
```java
// 转换成Document的关键方法
Document doc = this.doLoadDocument(inputSource, resource);
```


> 调用doRegisterBeanDefinitions方法

org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#doRegisterBeanDefinitions
调用parseBeanDefinitions方法去解析数据

> 调用DefaultBeanDefinitionDocumentReader的parseBeanDefinitions方法 来解析Element

org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#parseBeanDefinitions

```java
 protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
        if (delegate.isDefaultNamespace(root)) {
            NodeList nl = root.getChildNodes();

            for(int i = 0; i < nl.getLength(); ++i) {
                Node node = nl.item(i);
                if (node instanceof Element) {
                    Element ele = (Element)node;
                    if (delegate.isDefaultNamespace(ele)) {
                        this.parseDefaultElement(ele, delegate);
                    } else {
                        delegate.parseCustomElement(ele);
                    }
                }
            }
        } else {
            delegate.parseCustomElement(root);
        }

    }
```

> 调用parseDefaultElement方法

org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader#parseDefaultElement

```java
 private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
        if (delegate.nodeNameEquals(ele, "import")) {
            this.importBeanDefinitionResource(ele);
        } else if (delegate.nodeNameEquals(ele, "alias")) {
            this.processAliasRegistration(ele);
        } else if (delegate.nodeNameEquals(ele, "bean")) {
            this.processBeanDefinition(ele, delegate);
        } else if (delegate.nodeNameEquals(ele, "beans")) {
            this.doRegisterBeanDefinitions(ele);
        }

    }
```

这里看到`if (delegate.nodeNameEquals(ele, "bean"))` 会不会很兴奋呢，接下来就是解析的方法了👇 

>  跳转到 processBeanDefinition(ele, delegate);

```java
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
        // 是的 就是这个方法了 👉
        BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
        if (bdHolder != null) {
            bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);

            try {
                BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, this.getReaderContext().getRegistry());
            } catch (BeanDefinitionStoreException var5) {
                this.getReaderContext().error("Failed to register bean definition with name '" + bdHolder.getBeanName() + "'", ele, var5);
            }

            this.getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
        }

    }
```


> parseBeanDefinitionElement 将元素数据解析到beanDefinition

org.springframework.beans.factory.xml.BeanDefinitionParserDelegate#parseBeanDefinitionElement(org.w3c.dom.Element, org.springframework.beans.factory.config.BeanDefinition)



```java
@Nullable
    public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
        String id = ele.getAttribute("id");
        String nameAttr = ele.getAttribute("name");
        List<String> aliases = new ArrayList();
        if (StringUtils.hasLength(nameAttr)) {
            String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, ",; ");
            aliases.addAll(Arrays.asList(nameArr));
        }

        String beanName = id;
        if (!StringUtils.hasText(id) && !aliases.isEmpty()) {
            beanName = (String)aliases.remove(0);
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("No XML 'id' specified - using '" + beanName + "' as bean name and " + aliases + " as aliases");
            }
        }

        if (containingBean == null) {
            this.checkNameUniqueness(beanName, aliases, ele);
        }
        // 将element数据最终转换成一个beanDefinition对象 是不是很惊奇 哈哈哈
     👉 AbstractBeanDefinition beanDefinition = this.parseBeanDefinitionElement(ele, beanName, containingBean);
        if (beanDefinition != null) {
            if (!StringUtils.hasText(beanName)) {
                try {
                    if (containingBean != null) {
                        beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, this.readerContext.getRegistry(), true);
                    } else {
                        beanName = this.readerContext.generateBeanName(beanDefinition);
                        String beanClassName = beanDefinition.getBeanClassName();
                        if (beanClassName != null && beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() && !this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
                            aliases.add(beanClassName);
                        }
                    }

                    if (this.logger.isTraceEnabled()) {
                        this.logger.trace("Neither XML 'id' nor 'name' specified - using generated bean name [" + beanName + "]");
                    }
                } catch (Exception var9) {
                    this.error(var9.getMessage(), ele);
                    return null;
                }
            }

            String[] aliasesArray = StringUtils.toStringArray(aliases);
            return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
        } else {
            return null;
        }
    }

```


