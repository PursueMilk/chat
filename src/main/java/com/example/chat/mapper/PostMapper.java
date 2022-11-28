package com.example.chat.mapper;

import com.example.chat.pojo.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostMapper {

    Integer insertPost(Post post);

    List<Post> listPost(int offset);

    int countPost();

    Post queryPostById(int id);

    int updateCommentCount(int entityId, int count);
    @Select("select * from post where user_id = #{userId} and status != 2 order by id desc limit #{offset} , #{limit} ")
    List<Post> getPosts(int uid, int offset, int i);
    @Select("select count(1) from post where user_id = #{userId} ")
    int getUserPostsCount(int uid);
}
