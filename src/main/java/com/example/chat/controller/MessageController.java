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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.example.chat.utils.ConstantUtil.*;

@Api(tags = "消息接口")
@RestController
public class MessageController extends BaseController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    @ApiOperation("消息通知数量")
    @GetMapping( "/message/list")
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

    @ApiOperation("评论回复信息")
    @GetMapping("/message/reply")
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

    @ApiOperation("获取点赞消息")
    @GetMapping("/message/like")
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

    @ApiOperation("获取关注消息")
    @GetMapping("/message/follow")
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

    @ApiOperation("获取系统通知")
    @GetMapping("/message/system")
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

    @ApiOperation("查看聊天列表")
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

    @ApiOperation("查看聊天记录")
    @PostMapping("/message/getSession")
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

}
