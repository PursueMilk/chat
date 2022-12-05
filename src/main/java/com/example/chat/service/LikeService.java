package com.example.chat.service;

public interface LikeService {
    long getPostLikeCount(int postId);

    int getPostLikeStatus(int userId, int pid);

    long getCommentLikeCount(int id);

    int getCommentLikeStatus(int userId, int id);

    void like(Integer id, String key, int entityId, int entityUserId);

    int getUserLikeCount(int userId);
}
