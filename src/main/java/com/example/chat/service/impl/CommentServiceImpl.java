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

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Override
    public List<Comment> getCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    @Override
    public int getCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Override
    public Comment getCommentById(int entityId) {
        return commentMapper.findCommentById(entityId);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ,propagation = Propagation.REQUIRED)
    @Override
    public int addComment(Comment comment) {
/*
        if (comment == null){
            throw new CustomException(CustomExceptionCode.COMMENT_ERROR);
        }
*/
        int rows = commentMapper.insertComment(comment);
        if (comment.getEntityType() == ConstantUtil.ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            postMapper.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

/*    @Override
    public String findCommentByIdAndType(int entityType, int entityId) {
        return commentMapper.findCommentByIdAndType(entityType,entityId);
    }*/
}
