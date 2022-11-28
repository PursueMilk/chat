package com.example.chat.mapper;

import com.example.chat.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    Comment findCommentById(int entityId);

    String findCommentByIdAndType(int entityType, int entityId);

    int insertComment(Comment comment);
}
