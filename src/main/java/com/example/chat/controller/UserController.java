package com.example.chat.controller;


import com.example.chat.annotion.TokenPass;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.FollowService;
import com.example.chat.service.LikeService;
import com.example.chat.service.PostService;
import com.example.chat.service.UserService;
import com.example.chat.vo.PaginationVo;
import com.example.chat.vo.PostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//TODO 忘记密码，修改密码，查看别人主页，文章
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


    @TokenPass
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        return userService.login(user);
    }


    @TokenPass
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }


    @GetMapping("/getInfo")
    public Result getInfo() {
        return Result.success().setData(getUserVo());
    }


    @PostMapping("/uploadImg")
    public Result uploadImg(@RequestPart MultipartFile file) {
        return userService.uploadImg(file, getUserId());
    }

    @TokenPass
    @GetMapping("/activation")
    public Result activation(@RequestParam String username, @RequestParam String code) {
        return userService.activation(username, code);
    }

    @PostMapping("/update")
    public Result update(@RequestBody User user) {
        user.setId(getUserId());
        return userService.update(user);
    }


    /**
     * 用户主页，无须登录也可访问，根据用户id来访问
     *
     * @return
     */
    @TokenPass
    @RequestMapping("/profile/{uid}")
    public Result userPage(@PathVariable(name = "uid") int uid,
                           HttpServletRequest request) {
        User user = userService.getUserById(uid);
        if (user == null) {
            return Result.fail().setMsg("用户未登录！");
        }
        //用request的token信息来判断是否是访问别人的主页还是自己的主页
        Map<String, Object> map = new HashMap<>();
        boolean isLogin = isLogin();
        if (isLogin && getUserId() == uid) {
            map.put("isMine", true);
        } else {
            map.put("isMine", false);
        }
        // 点赞数量
        int likeCount = likeService.getUserLikeCount(uid);
        // 关注数量
        long followeeCount = followService.getFolloweeCount(uid);
        map.put("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.getFollowerCount(uid);
        map.put("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (isLogin) {
            hasFollowed = followService.hasFollowed(getUserId(), uid);
        }
        map.put("hasFollowed", hasFollowed);
        map.put("user", user);
        map.put("likeCount", likeCount);
        return Result.success().setData(map);
    }

    /**
     * 查询用户文章
     * @return
     */
    @TokenPass
    @GetMapping("/userPost/{uid}")
    public Result posts(@PathVariable(name = "uid") int uid, @RequestParam(defaultValue = "1") int currentPage) {
        PaginationVo<PostVo> pagination = postService.listByUserId(currentPage, uid);
        Map<String, Object> map = new HashMap<>();
        map.put("pagination", pagination);
        User user = userService.getUserById(uid);
        map.put("user", user);
        return Result.success().setData(map);
    }

/**
     * 忘记密码,往注册邮箱里发送新密码
     *
     * @param user
     * @return
     *//*

    @TokenPass
    @PostMapping("/forget")
    public Result forgetPWD(@RequestBody User user) throws MessagingException {
        String email = user.getEmail();
        user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.fail().setMsg("该邮箱尚未注册");
        }
        //创建新密码
        String newPassword = UUID.randomUUID().toString().substring(0, 10);
        newPassword.replace("-", "v");

        //对密码进行加密
        String md5Pass = SHA.getSHA(newPassword);
        user.setPasswd(md5Pass);
        //更新数据库密码
        userService.updatePasswd(user);

        user.setPasswd(newPassword);
//        //触发忘记密码事件
//        Event event = new Event()
//                .setTopic(TOPIC_FORGET)
//                .setData("user",user);
//        //发送邮件
//        EventHandler.handleTask(event);

        */
/**
         * 忘记密码
         *//*

        mailUtil.forgetMail(user.getEmail(), "忘记密码", user);


        return new Result().success("");
    }


    */
/**
     * 修改密码
     *
     * @return
     *//*

    @PostMapping("/resetPass")
    public Result resetPassword(@RequestBody NewPassDto newPassDto) {
        int userId = getUserId(request);
        //取出数据库旧密码
        String dbOldPass = userService.getUserPasswordById(userId);

        String oldpass = newPassDto.getOldpass();
        oldpass = MD5Util.md5Encryption(oldpass);

        if (!dbOldPass.equals(oldpass)) {
            return new Result().fail("原密码不正确！");
        }
        //修改新密码
        String pass = newPassDto.getPass();
        pass = MD5Util.md5Encryption(pass);
        User user = new User();
        user.setId(userId);
        user.setPassword(pass);
        userService.updatePassword(user);

        return Result.success();
    }
*/


}
