package com.example.chat.service;

public interface LikeService {
    Long getPostLikeCount(Integer postId);

    int getPostLikeStatus(int userId, int pid);

    long getCommentLikeCount(int id);

    int getCommentLikeStatus(int userId, int id);

    void like(Integer id, String key, int entityId, int entityUserId);

    int getUserLikeCount(int userId);
}
