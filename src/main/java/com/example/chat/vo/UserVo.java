package com.example.chat.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserVo {

    private Integer id;

    private String username;

    private Integer sex;

    private String avatar;

    private Date createTime;
}
