# 校园论坛
本论坛系统为在校学生提供一个交流互动平台，便于学生获取信息，分享校园生活、交流学习经验及心得等。系统采用前后端分离技术（SpringBoot+Vue开发），实现了登录注册、发帖评论、点赞关注、内容搜索、系统通知、私信等功能。

**技术栈**：SpringBoot、Mybatis、Mysql、Redis、RabbitMQ、ElasticSearch

## 功能实现：

- 发表文章（markdown格式，富文本编辑，可上传图片）
- 评论文章
- 回复评论
- 邮件发送（注册时发送激活邮件）
- 系统通知
- 关注
- 收藏
- 点赞
- 搜索
- 聊天（类似网页版微信）


## 技术实现

* 使用Redis存储登录Token，解决分布式Session问题

* 通过字典树对帖子进行敏感词过滤

* 使用定时器，定时计算帖子分数生成热榜

* 通过切面编程对点赞接口进行限流

* 采用websocket技术实现即时聊天

* 使用Redis缓存文章信息，提升访问速度和并发量，减少数据库压力

* 使用RabbitMQ异步实现系统通知、邮件发送，降低请求响应时间，提升用户体验

* 使用ElasticSearch实现文章搜索功能，通过IK中文分词实现对文章的模糊查询


## 项目预览图

论坛首页

![image-20230213214009420](https://raw.githubusercontent.com/PursueMilk/img/main/img/202302132140654.png)

个人主页

![image-20230213213946241](https://raw.githubusercontent.com/PursueMilk/img/main/img/202302132139433.png)

发表文章

![image-20230213213600378](https://raw.githubusercontent.com/PursueMilk/img/main/img/202302132136618.png)

文章详情

![image-20230213214038163](https://raw.githubusercontent.com/PursueMilk/img/main/img/202302132140321.png)



文章搜索

![image-20230213213918097](https://raw.githubusercontent.com/PursueMilk/img/main/img/202302132139294.png)

通知页面

![image-20230213214108752](https://raw.githubusercontent.com/PursueMilk/img/main/img/202302132141940.png)

## 启动

在此之前请确保在application.yaml文件中正确配置Mysql、Redis、RabbitMQ、ElasticSearch有关信息并确保它们的正常运行，然后可启动程序。

## LICENSE

chat is under the Apache license. See the [LICENSE](https://github.com/PursueMilk/chat/blob/master/license) file for details.
