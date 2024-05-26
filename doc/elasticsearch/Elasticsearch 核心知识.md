#### 用途
- 应用程序搜索
- 网站搜索
- 企业搜索
- 日志处理和分析
- 基础设施指标和容器监测
- 应用程序性能监测
- 地理空间数据分析和可视化
- 安全分析
- 业务分析

#### 工作原理
原始数据会从从多个来源（包括日志、系统指标和网络应用程序）输入到 Elasticsearch 中，数据采集指在 ElastIcsearch 中进行索引之前解析、标准化并充实这些原始数据的过程，这些数据在 ElasticSearch 中索引完成之后，用户便可针对数据运行复杂的查询，并使用聚合来检索自身数据的复杂汇总，在 Kibana 中，用户可以基于自己的数据创建强大的可视化，分享仪表板，并对 Elastic Stack 进行管理。

#### 基本概念
**Index **索引指相互关联的文档集合。

**Document **以 JSON 文档的形式存储数据。

**Type **每个文档都会在一组键（字段或属性的名称）和它们对应的值（字符串、数字、布尔值、日期、数值组、地理位置或其他类型的数据）之间建立联系。

Elasticsearch 使用的是倒排索引的数据结构，倒排索引会列出在所有文档中出现的每个特有词汇，并且可以找到包含每个词汇的全部文档。

在索引过程中，Elasticsearch 会存储文档并构建倒排索引，这样用户便可以近实时地对文档数据进行搜索。索引过程是在索引 API 中启动的，通过此 API 您既可向特定索引中添加 JSON 文档，也可更改特定索引中的 JSON 文档。

分词->记录->检索->根据相关性得分，倒排
 
![Elasticsearch数据模型](/pic/Elasticsearch数据模型.jpeg)
[Start searching | Elasticsearch Guide [7.16] | Elastic](https://www.elastic.co/guide/en/elasticsearch/reference/7.x/getting-started-search.html)
