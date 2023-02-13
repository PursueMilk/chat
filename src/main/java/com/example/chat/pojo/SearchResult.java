package com.example.chat.pojo;


import lombok.ToString;

import java.util.List;

@ToString
public class SearchResult {
    private List<Post> list;
    private long total;

    public SearchResult(List<Post> list, long total) {
        this.list = list;
        this.total = total;
    }

    public List<Post> getList() {
        return list;
    }

    public void setList(List<Post> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}