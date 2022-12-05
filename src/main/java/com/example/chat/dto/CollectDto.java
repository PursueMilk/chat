package com.example.chat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel("收藏类")
@Data
public class CollectDto {

    @ApiModelProperty("被收藏的文章ID")
    private int entityId;

/*    @ApiModelProperty("被收藏的文章作者的ID")
    private int entityUserId;*/
}
