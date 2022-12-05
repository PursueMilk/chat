package com.example.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.chat.async.EventHandler;
import com.example.chat.dto.NewPassDto;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.chat.utils.RedisKeyUtil.*;


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
        if (!judge(user.getAccount(), user.getPasswd())) {
            return Result.fail().setMsg("账号密码不规范");
        }
        User query = userMapper.queryUserByAccount(user.getAccount());
        if (Objects.isNull(query)) {
            return Result.fail().setMsg("账号不存在");
        }
        if (query.getState() != 1) {
            return Result.fail().setMsg("用户未激活");
        }
        if (!SHA.getSHA(user.getPasswd()).equals(query.getPasswd())) {
            return Result.fail().setMsg("密码错误");
        }
        String uuid = IdUtil.simpleUUID();
        UserVo userVo = BeanUtil.copyProperties(query, UserVo.class);
        redisTemplate.opsForValue().set(getUserTokenKey(uuid), userVo, 6, TimeUnit.HOURS);
        return Result.success().setData(uuid);
    }

    @Override
    public Result register(User user) {
        //对用户数据进行验证
        if (!judge(user.getAccount(), user.getPasswd())) {
            return Result.fail().setMsg("账号密码不规范");
        }
        if (!compare(user.getNickname(), user.getType())) {
            return Result.fail().setMsg("昵称为空或超过最大长度");
        }
        //TODO 正则验证
        if (Objects.isNull(user.getEmail())) {
            return Result.fail().setMsg("邮箱为空");
        }
        //判断账号、邮箱是否已存在
        User query = userMapper.queryUserByAccount(user.getAccount());
        if (ObjectUtil.isNotNull(query)) {
            return Result.fail().setMsg("账号已经存在");
        }
        if (userMapper.queryUserCountByEmail(user.getEmail()) != 0) {
            return Result.fail().setMsg("邮箱已经绑定");
        }
        //设置头像地址 TODO 优化
        String url = ImgUtil.getHttpUrl() + port + "/img/user/";
        if (user.getSex() == 0) {
            url += "boy.png";
        } else {
            url += "girl.png";
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
    public Result activation(String account, String code) {
        System.out.println(account);
        Object object = redisTemplate.opsForValue().get(getUserCodeKey(account));
        System.out.println(object);
        System.out.println(code);
        if (ObjectUtil.isNotNull(object) && object.equals(code)) {
            userMapper.updateState(account);
            return Result.success();
        }
        return Result.fail().setMsg("激活失败");
    }

    @Override
    public Result update(User user, String token) {
        userMapper.updateUser(user);
        //查询修改的信息，更新缓存
        User updateUser = userMapper.queryUserById(user.getId());
        UserVo userVo = BeanUtil.copyProperties(updateUser, UserVo.class);
        redisTemplate.opsForValue().set(token, userVo, 6, TimeUnit.HOURS);
        return Result.success();
    }

    @Override
    public User getUserById(Integer userId) {
        return userMapper.queryUserById(userId);
    }

    @Override
    public List<User> getUserByIds(List<Integer> ids) {
        List<User> userByIds = userMapper.queryUserByIds(ids);
        return userByIds;
    }


    @Override
    public Result updatePasswd(Integer userId, NewPassDto newPassDto) {
        User user = userMapper.queryUserPasswdById(userId);
        String oldPasswd = SHA.getSHA(newPassDto.getOldPass());
        if (!oldPasswd.equals(user.getPasswd())) {
            return Result.fail().setMsg("原密码不正确");
        }
        user.setPasswd(SHA.getSHA(newPassDto.getNewPass()));
        userMapper.updateUser(user);
        return Result.success();
    }

    @Override
    public Result forgetPwd(String email) {
        System.out.println(email);
        User user = userMapper.queryUserByEmail(email);
        if (Objects.isNull(user)) {
            return Result.fail().setMsg("该邮箱未注册用户");
        }
        //密码未加密发送
        String newPasswd = RandomUtil.randomNumbers(8);
        user.setPasswd(newPasswd);
        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_FORGET)
                .setData("user", user);
        eventHandler.handleTask(event);
        //密码加密
        user.setPasswd(SHA.getSHA(newPasswd));
        userMapper.updateUser(user);
        return Result.success();
    }



    public boolean judge(String account, String passwd) {
        if (!StringUtils.hasLength(account) || !StringUtils.hasLength(passwd)) {
            return false;
        }
        if (account.length()>15||passwd.length()>15) {
            return false;
        }
        return true;
    }


    public boolean compare(String nickname, Integer type) {
        if (!StringUtils.hasLength(nickname) || Objects.isNull(type)) {
            return false;
        }
        if (nickname.length() > 20) {
            return false;
        }
        return true;
    }


}
