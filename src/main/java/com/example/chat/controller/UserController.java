package com.example.chat.controller;


import com.example.chat.annotion.TokenPass;
import com.example.chat.dto.NewPassDto;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.FollowService;
import com.example.chat.service.LikeService;
import com.example.chat.service.PostService;
import com.example.chat.service.UserService;
import com.example.chat.vo.PaginationVo;
import com.example.chat.vo.PostVo;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static com.example.chat.utils.RedisKeyUtil.getUserTokenKey;


@Api(tags = "用户接口")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private PostService postService;

    @Autowired
    private FollowService followService;


    @ApiOperation(value = "登录接口")
    @TokenPass
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        return userService.login(user);
    }

    @ApiOperation(value = "注册接口")
    @TokenPass
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }

    @ApiOperation(value = "获取用户信息")
    @GetMapping("/getInfo")
    public Result getInfo() {
        return Result.success().setData(getUserVo());
    }


    @ApiOperation(value = "上传图片")
    @PostMapping("/uploadImg")
    public Result uploadImg(@ApiParam(name = "file", value = "上传的图片") @RequestPart MultipartFile file) {
        return userService.uploadImg(file, getUserId());
    }


    @ApiOperation(value = "激活账号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "account",value = "账号",dataType = "String",dataTypeClass = String.class),
            @ApiImplicitParam(name = "code",value = "验证码",dataType = "String",dataTypeClass = String.class)
    })
    @TokenPass
    @GetMapping("/activation")
    public Result activation(@RequestParam String account, @RequestParam String code) {
        return userService.activation(account, code);
    }


    @ApiOperation(value = "修改密码")
    @PostMapping("/changePwd")
    public Result updatePasswd(@RequestBody NewPassDto newPassDto) {
        return userService.updatePasswd(getUserId(), newPassDto);
    }


    //修改邮箱、性别、昵称
    @ApiOperation(value = "修改信息")
    @PostMapping("/updateOther")
    public Result updateOther(@RequestBody User user) {
        String  token= getUserTokenKey(request.getHeader("TOKEN"));
        user.setId(getUserId());
        return userService.update(user,token);
    }

    @ApiOperation(value = "忘记密码")
    @TokenPass
    @PostMapping("/forgetPwd")
    public Result forgetPwd(@RequestBody User user) {
        return userService.forgetPwd(user.getEmail());
    }

    @ApiOperation(value = "用户主页信息")
    @TokenPass
    @GetMapping("/profile/{uid}")
    public Result userPage(@ApiParam(name = "uid",value = "用户编号") @PathVariable(name = "uid") Integer uid) {
        User user = userService.getUserById(uid);
        if (user == null) {
            return Result.fail().setMsg("用户不存在！");
        }
        //用request的token信息来判断是否是访问别人的主页还是自己的主页
        Map<String, Object> map = new HashMap<>();
        boolean isLogin = isLogin();
        //查询是否为当前账号
        if (isLogin && getUserId().equals(uid)) {
            map.put("isMine", true);
        } else {
            map.put("isMine", false);
        }
        // 点赞数量
        long likeCount = likeService.getUserLikeCount(uid);
        map.put("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.getFolloweeCount(uid);
        map.put("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.getFansCount(uid);
        map.put("followerCount", followerCount);
        // 是否已关注
        int hasFollowed = 0;
        if (isLogin) {
            hasFollowed = followService.hasFollowed(getUserId(), uid);
        }
        map.put("hasFollowed", hasFollowed);
        map.put("user", user);
        return Result.success().setData(map);
    }


    @ApiOperation("用户发布的文章")
    @TokenPass
    @GetMapping("/userPost/{uid}")
    public Result posts(@ApiParam(name = "uid",value = "用户编号") @PathVariable(name = "uid") int uid,
                        @ApiParam(name = "currentPage",value = "当前页")  @RequestParam(defaultValue = "1") int currentPage) {
        PaginationVo<PostVo> pagination = postService.listByUserId(currentPage, uid);
        Map<String, Object> map = new HashMap<>();
        map.put("pagination", pagination);
        User user = userService.getUserById(uid);
        map.put("user", user);
        return Result.success().setData(map);
    }

}
