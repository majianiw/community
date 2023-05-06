package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Date: create in 14:42 2023/3/13
 * @describe: 评论mapper
 */
@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntityId(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType, int entityId);

    //增加评论
    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
