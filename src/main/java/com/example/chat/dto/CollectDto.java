package com.example.chat.dto;

import lombok.Data;


//TODO 简化
@Data
public class CollectDto {

    private int entityId;//被收藏的文章ID
    private int entityUserId; //被收藏的文章作者ID

}
