package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Date: create in 14:40 2023/3/13
 * @describe: 评论entity
 */

@Data
public class Comment {

    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private Date createTime;
    private int status;
}
