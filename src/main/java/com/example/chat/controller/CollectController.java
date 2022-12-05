package com.example.chat.controller;

import com.example.chat.annotion.TokenPass;
import com.example.chat.dto.CollectDto;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.CollectService;
import com.example.chat.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "收藏接口")
@RestController
public class CollectController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private CollectService collectService;

    @ApiOperation(value = "收藏文章")
    @PostMapping("/collect")
    public Result collect(@RequestBody CollectDto collectDto) {
        redisUtil.increaseScore(collectDto.getEntityId(),15);
        return collectService.collect(getUserId(), collectDto.getEntityId());
    }


    @ApiOperation(value = "取消收藏")
    @PostMapping("/uncollect")
    public Result uncollect(@RequestBody CollectDto collectDto) {
        redisUtil.increaseScore(collectDto.getEntityId(),-15);
        return collectService.unCollect(getUserId(), collectDto.getEntityId());
    }

    @ApiOperation(value = "查询用户收藏的文章")
    @TokenPass
    @GetMapping("/collection/{uid}")
    public Result postCollection(@ApiParam(name = "uid", value = "用户编号") @PathVariable("uid") int uid,
                                 @ApiParam(name = "currentPage", value = "用户当前页") @RequestParam(defaultValue = "1") int currentPage) {
        User user = userService.getUserById(uid);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        //查询用户收藏文章总数
        long total = collectService.getUserCollectCount(uid);
        map.put("total", total);
        //查询文章
        int offset = (currentPage - 1) * 5;
        List<Map<String, Object>> postList = collectService.getCollections(uid, offset, 5);
        map.put("posts", postList);
        map.put("currentPage", currentPage);
        return Result.success().setData(map);
    }
}
