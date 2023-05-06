package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @Date: create in 14:50 2023/3/13
 * @describe:
 */
@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentByEntity(int entityType, int enetityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntityId(entityType, enetityId, offset, limit);
    }

    public int findCommentCount(int entityType, int enetityId) {
        return commentMapper.selectCountByEntity(entityType, enetityId);
    }

    //添加评论
    //注释中的参数 ：  隔离级别 和 传播机制
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //添加完之后更新评论数量(评论分为了回复和给帖子评论，那么进就要分辨出给帖子的评论)
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //分辨出来之后,得到该帖子id下的评论总数
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            //然后把得到的评论总数更新到帖子里
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }


}
