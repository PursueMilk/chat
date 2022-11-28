package com.example.chat.vo;

import com.example.chat.pojo.Message;
import com.example.chat.pojo.User;
import lombok.Data;

@Data
public class MessageVo {
    private Message conversation;
    private int letterCount;
    private int unreadCount;
    private User target;
}
