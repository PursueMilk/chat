package com.example.chat.controller;

import com.example.chat.annotion.TokenPass;
import com.example.chat.async.EventHandler;
import com.example.chat.dto.FollowDto;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.FollowService;
import com.example.chat.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.chat.utils.ConstantUtil.TOPIC_FOLLOW;

@Api(tags = "关注接口")
@RestController
public class FollowController extends BaseController{

    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;
    @Autowired
    private EventHandler eventHandler;

    @ApiOperation("添加关注")
    @PostMapping(path = "/follow")
    public Result follow(@RequestBody FollowDto followDto) {
        User user = userService.getUserById(getUserId());
        int entityId = followDto.getEntityId();
        int entityType = followDto.getEntityType();
        //不可以自己关注自己
        if (user.getId() == entityId){
            return Result.fail().setMsg("不能关注自己");
        };
        followService.follow(user.getId(),entityId);
        // 触发关注事件,类型是为了区分不同的消息类型
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        //用线程池去发送事件
        eventHandler.handleTask(event);
        // 返回粉丝数量
        long followerCount = followService.getFansCount(entityId);
        Map<String,Object> map = new HashMap<>();
        map.put("followerCount", followerCount);
        map.put("hasFollowed", true);
        return Result.success().setData(map);
    }


    @ApiOperation("取消关注")
    @PostMapping(path = "/unfollow")
    public Result unfollow(@RequestBody FollowDto followDto) {
        User user = userService.getUserById(getUserId());
        int entityId = followDto.getEntityId();
        followService.unfollow(user.getId(), entityId);
        // 粉丝数量
        long followerCount = followService.getFansCount(entityId);
        Map<String,Object> map = new HashMap<>();
        map.put("followerCount", followerCount);
        map.put("hasFollowed", false);
        return  Result.success().setData(map);
    }

    @ApiOperation("个人关注的用户")
    @TokenPass
    @GetMapping(path = "/followees/{userId}")
    public Result getFollowees(@PathVariable("userId") int userId, @RequestParam(defaultValue = "1") int currentPage) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("user",user);
        long followeeCount = followService.getFolloweeCount(userId);
        map.put("total",followeeCount);
        int offset = (currentPage-1)*5;
        List<Map<String, Object>> userList = followService.getFollowees(userId, offset,5);
        //判断用户是否关注
        if (userList != null) {
            for (Map<String, Object> m : userList) {
                User u = (User) m.get("user");
                m.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        map.put("userList",userList);
        return  Result.success().setData(map);
    }

    @ApiOperation("查看用户的粉丝")
    @TokenPass
    @GetMapping(path = "/fans/{userId}")
    public Result getFollowers(@PathVariable("userId") int userId, @RequestParam(defaultValue = "1") int currentPage) {
        User user = userService.getUserById(userId);
        if (user == null) {
            //TODO 异常优化
            return Result.fail();
        }
        Map<String,Object> map = new HashMap<>();
        map.put("user",user);
        // 粉丝数量
        long followerCount = followService.getFansCount(userId);
        map.put("total",followerCount);
        int offset = (currentPage-1)*5;
        //TODO 优化
        List<Map<String, Object>> userList = followService.getFans(userId, offset,5);
        if (userList != null) {
            for (Map<String, Object> m : userList) {
                User u = (User) m.get("user");
                m.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        map.put("userList",userList);
        return Result.success().setData(map);
    }


    //查询当前用户是否关注了id为userId的用户
    private int hasFollowed(int userId) {
        if (!isLogin()) {
            return 0;
        }
        return followService.hasFollowed(getUserId(), userId);
    }

}
