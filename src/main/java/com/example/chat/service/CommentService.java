package com.example.chat.service;

import com.example.chat.pojo.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int getCommentCount(int entityType, int pid);

    Comment getCommentById(int entityId);

    int addComment(Comment comment);
}
