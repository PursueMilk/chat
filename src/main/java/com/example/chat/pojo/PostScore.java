package com.example.chat.pojo;

import lombok.Data;

@Data
public class PostScore {

    private Integer id;

    private double score;

    public PostScore() {
    }

    public PostScore(Integer id, double score) {
        this.id = id;
        this.score = score;
    }
}
