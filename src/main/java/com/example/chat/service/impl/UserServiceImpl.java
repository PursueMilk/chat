package com.example.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.example.chat.async.EventHandler;
import com.example.chat.mapper.UserMapper;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.UserService;
import com.example.chat.utils.*;
import com.example.chat.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.chat.utils.RedisKeyUtil.PREFIX_USER_CODE;
import static com.example.chat.utils.RedisKeyUtil.PREFIX_USER_TOKEN;



@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EventHandler eventHandler;

    @Value("${server.port}")
    private String port;

    private String fileName = "user";

    @Override
    public Result login(User user) {
        if (!judge(user.getUsername(), user.getPasswd())) {
            return Result.fail().setMsg("用户名密码为空或超过最大长度");
        }
        User query = userMapper.queryUser(user);
        if (Objects.isNull(query)) {
            return Result.fail().setMsg("用户名不存在");
        }
        if (query.getState() != 1) {
            return Result.fail().setMsg("用户未激活");
        }
        if (!SHA.getSHA(user.getPasswd()).equals(query.getPasswd())) {
            return Result.fail().setMsg("密码错误");
        }
        String uuid = IdUtil.simpleUUID();
        UserVo userVo = BeanUtil.copyProperties(query, UserVo.class);
        redisTemplate.opsForValue().set(PREFIX_USER_TOKEN + uuid, userVo, 2, TimeUnit.HOURS);
        return Result.success().setData(uuid);
    }

    @Override
    public Result register(User user) {
        if (!judge(user.getUsername(), user.getPasswd())) {
            return Result.fail().setMsg("用户名密码为空或超过最大长度");
        }
        if (user.getEmail() == null) {
            return Result.fail().setMsg("邮箱为空");
        }
        User name = userMapper.queryUser(user);
        if (name != null) {
            return Result.fail().setMsg("用户名已经存在");
        }
        User email = userMapper.queryEmail(user.getEmail());
        if (email != null) {
            return Result.fail().setMsg("邮箱已经绑定");
        }
        //设置头像地址 TODO 优化
        String url = ImgUtil.getHttpUrl() + port + "/img/user/";
        if (user.getSex()==0){
            url+="boy.png";
        }else {
            url+="girl.png";
        }
        user.setAvatar(url);
        //密码加密
        user.setPasswd(SHA.getSHA(user.getPasswd()));
        userMapper.insertUser(user);
        //发送邮件
        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_REGISTER)
                .setData("user", user);
        eventHandler.handleTask(event);
        return Result.success();
    }

    @Override
    public Result uploadImg(MultipartFile file, Integer userId) {
        if (file != null) {
            String imgUrl = ImgUtil.upload(port, fileName, file);
            userMapper.updateImg(userId, imgUrl);
            return Result.success().setData(imgUrl);
        }
        return Result.fail().setMsg("图片不能为空");
    }

    @Override
    public Result activation(String username, String code) {
        Object object = redisTemplate.opsForValue().get(PREFIX_USER_CODE +username);
        if (!Objects.isNull(object) && object.equals(code)) {
            userMapper.updateState(username);
            return Result.success();
        }
        return Result.fail().setMsg("激活失败");
    }

    @Override
    public Result update(User user) {
        userMapper.updateUser(user);
        return Result.success();
    }

    @Override
    public User getUserById(Integer userId) {
        return userMapper.queryUserById(userId);
    }

    @Override
    public List<User> getUserByIds(List<Integer> ids) {
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.queryUserByEmail(email);
    }

    @Override
    public void updatePasswd(User user) {

    }


    public Boolean judge(String username, String passwd) {
        if (!StringUtils.hasLength(username) || !StringUtils.hasLength(passwd)) {
            return false;
        }
        if (username.length() > 20 || passwd.length() > 20) {
            return false;
        }
        return true;
    }


}
