package com.example.chat.service;

import java.util.List;
import java.util.Map;

public interface FollowService {
    void follow(Integer userId, int entityId);

    long getFollowerCount(int entityId);

    void unfollow(Integer id, int entityId);

    long getFolloweeCount(int userId);

    // 查询当前用户是否已关注该实体
    boolean hasFollowed(int userId, int entityId);


    // 查询某用户关注的人
    List<Map<String, Object>> getFollowees(int userId, int offset, int limit);

    // 查询某用户的粉丝
    List<Map<String, Object>> getFollowers(int userId, int offset, int limit);
}
