package com.example.chat.service;

import com.example.chat.pojo.Message;

import java.util.List;
import java.util.Set;

public interface MessageService {

    int getConversationCount(int userId);

    int getNoticeCount(int userId, String comment);

    List<Message> getConversations(int userId, int offset, int i);

    int getLetterCount(String conversationId);

    int getLetterUnreadCount(Integer id, String conversationId);

    int getLetterUnreadCount(int userId);

    int getNoticeUnreadCount(int userId, String topic);

    List<Message> getAllMessage(Integer id, String topicComment, int offset, int limit);

    int readMessage(List<Integer> ids);

    Set<Integer> getChatToMeIds(int userId);

    Set<Integer> getIChatToids(int userId);

    List<Message> getChatList(int userId, int currentId);
}
