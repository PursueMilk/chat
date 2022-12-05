package com.example.chat.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@ApiModel
@Data
public class Comment implements Serializable {

    @ApiModelProperty("评论编号")
    private int id;
    @ApiModelProperty("评论用户")
    private int userId;
    @ApiModelProperty("评论类型")
    private int entityType; //被评论实体的类型  1-帖子  2-评论
    @ApiModelProperty("被评论的编号")
    private int entityId;
    @ApiModelProperty("被评论作者的编号")
    private int targetId;
    @ApiModelProperty("评论内容")
    private String content;
    @ApiModelProperty("状态")
    private int status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private long likeCount;
    private int likeStatus;
    //便于前端数据交互
    private int postId;//冗余属性，方便通知而已
}
