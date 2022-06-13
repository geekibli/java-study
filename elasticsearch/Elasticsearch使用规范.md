---
title: Elasticsearch使用规范
toc: true
date: 2021-11-01 14:45:39
tags: Elasticsearch
categories:
---




# Elasticsearch使用规范


## 查询规范建议

- **定义好mappings和settings**，不同的数据类型查询效率不一样，建议只需做精确查询以及范围查询的字段设置为keyword类型。对于要进行全文检索的字段设置合理的分词器。

- 对于只需要查询数据结果而不需要结果的相关度计算的情况，**使用filter query能大幅提升你的查询效率**。例如过滤某车牌和号码。

- **避免一次性取出大量的数量**：Elasticsearch被设计为一个搜索引擎，这使得它非常擅长获取与查询匹配的最优文档，但是不适合用来检索与特定查询匹配的所有文档。 Elasticsearch为了避免深分页，不允许使用分页（from&size）查询10000条以后的数据，如果需要这样做，请确保使用Scroll API。 （Scroll API 后来不被推荐使用 可以使用search after）

- **尽量细化查询条件**，查询的条件越细，查询效率越高。

- **选择合适的查询类型**，比如term查询效率相对会高一些。

- **优化路由**

    Elasticsearch写入文档时，文档会通过一个公式路由到一个索引中的一个分片上。默认公式如下：
    `shard_num = hash(_routing) % num_primary_shards`
    _routing字段的取值，默认是_id字段，可以根据业务场景设置经常查询的字段作为路由字段。例如可以考虑将用户id、地区作为路由字段，查询时可以过滤不必要的分片，加快查询速度。
    
- 避免使用wildcard模糊匹配查询
    Elasticsearch默认支持通过*？正则表达式来做模糊匹配，数据量级别达到TB+甚至更高之后，模糊匹配查询通常会耗时比较长，甚至可能导致内存溢出，卡死乃至崩溃宕机的情况。所以数据量大的情况下，不要使用模糊匹配查询。

- 合理的配置使用index属性，analyzed和not_analyzed，根据业务需求来控制字段是否分词或不分词。只有groupby需求的字段，配置时就设置成not_analyzed,以提高查询或聚类的效率。

- query_string或multi_match的查询字段越多，查询越慢。
    可以在mapping阶段，利用copy_to属性将多字段的值索引到一个新字段，multi_match时，用新的字段查询。

- **日期字段的查询**
    尤其是用now的查询实际上是不存在缓存的，因此， 可以从业务的角度来考虑是否一定要用now,毕竟利用query cache是能够大大提高查询效率的。

- **查询结果集的大小不能随意设置成大得离谱的值**
    如query.setSize不能设置成Integer.MAX_VALUE，<font color=red>因为ES内部需要建立一个数据结构来放指定大小的结果集数据。</font>

- **尽量避免使用script，万不得已需要使用的话，选择painless & experssions引擎。**
    <font color=blue>一旦使用script查询，一定要注意控制返回，千万不要有死循环，因为ES没有脚本运行的超时控制，只要当前的脚本没执行完，该查询会一直阻塞。</font>



## 容量规划

- **分片(shard)容量**
    - 非日志型(搜索型、线上业务型)的shard容量在10~30GB（建议在10G）
    - 日志型的shard容量在30~100GB（建议30G）
    - 单个shard的文档个数不能超过21亿左右(Integer.MAX_VALUE - 128)
    注：一个shard就是一个lucene分片，ES底层基于lucene实现。主分片个数一旦确定，就不可以更改。副本分片个数可以根据需要随时修改。

- **索引(index)数量**
大索引需要拆分：增强性能，风险分散。
反例：一个10T的索引，例如按date查询、name查询
正例：index_name拆成多个index_name_${date}
正例：index_name按hash拆分index_name_{1,2,3,...100..}
提示：索引和shard数并不是越多越好，对于批量读写都会有性能下降，所以要综合考虑性能和容量规划，同时配合压力测试，不存在真正的最优解。

- **节点、分片、索引**
一个节点管理的shard数不要超过200个


## 配置使用规范

- **shard个数（number_of_shards）**
    primery shard ：默认数量是1 
    replica shard数量为1： 是每个primary shard 有多少个副本分片的意思
    primery shard = 1 ; replica shard = 2 ; 意味着一个索引，一共存在9个shard

- **refresh频率（refresh_interval）**
    ES的定位是准实时搜索引擎，该值默认是1s，表示写入后1秒后可被搜索到，所以这里的值取决于业务对实时性的要求，注意这里并不是越小越好，刷新频率高也意味着对ES的开销也大，通常业务类型在1-5s，日志型在30s-120s，如果集中导入数据可将其设置为-1，ES会自动完成数据刷新（注意完成后更改回来，否则后续会出现搜索不到数据）

- **使用别名（aliases）**：不要过度依赖别名功能

- **慢日志（slowlog）**

- **设置合理的routing key(默认是id)**
    id不均衡：集群容量和访问不均衡，对于分布式存储是致命的

- **关闭_all**
    ES6.0已经去掉，对容量（索引过大）和性能（性能下降）都有影响。    

- **避免大宽表**
    ES默认最大1000，但建议不要超过100.    

- **text类型的字段不要使用聚合查询。**
    <font color=red>text类型fileddata会加大对内存的占用，如果有需求使用，建议使用keyword</font>
    
- **聚合查询避免使用过多嵌套**
    <font color=red>聚合查询的中间结果和最终结果都会在内存中进行，嵌套过多，会导致内存耗尽</font>
    
