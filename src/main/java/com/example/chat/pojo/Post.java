package com.example.chat.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;


import java.io.Serializable;
import java.util.Date;


@Data
public class Post implements Serializable {
    private Integer id;

    private Integer userId;

    private String title;

    private String content;

    private int type;

    private int tag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    //TODO 是否用到
    private String createTimeStr;

    private int commentCount;

    private long likeCount;

    private long collectCount;

    private double score;

    private int status;
}
