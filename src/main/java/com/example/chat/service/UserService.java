package com.example.chat.service;

import com.example.chat.pojo.Result;
import com.example.chat.pojo.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    Result login(User user);

    Result register(User user);

    Result uploadImg(MultipartFile file,Integer userId);

    Result activation(String username, String code);


    Result update(User user);

    User getUserById(Integer userId);

    List<User> getUserByIds(List<Integer> ids);

    User getUserByEmail(String email);

    void updatePasswd(User user);
}
