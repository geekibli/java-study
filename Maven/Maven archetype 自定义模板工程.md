# maven archetype 自定义模板工程






自定义模板工程原理：利用maven插件archetype，将源工程生成为archetype工程，即模板工程，生成模板工程的过程中将指定字符串或文件名替换为占位符，然后将这个模板工程发布到本地仓库或私服仓库，就可以在创建工程的时候引用这个模板工程，给占位符赋值，生成新的工程，主要以下三步：

  1，根据源工程生成模板工程：

 	`mvn archetype:create-from-project`
  
  2，发布模板工程：

	`mvn clean install 或 mvn clean deploy`
  
  3，根据模板工程创建新工程：

  `archetype:generate -DarchetypeGroupId="..." -archetypeArtifactId="..." -DarchetypeVersion="..."`




## 参考链接

- [maven archetype 自定义模板工程](http://t.zoukankan.com/xzhuo0827-p-12582154.html)