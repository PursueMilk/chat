package com.example.chat.pojo;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("用户类")
@Data
public class User {

    @ApiModelProperty("用户编号")
    private Integer id;

    @ApiModelProperty("用户账号")
    private String account;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户邮箱")
    private String email;

    @ApiModelProperty("性别")
    private Integer sex;

    @ApiModelProperty("头像地址")
    private String avatar;

    @ApiModelProperty("密码")
    private String passwd;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("账号类型")
    private Integer type;

}
