package com.example.chat.mapper;

import com.example.chat.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    User queryUser(User user);

    Integer insertUser(User user);

    User queryEmail(String email);


    Integer updateState(String username);

    Integer updateUser(User user);

    Integer updateImg(Integer id,String avatar);


    User queryUserById(Integer id);

    User queryUserByEmail(String email);
}
