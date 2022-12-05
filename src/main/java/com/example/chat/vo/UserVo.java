package com.example.chat.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserVo {

    private Integer id;

    private String account;

    private String nickname;

    private Integer type;

    private String email;

    private Integer sex;

    private String avatar;

    private Date createTime;
}
