package com.example.chat.controller;

import com.example.chat.annotion.TokenPass;
import com.example.chat.dto.CollectDto;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.CollectService;
import com.example.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CollectController extends BaseController {

    @Autowired
    private UserService userService;
    @Autowired
    private CollectService collectService;

    /**
     * 收藏
     *
     * @param collectDto
     * @return
     */
    @PostMapping("/collect")
    public Result collect(@RequestBody CollectDto collectDto) {
        return collectService.collect(getUserId(), collectDto.getEntityId());
    }

    @PostMapping("/uncollect")
    public Result uncollect(@RequestBody CollectDto collectDto) {
        return collectService.unCollect(getUserId(), collectDto.getEntityId());
    }

    /**
     * 查询用户收藏的文章
     * @param uid
     * @return
     */
    @TokenPass
    @GetMapping("/collection/{uid}")
    public Result postCollection(@PathVariable("uid") int uid, @RequestParam(defaultValue = "1") int currentPage) {
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
