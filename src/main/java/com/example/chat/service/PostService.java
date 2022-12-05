package com.example.chat.service;

import com.example.chat.dto.CommentDto;
import com.example.chat.pojo.Post;
import com.example.chat.pojo.Result;
import com.example.chat.vo.PaginationVo;
import com.example.chat.vo.PostVo;
import org.springframework.web.multipart.MultipartFile;

public interface PostService {
    Result publish(Post post);

    PaginationVo<PostVo> posts(int currentPage, int listMode);

    Result postDetail(int pid,int userId,boolean state);

    Result CommentList(CommentDto commentDto, int userId, boolean state);

    Result uploadImg(MultipartFile file);

    Post getPostById(Integer targetId);

    PaginationVo<PostVo> listByUserId(int currentPage, int uid);

    Result search(String message,int currentPage);
}