- **修改index_buffer_size的设置**
    可以设置成百分数，也可设置成具体的大小，大小可根据集群的规模做不同的设置测试。
　　`indices.memory.index_buffer_size：10%（默认）`
　　`indices.memory.min_index_buffer_size： 48mb（默认）`
　　`indices.memory.max_index_buffer_size`

- **修改translog相关的设置**
    - 控制数据从内存到硬盘的操作频率，以减少硬盘IO。可将sync_interval的时间设置大一些。
　　      `index.translog.sync_interval：5s(默认)`
    - 控制tranlog数据块的大小，达到threshold大小时，才会flush到lucene索引文件。
        `index.translog.flush_threshold_size：512mb(默认)    `

-  **_id字段的使用**
    应尽可能避免自定义_id,以避免针对ID的版本管理；建议使用ES的默认ID生成策略或使用数字类型ID做为主键    
    
- **Cache的设置及使用**
    - QueryCache: ES查询的时候，使用filter查询会使用query cache,如果业务场景中的过滤查询比较多，建议将querycache设置大一些，以提高查询速度。
    `indices.queries.cache.size： 10%（默认）`，可设置成百分比，也可设置成具体值，如256mb。
    
    当然也可以禁用查询缓存（默认是开启）, 通过`index.queries.cache.enabled：false`设置。

    - FieldDataCache:在聚类或排序时，`field data cache`会使用频繁，因此，**设置字段数据缓存的大小，在聚类或排序场景较多的情形下很有必要**
        可通过`indices.fielddata.cache.size：30%`或`具体值10GB`来设置。**但是如果场景或数据变更比较频繁，设置cache并不是好的做法，因为缓存加载的开销也是特别大的。**
    
    - **ShardRequestCache**
    查询请求发起后，每个分片会将结果返回给协调节点(Coordinating Node),由协调节点将结果整合。
    如果有需求，可以设置开启;通过设置**index.requests.cache.enable: true**来开启。
    不过，`shard request cache`只缓存`hits.total`, `aggregations`, `suggestions`类型的数据，并不会缓存hits的内容。也可以通过设置`indices.requests.cache.size: 1%（默认）`来控制缓存空间大小。    
    

    
## 字段设计规范

- text和keyword的用途必须分清：分词和关键词（确定字段是否需要分词）

- 确定字段是否需要独立存储

- 字段类型不支持修改，必须谨慎。

- 对不需要进行聚合/排序的字段禁用doc_values

- 不要在text做模糊搜索：




## 违规操作

- 原则：不要忽略设计，快就是慢，坏的索引设计后患无穷.
- 拒绝大聚合 ：ES计算都在JVM内存中完成。
- 拒绝模糊查询：es一大杀手

```json
{
    "query":{
        "wildcard":{
            "title.keyword":"*张三*"
    }
}
```

- 拒绝深度分页
    ES获取数据时，每次默认最多获取10000条，获取更多需要分页，但存在深度分页问题，<font color=red>一定不要使用from/Size方式，建议使用scroll或者searchAfter方式。</font> scroll会把上一次查询结果缓存一定时间（通过配置scroll=1m实现)，所以在使用scroll时一定要保证search结果集不要太大。

- 基数查询
尽量不要用基数查询去查询去重后的数据量大小（kibana中界面上显示是Unique Count，Distinct Count等），即少用如下的查询：

```json
"aggregations": {
     "cardinality": {
          "field": "userId"
      }
 }
```
 
- 禁止查询 indexName-*
- 避免使用script、update_by_query、delete_by_query，对线上性能影响较大。

## 建议操作

- **复用预索引数据方式来提高AGG性能**
    如通过terms aggregations替代range aggregations， 如要根据年龄来分组，分组目标是:少年（14岁以下） 青年（14-28） 中年（29-50） 老年（51以上）， <font color=red>可以在索引的时候设置一个age_group字段，预先将数据进行分类</font>。从而不用按age来做range aggregations,通过age_group字段就可以了。
    
- **避免将不相关的数据放在同一个索引中，以避免稀疏，将这些文件放在不同的索引中往往更好。**


## 索引及字段命名规范

### 索引

索引受文件系统的限制。仅可能为小写字母，不能下划线开头。同时需遵守下列规则：

- 不能包括 , /, *, ?, ", <, >, |, 空格, 逗号, #
- 7.0版本之前可以使用冒号:,但不建议使用并在7.0版本之后不再支持
- 不能以这些字符 -, _, + 开头
- 不能包括 . 或 …
- 长度不能超过 255 个字符

以上这些命名限制是因为当Elasticsearch使用索引名称作为磁盘上的目录名称，这些名称必须符合不同操作系统的约定。
未来可能会放开这些限制，因为我们使用uuid关联索引放在磁盘上，而不使用索引名称

### 类型
<font color=red>7.0版本之后不再支持类型，默认为_doc</font>


## 常见问题
- 一个索引的shard数一旦确定不能改变
- ES不支持事务ACID特性。
- reindex：reindex可以实现索引的shard变更，但代价非常大：速度慢、对性能有影响，所以好的设计和规划更重要
- field一旦创建不能更改mapping，如果需要修改，则必须重新创建索引








## 参考文档

- [Elasticsearch 使用规范](http://www.javajcw.com/72.html)
- [Elasticsearch索引及字段命名规范](https://blog.csdn.net/neweastsun/article/details/95868716)



