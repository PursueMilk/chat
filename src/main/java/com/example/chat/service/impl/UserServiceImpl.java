package com.example.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.chat.async.RabbitProduce;
import com.example.chat.dto.NewPassDto;
import com.example.chat.mapper.UserMapper;
import com.example.chat.pojo.Event;
import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import com.example.chat.service.UserService;
import com.example.chat.utils.ConstantUtil;
import com.example.chat.utils.ImgUtil;
import com.example.chat.utils.MD5Util;
import com.example.chat.utils.RedisKeyUtil;
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
import java.util.regex.Pattern;

import static com.example.chat.utils.RedisKeyUtil.getUserCodeKey;
import static com.example.chat.utils.RedisKeyUtil.getUserTokenKey;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitProduce rabbitProduce;

    @Value("${img.ip}")
    private String ip;

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
            //未激活发送激活邮件
            asyncSend(query);
            return Result.fail().setMsg("用户未激活");
        }
        if (!MD5Util.getMD5(user.getPasswd()).equals(query.getPasswd())) {
            return Result.fail().setMsg("密码错误");
        }
        String uuid = IdUtil.simpleUUID();
        UserVo userVo = BeanUtil.copyProperties(query, UserVo.class);
        //将用户信息保存在redis中
        redisTemplate.opsForValue().set(getUserTokenKey(uuid), userVo, 6, TimeUnit.HOURS);
        return Result.success().setData(uuid);
    }

    @Override
    public Result register(User user) {
        //对用户数据进行验证
        if (!judge(user.getAccount(), user.getPasswd())) {
            return Result.fail().setMsg("账号密码不规范");
        }
        if (!match(user.getNickname(), user.getType())) {
            return Result.fail().setMsg("昵称为空或超过最大长度");
        }
        //正则效验邮箱
        if (Objects.isNull(user.getEmail()) && Pattern.matches(ConstantUtil.EMAIL_PATTERN, user.getEmail())) {
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
        //设置头像地址
        String url = ImgUtil.getImgUrl(ip, port) + "/img/user/";
        if (user.getSex() == 0) {
            url += "boy.png";
        } else {
            url += "girl.png";
        }
        user.setAvatar(url);
        //密码加密
        user.setPasswd(MD5Util.getMD5(user.getPasswd()));
        userMapper.insertUser(user);
        //发送邮件
        asyncSend(user);
        return Result.success();
    }


    @Override
    public Result uploadImg(MultipartFile file, Integer userId) {
        if (file != null) {
            String imgUrl = ImgUtil.upload(ImgUtil.getImgUrl(ip, port), fileName, file);
            userMapper.updateImg(userId, imgUrl);
            return Result.success().setData(imgUrl);
        }
        return Result.fail().setMsg("图片不能为空");
    }

    @Override
    public Result activation(String account, String code) {
        Object object = redisTemplate.opsForValue().get(getUserCodeKey(account));
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
        String oldPasswd = MD5Util.getMD5(newPassDto.getOldPass());
        if (!oldPasswd.equals(user.getPasswd())) {
            return Result.fail().setMsg("原密码不正确");
        }
        user.setPasswd(MD5Util.getMD5(newPassDto.getNewPass()));
        userMapper.updateUser(user);
        return Result.success();
    }

    @Override
    public Result forgetPwd(String email) {
        User user = userMapper.queryUserByEmail(email);
        if (Objects.isNull(user)) {
            return Result.fail().setMsg("该邮箱未注册用户");
        }
        String key = RedisKeyUtil.getUserEmailKey(email);
        //限制同一个邮箱发送邮件的次数，五分钟内只能发送一次
        if (redisTemplate.hasKey(key)) {
            return Result.fail().setMsg("邮件已经发送，请稍后重试！");
        }
        //密码未加密发送
        String newPasswd = RandomUtil.randomNumbers(8);
        user.setPasswd(newPasswd);
        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_FORGET)
                .setData("user", user);
        rabbitProduce.handleTask(event);
        //密码加密
        user.setPasswd(MD5Util.getMD5(newPasswd));
        //更新用户密码
        userMapper.updateUser(user);
        return Result.success();
    }


    /**
     * 对用户名和密码进行判断
     *
     * @param account
     * @param passwd
     * @return
     */
    public boolean judge(String account, String passwd) {
        if (!StringUtils.hasLength(account) || !StringUtils.hasLength(passwd)) {
            return false;
        }
        if (account.length() > 15 || passwd.length() > 15) {
            return false;
        }
        return true;
    }


    /**
     * 对昵称和类型进行判断
     *
     * @param nickname
     * @param type
     * @return
     */
    public boolean match(String nickname, Integer type) {
        if (!StringUtils.hasLength(nickname) || Objects.isNull(type)) {
            return false;
        }
        if (nickname.length() > 20) {
            return false;
        }
        return true;
    }


    /**
     * 异步处理发送邮件
     *
     * @param user
     */
    public void asyncSend(User user) {
        //判断是否已经发送，已经发送不做处理
        if (redisTemplate.hasKey(getUserCodeKey(user.getAccount()))) {
            return;
        }
        Event event = new Event()
                .setTopic(ConstantUtil.TOPIC_REGISTER)
                .setData("user", user);
        rabbitProduce.handleTask(event);
    }


}
