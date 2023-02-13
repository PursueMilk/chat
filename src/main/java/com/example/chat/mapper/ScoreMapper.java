package com.example.chat.mapper;


import com.example.chat.pojo.PostScore;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScoreMapper {
    void updatePostScore(PostScore postScore);

    PostScore findPostScore(int postId);

    void insertPostScore(PostScore postScore);

    List<PostScore> hotList();
}
