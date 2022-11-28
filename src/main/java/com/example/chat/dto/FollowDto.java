package com.example.chat.dto;

import lombok.Data;

//优化
@Data
public class FollowDto {
    private int entityType;
    private int entityId;
}
