package com.example.chat.controller;

import com.example.chat.annotion.TokenPass;
import com.example.chat.dto.CommentDto;
import com.example.chat.pojo.Post;
import com.example.chat.pojo.Result;
import com.example.chat.service.*;
import com.example.chat.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/post")
public class PostController extends BaseController {

    @Autowired
    private PostService postService;

    @PostMapping("/publish")
    public Result publish(@RequestBody Post post) {
        Integer id = getUserId();
        return postService.publish(post, id);
    }


    /**
     * 分页展示推文
     * listMode代表是最热还是最热
     *
     * @return
     */
    @TokenPass
    @GetMapping("/list")
    public Result posts(@RequestParam(defaultValue = "1") int currentPage, @RequestParam(defaultValue = "0") int listMode) {
        PaginationVo<PostVo> pagination = postService.posts(currentPage, listMode);
        return Result.success().setData(pagination);
    }


    /**
     * 帖子详情
     *
     * @param pid
     * @return
     */
    @TokenPass
    @GetMapping("/detail/{pid}")
    public Result postDetail(@PathVariable("pid") int pid) {
        //判断是否登录
        return isLogin() ? postService.postDetail(pid, getUserId(), true) : postService.postDetail(pid, -1, false);
    }


    /**
     * 查询帖子评论
     *
     * @param commentDto
     * @return
     */
    @TokenPass
    @PostMapping("/comment/list")
    public Result CommentList(@RequestBody CommentDto commentDto) {
        return isLogin() ? postService.CommentList(commentDto, getUserId(), true) : postService.CommentList(commentDto, -1, false);
    }


    @PostMapping("/uploadImg")
    public Result uploadImg(@RequestPart MultipartFile file) {
        return postService.uploadImg(file);
    }

    //TODO 管理员

}
