package com.example.chat.vo;

import lombok.Data;

import java.util.List;

@Data
public class PostDetailVo {

    PostVo postVo;

    List<CommentVo> comments;

    private int likeStatus;

    private int collectStatus;
}
