package com.example.chat.mapper;

import com.example.chat.pojo.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface PostMapper {

    Integer insertPost(Post post);

    List<Post> listPost(int offset);

    int countPost();

    Post queryPostById(int id);

    int updateCommentCount(int entityId, int count);
    @Select("select * from post where user_id = #{uid} and status != 2 order by id desc limit #{offset} , #{limit} ")
    List<Post> getPosts(int uid, int offset, int limit);
    @Select("select count(1) from post where user_id = #{userId} ")
    int getUserPostsCount(int uid);

    List<Post> queryPostByIds(Set<Integer> set);

    List<Post> searchPost(String message,int offset);

    int countSearchPost(String message);
}
