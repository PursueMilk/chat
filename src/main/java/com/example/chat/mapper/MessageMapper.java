package com.example.chat.mapper;

import com.example.chat.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface MessageMapper {
    int queryConversationCount(int userId);

    int queryNoticeAllCount(int userId, String topic);

    List<Message> queryConversations(int userId, int offset, int limit);

    int queryLetterCount(String conversationId);

    int queryLetterUnreadCount(Integer userId, String conversationId);

    int queryLetterUnreadCountWithoutCid(int userId);

    int queryNoticeUnreadCount(int userId, String topic);

    List<Message> queryAllMessage(Integer id, String topic, int offset, int limit);

    int updateStatus(List<Integer> ids, int status);

    Set<Integer> queryChatToMeIds(int userId);

    Set<Integer> queryMeChatToIds(int userId);

    List<Message> queryChatList(String conversation_id);

    int insertMessage(Message message);
}
