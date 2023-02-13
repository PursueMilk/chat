package com.example.chat.service;


import com.example.chat.pojo.Post;
import com.example.chat.pojo.SearchResult;
import org.springframework.data.domain.Page;


import java.io.IOException;
import java.util.List;

public interface ElasticSearchService {

    public void savePost(Post post);

    public void deletePost(int id);

    public SearchResult searchPost(String keyword, int current, int limit) throws IOException;


}
