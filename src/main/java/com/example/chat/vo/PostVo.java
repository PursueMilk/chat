package com.example.chat.vo;

import com.example.chat.pojo.Post;
import com.example.chat.pojo.User;
import lombok.Data;

import java.io.Serializable;

@Data
public class PostVo implements Serializable {
    private Post post;
    private User user;
}
