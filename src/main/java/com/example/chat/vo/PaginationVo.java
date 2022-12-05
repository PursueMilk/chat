package com.example.chat.vo;

import com.example.chat.pojo.User;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PaginationVo<T> implements Serializable {

    private List<T> records;
    private int total;
    private int currentPage;
    private int pageSize;

    //无用
    private int letterUnreadCount;
    private int noticeUnreadCount;
    //目标
    private User target;
}
