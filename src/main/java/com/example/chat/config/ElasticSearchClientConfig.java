package com.example.chat.config;


import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchClientConfig {

    //配置RestHighLevelClient依赖到spring容器中待用
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        //绑定本机，端口，协议，如果是ES集群，就配置多个
                        new HttpHost("127.0.0.1",9200,"http")));
        return client;
    }

}
