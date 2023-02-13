package com.example.chat.service.impl;

import com.example.chat.mapper.MessageMapper;
import com.example.chat.pojo.Message;
import com.example.chat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 系统通知实现
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;


    @Override
    public int getConversationCount(int userId) {
        return messageMapper.queryConversationCount(userId);
    }

    @Override
    public int getNoticeCount(int userId, String topic) {
        return messageMapper.queryNoticeAllCount(userId, topic);
    }

    @Override
    public List<Message> getConversations(int userId, int offset, int limit) {
        return messageMapper.queryConversations(userId, offset, limit);
    }

    @Override
    public int getLetterCount(String conversationId) {
        return messageMapper.queryLetterCount(conversationId);
    }

    @Override
    public int getLetterUnreadCount(Integer userId, String conversationId) {
        return  messageMapper.queryLetterUnreadCount(userId, conversationId);
    }

    @Override
    public int getLetterUnreadCount(int userId) {
        return messageMapper.queryLetterUnreadCountWithoutCid(userId);
    }

    @Override
    public int getNoticeUnreadCount(int userId, String topic) {
        return messageMapper.queryNoticeUnreadCount(userId, topic);
    }

    @Override
    public List<Message> getAllMessage(Integer id, String topic, int offset, int limit) {
        return messageMapper.queryAllMessage(id,topic,offset,limit);
    }

    @Override
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    @Override
    public Set<Integer> getChatToMeIds(int userId) {
        return messageMapper.queryChatToMeIds(userId);
    }

    @Override
    public Set<Integer> getIChatToids(int userId) {
        return messageMapper.queryMeChatToIds(userId);
    }

    @Override
    public List<Message> getChatList(int userId, int currentId) {
        String conversation_id;
        if (userId < currentId){
            conversation_id = userId+"_"+currentId;
        }else {
            conversation_id = currentId+"_"+userId;
        }
        return messageMapper.queryChatList(conversation_id);
    }
}
