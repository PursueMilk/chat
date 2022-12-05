package com.example.chat.controller;

import com.example.chat.annotion.TokenPass;
import com.example.chat.dto.CommentDto;
import com.example.chat.pojo.Post;
import com.example.chat.pojo.Result;
import com.example.chat.service.*;
import com.example.chat.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Api(tags = "文章接口")
@RestController
@RequestMapping("/post")
public class PostController extends BaseController {

    @Autowired
    private PostService postService;

    @ApiOperation(value = "发表文章")
    @PostMapping("/publish")
    public Result publish(@RequestBody Post post) {
        post.setUserId(getUserId());
        return postService.publish(post);
    }


    @ApiOperation(value = "首页文章列表")
    @TokenPass
    @GetMapping("/list")
    public Result posts(@RequestParam(defaultValue = "1") int currentPage, @RequestParam(defaultValue = "0") int listMode) {
        PaginationVo<PostVo> pagination = postService.posts(currentPage, listMode);
        return Result.success().setData(pagination);
    }


    @ApiOperation(value = "文章详情")
    @TokenPass
    @GetMapping("/detail/{pid}")
    public Result postDetail(@PathVariable("pid") int pid) {
        //判断是否登录
        return isLogin() ? postService.postDetail(pid, getUserId(), true) : postService.postDetail(pid, -1, false);
    }


    @ApiOperation(value = "评论列表")
    @TokenPass
    @PostMapping("/comment/list")
    public Result CommentList(@RequestBody CommentDto commentDto) {
        return isLogin() ? postService.CommentList(commentDto, getUserId(), true) : postService.CommentList(commentDto, -1, false);
    }


    @ApiOperation(value = "上传图片")
    @PostMapping("/uploadImg")
    public Result uploadImg(@RequestPart MultipartFile file) {
        return postService.uploadImg(file);
    }


    @TokenPass
    @ApiOperation("查询信息")
    @GetMapping("/search")
    public Result searchPost(@RequestParam("keyword") String message, @RequestParam(defaultValue = "1") int currentPage) {
        System.out.println(message);
        return postService.search("%"+message+"%", currentPage);
    }


    //TODO 管理员

}
