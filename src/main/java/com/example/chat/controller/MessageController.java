package com.example.chat.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.chat.dto.ChatDto;
import com.example.chat.pojo.Message;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.CommentService;
import com.example.chat.service.MessageService;
import com.example.chat.service.UserService;
import com.example.chat.vo.MessageVo;
import com.example.chat.vo.MsgVo;
import com.example.chat.vo.PaginationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.example.chat.utils.ConstantUtil.*;


@RestController
public class MessageController extends BaseController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    // 会话列表
    @GetMapping( "/letter/list")
    public Result getLetterList(@RequestParam(defaultValue = "1") int currentPage) {
        int userId = getUserId();
        User user = userService.getUserById(userId);
        //查询用户有几个会话
        int conversationCount = messageService.getConversationCount(userId);
        int offset = (currentPage - 1) * 5;
        //会话列表
        List<Message> conversationList = messageService.getConversations(userId, offset, 5);
        List<MessageVo> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                MessageVo messageVo = new MessageVo();
                messageVo.setConversation(message);
                //消息数量
                messageVo.setLetterCount(messageService.getLetterCount(message.getConversationId()));
                if (message.getConversationId() == null) {
                    //TODO 问题
                    messageVo.setUnreadCount(user.getId());
                } else {
                    messageVo.setUnreadCount(messageService.getLetterUnreadCount(user.getId(), message.getConversationId()));
                }
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                messageVo.setTarget(userService.getUserById(targetId));
                conversations.add(messageVo);
            }
        }
        // 查询未读私信数量
        int letterUnreadCount = messageService.getLetterUnreadCount(user.getId());
        //查询未读系统消息数量
        int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(), null);
        PaginationVo<MessageVo> paginationVo = new PaginationVo<>();
        paginationVo.setTotal(conversationCount);
        paginationVo.setRecords(conversations);
        paginationVo.setCurrentPage(currentPage);
        paginationVo.setPageSize(5);
        paginationVo.setLetterUnreadCount(letterUnreadCount);
        paginationVo.setNoticeUnreadCount(noticeUnreadCount);
        return Result.success().setData(paginationVo);
    }


    //消息中心
    @RequestMapping(path = "/message/list", method = RequestMethod.GET)
    public Result getNoticeList() {
        User user = userService.getUserById(getUserId());

        //查询【回复我的消息总数】（未读）
        int replyCount = messageService.getNoticeUnreadCount(user.getId(), TOPIC_COMMENT);

        //查询【点赞我的消息总数】
        int likeCount = messageService.getNoticeUnreadCount(user.getId(), TOPIC_LIKE);

        //查询【新粉丝数量】
        int followCount = messageService.getNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);

        //查询【系统通知】
        int systemCount = messageService.getNoticeUnreadCount(user.getId(), TOPIC_SYSTEM);

        Map<String, Object> map = new HashMap<>();

        map.put("replyCount", replyCount);
        map.put("likeCount", likeCount);
        map.put("followCount", followCount);
        map.put("systemCount", systemCount);

        return Result.success().setData(map);
    }


    /**
     * 获取回复我的消息
     *
     * @param currentPage
     * @return
     */
    @RequestMapping("/message/reply")
    public Result getReply(@RequestParam(defaultValue = "1") int currentPage) {
        User user = userService.getUserById(getUserId());
        int limit = 5;
        int offset = (currentPage - 1) * 5;
        // 查询评论我的消息(所有的消息，不管是已读还是未读)
        List<Message> messages = messageService.getAllMessage(user.getId(), TOPIC_COMMENT, offset, limit);
        int total = messageService.getNoticeCount(user.getId(), TOPIC_COMMENT);
        Map<String, Object> map = new HashMap<>();
        List<MsgVo> msgVos = new ArrayList<>();
        for (Message message : messages) {
            //获取消息的内容
            String content = message.getContent();
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            MsgVo msgVo = new MsgVo();
            //评论我的用户
            msgVo.setFromUser(userService.getUserById((Integer) data.get("userId")));
            int entityType = Integer.parseInt("" + data.get("entityType"));
            int entityId = Integer.parseInt("" + data.get("entityId"));
            //评论我的帖子还是评论
            msgVo.setMsgType(entityType);
            //被评论的实体id
            msgVo.setEntityId(entityId);
            msgVo.setContent("" + data.get("content"));
            //该评论所在的帖子
            msgVo.setPostId(Integer.parseInt("" + data.get("postId")));
            msgVo.setCreateTime(message.getCreateTime());
            msgVos.add(msgVo);
        }
        map.put("msgVos", msgVos);
        map.put("total", total);
        //把消息设置为已读
        List<Integer> ids = new ArrayList<>();
        messages.stream().forEach(message -> ids.add(message.getId()));
        //如果ID为空，Sql会有语法错误
        if (ids.size() > 0) {
            messageService.readMessage(ids);
        }
        return Result.success().setData(map);
    }

    /**
     * 获取点赞我的消息
     *
     * @param currentPage
     * @return
     */
    @RequestMapping("/message/like")
    public Result getLike(@RequestParam(defaultValue = "1") int currentPage) {
        User user = userService.getUserById(getUserId());
        int limit = 5;
        int offset = (currentPage - 1) * 5;
        // 查询点赞我的消息(所有的消息，不管是已读还是未读)
        List<Message> messages = messageService.getAllMessage(user.getId(), TOPIC_LIKE, offset, limit);
        int total = messageService.getNoticeCount(user.getId(), TOPIC_LIKE);
        Map<String, Object> map = new HashMap<>();
        List<MsgVo> msgVos = new ArrayList<>();
        for (Message message : messages) {
            //获取消息的内容
            String content = message.getContent();
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            MsgVo msgVo = new MsgVo();
            //点赞我的用户
            msgVo.setFromUser(userService.getUserById((Integer) data.get("userId")));
            //点赞我的文章还是评论
            int entityType = Integer.parseInt("" + data.get("entityType"));
            msgVo.setMsgType(entityType);
            //该评论所在的帖子
            msgVo.setPostId(Integer.parseInt("" + data.get("postId")));
            msgVo.setCreateTime(message.getCreateTime());
            msgVos.add(msgVo);

        }
        map.put("msgVos", msgVos);
        map.put("total", total);
        //把消息设置为已读
        List<Integer> ids = new ArrayList<>();
        messages.stream().forEach(message -> ids.add(message.getId()));
        //如果ID为空，Sql会有语法错误
        if (ids.size() > 0) {
            messageService.readMessage(ids);
        }

        return Result.success().setData(map);
    }

    /**
     * 获取关注我的消息
     *
     * @param currentPage
     * @return
     */
    @RequestMapping("/message/follow")
    public Result getFollow(@RequestParam(defaultValue = "1") int currentPage) {
        User user = userService.getUserById(getUserId());
        int limit = 5;
        int offset = (currentPage - 1) * 5;
        // 查询关注我的消息(所有的消息，不管是已读还是未读)
        List<Message> messages = messageService.getAllMessage(user.getId(), TOPIC_FOLLOW, offset, limit);
        int total = messageService.getNoticeCount(user.getId(), TOPIC_FOLLOW);
        Map<String, Object> map = new HashMap<>();
        List<MsgVo> msgVos = new ArrayList<>();
        for (Message message : messages) {
            //获取消息的内容
            String content = message.getContent();
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            MsgVo msgVo = new MsgVo();
            //关注我的用户
            msgVo.setFromUser(userService.getUserById((Integer) data.get("userId")));
            msgVo.setCreateTime(message.getCreateTime());
            msgVos.add(msgVo);
        }
        map.put("msgVos", msgVos);
        map.put("total", total);
        //把消息设置为已读
        List<Integer> ids = new ArrayList<>();
        messages.stream().forEach(message -> ids.add(message.getId()));
        //如果ID为空，Sql会有语法错误
        if (ids.size() > 0) {
            messageService.readMessage(ids);
        }
        return Result.success().setData(map);
    }

    //获取系统通知
    @RequestMapping("/message/system")
    public Result getSystem(@RequestParam(defaultValue = "1") int currentPage) {
        User user = userService.getUserById(getUserId());

        int limit = 5;
        int offset = (currentPage - 1) * 5;

        // 查询系统消息(所有的消息，不管是已读还是未读)
        List<Message> messages = messageService.getAllMessage(user.getId(), TOPIC_SYSTEM, offset, limit);
        int total = messageService.getNoticeCount(user.getId(), TOPIC_SYSTEM);

        Map<String, Object> map = new HashMap<>();
        List<MsgVo> msgVos = new ArrayList<>();

        for (Message message : messages) {

            //获取消息的内容
            String content = message.getContent();
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            MsgVo msgVo = new MsgVo();
            msgVo.setCreateTime(message.getCreateTime());
            msgVos.add(msgVo);

        }

        map.put("msgVos", msgVos);
        map.put("total", total);

        //把消息设置为已读（status置1）
        List<Integer> ids = new ArrayList<>();
        messages.stream().forEach(message -> ids.add(message.getId()));
        //如果ID为空，Sql会有语法错误
        if (ids.size() > 0) {
            messageService.readMessage(ids);
        }

        return Result.success().setData(map);
    }

    //获取与用户有消息来往的用户列表
    @GetMapping("/message/getUsers")
    public Result getUsers() {
        int userId = getUserId();
        //连接成功后，查询跟当前用户有消息来往的用户
        List<User> userList = new ArrayList<>();
        Set<Integer> toMeids = messageService.getChatToMeIds(userId);
        Set<Integer> meToIds = messageService.getIChatToids(userId);
        //查询聊天用户列表
        //合并集合
        toMeids.addAll(meToIds);
        List<Integer> ids = new ArrayList<>(toMeids);
        if (toMeids.size() > 0) {
            userList = userService.getUserByIds(ids);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userList", userList);
        return Result.success().setData(map);
    }

    //查找聊天记录，需要对方的id
    @RequestMapping("/message/getSession")
    public Result getSession(@RequestBody ChatDto chatDto) {
        int userId = getUserId();
        int currentId = chatDto.getToId();

        Map<String, Object> map = new HashMap<>();

        //查询聊天记录
        List<Message> chatList = messageService.getChatList(userId, currentId);
        //消息记录
        map.put("chatList", chatList);

        return Result.success().setData(map);
    }


/*    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if (getUserId() == id0) {
            return userService.getUserById(id1);
        } else {
            return userService.getUserById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (getUserId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }*/


}
