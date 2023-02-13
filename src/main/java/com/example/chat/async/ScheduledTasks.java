package com.example.chat.async;


import com.example.chat.mapper.PostMapper;
import com.example.chat.mapper.ScoreMapper;
import com.example.chat.pojo.Post;
import com.example.chat.pojo.PostScore;
import com.example.chat.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.example.chat.utils.RedisKeyUtil.POST_CHANGE;
import static com.example.chat.utils.RedisKeyUtil.POST_SCORE;

@Component
public class ScheduledTasks {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ScoreMapper scoreMapper;


    // 校园论坛开始运行的时间
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }

    //每天的凌晨更新榜单
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateScore() {
        if (!redisTemplate.hasKey(POST_CHANGE)) {
            return;
        }
        BoundSetOperations operations = redisTemplate.boundSetOps(POST_CHANGE);
        if (operations.size() == 0) {
            return;
        }
        while (operations.size() > 0) {
            refresh((Integer) operations.pop());
        }
        Long num = redisTemplate.opsForZSet().size(POST_SCORE);
        if (Objects.nonNull(num) && num > 100) {
            redisTemplate.opsForZSet().removeRange(POST_SCORE, 101, num);
        }
    }

    public void refresh(int postId) {
        Post post = postMapper.queryPostById(postId);
        long likeNum = likeService.getPostLikeCount(postId);
        int commentNum = post.getCommentCount();
        //计算权重
        double w = commentNum * 10 + likeNum * 2;
        //最后的分值
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        if (Objects.isNull(scoreMapper.findPostScore(postId))){
            scoreMapper.insertPostScore(new PostScore(postId,score));
        }else {
            scoreMapper.updatePostScore(new PostScore(postId,score));
        }
        redisTemplate.opsForZSet().add(POST_SCORE, postId, score);
    }
}
