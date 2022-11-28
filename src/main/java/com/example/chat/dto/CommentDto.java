package com.example.chat.dto;

import lombok.Data;

@Data
public class CommentDto {
    private int pid;
    private int currentPage = 1;
}
