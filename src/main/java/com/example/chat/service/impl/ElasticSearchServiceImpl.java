package com.example.chat.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.example.chat.pojo.Post;
import com.example.chat.pojo.SearchResult;
import com.example.chat.service.ElasticSearchService;
import com.example.chat.service.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private PostRepository postRepository;


    @Autowired
    private RestHighLevelClient restHighLevelClient;


    public SearchResult searchPost(String keyword, int current, int limit) throws IOException {
        //discusspost是索引名，就是表名
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .sort(SortBuilders.fieldSort("createTime.keyword").order(SortOrder.DESC))
                // 指定从哪条开始查询
                .from(current)
                // 需要查出的总记录条数
                .size(limit);
        searchRequest.source(searchSourceBuilder);
        //搜索结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //封装结果
        List<Post> list = new ArrayList<>();
        long total = searchResponse.getHits().getTotalHits().value;
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Post discussPost = JSONObject.parseObject(hit.getSourceAsString(), Post.class);
            list.add(discussPost);
        }
        //返回搜索结果
        return new SearchResult(list, total);
    }

    @Override
    public void savePost(Post post) {
        log.info("es保存推文：" + post.getTitle());
        postRepository.save(post);
    }

    @Override
    public void deletePost(int id) {
        postRepository.deleteById(id);
    }
}
