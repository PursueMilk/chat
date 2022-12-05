package com.example.chat.mapper;

import com.example.chat.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    User queryUserByAccount(String account);

    int queryUserCountByEmail(String email);

    Integer insertUser(User user);

    Integer updateState(String account);

    Integer updateUser(User user);

    User queryUserById(Integer id);

    Integer updateImg(Integer id,String avatar);

    User queryUserPasswdById(Integer userId);

    List<User> queryUserByIds(List<Integer> ids);

    User queryUserByEmail(String email);
}
