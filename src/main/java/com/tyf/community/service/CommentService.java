package com.tyf.community.service;

import com.tyf.community.dao.CommentMapper;
import com.tyf.community.entity.Comment;
import com.tyf.community.util.CommunityConstant;
import com.tyf.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 事务管理，使这个方法中
     * 1.添加评论
     * 2.添加帖子的评论数
     * 两个功能要不都完成，要不一个都不行
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //更新帖子的评论数
        if(comment.getEntityId() == ENTITY_TYPE_POST){
            //我觉得这样很OK，比自己手动+1要好
           int count =  commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
           discussPostService.updateCommentCount(comment.getEntityId(),count);
        }


        return rows;
    }
}
