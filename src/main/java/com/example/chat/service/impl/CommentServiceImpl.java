package com.example.chat.service.impl;

import com.example.chat.mapper.CommentMapper;
import com.example.chat.mapper.PostMapper;
import com.example.chat.pojo.Comment;
import com.example.chat.service.CommentService;
import com.example.chat.utils.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 文章评论实现
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 获取评论列表
     */
    @Override
    public List<Comment> getCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    /**
     * 获得评论数量
     */
    @Override
    public int getCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 获得评论的id
     */
    @Override
    public Comment getCommentById(int entityId) {
        return commentMapper.findCommentById(entityId);
    }

    /**
     * 添加评论
     */
    //TODO 判断不为空
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    @Override
    public int addComment(Comment comment) {
        int rows = commentMapper.insertComment(comment);
        if (comment.getEntityType() == ConstantUtil.ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            postMapper.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

}
