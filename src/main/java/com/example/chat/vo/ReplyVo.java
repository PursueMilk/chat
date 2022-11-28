package com.example.chat.vo;

import com.example.chat.pojo.Comment;
import com.example.chat.pojo.User;
import lombok.Data;

@Data
public class ReplyVo {
    Comment reply;
    User user;
    User target;
}
