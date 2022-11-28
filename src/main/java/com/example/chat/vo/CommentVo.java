package com.example.chat.vo;


import com.example.chat.pojo.Comment;
import com.example.chat.pojo.User;
import lombok.Data;

import java.util.List;

@Data
public class CommentVo {
    Comment comment;
    User user;
    List<ReplyVo> replies;
    int replyCount;
}
