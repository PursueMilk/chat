package com.example.chat.service;

import com.example.chat.pojo.Result;

import java.util.List;
import java.util.Map;

public interface CollectService {


    Result collect(int userId, int entityId);

    Result unCollect(int userId, int entityId);

    long getPostCollectCount(int id);

    int getPostCollectStatus(int userId, int pid);

    long getUserCollectCount(int uid);

    List<Map<String, Object>> getCollections(int uid, int offset, int i);
}
