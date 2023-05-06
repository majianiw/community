package com.nowcoder.community.util;

/**
 * @Date: create in 15:41 2023/3/8
 * @describe:
 */
public interface CommunityConstant {
    /**
     * @Description: 激活成功
     * @Date: 2023/3/8 15:41
     */
    int ACTIVATION_SUCCESS = 0;
    /**
     * @Description: 重复激活
     * @Date: 2023/3/8 15:42
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * @Description: 激活失败
     * @Date: 2023/3/8 15:42
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * @Description: 默认状态的登录凭证超时时间
     * @Date: 2023/3/10 14:26
     */

    int DEAFULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * @Description: 记住  状态下的登陆凭证超时时间
     * @Date: 2023/3/10 14:27
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * @Description: 实体类型：帖子
     * @Date: 2023/3/13 14:59
     */
    int ENTITY_TYPE_POST = 1;
    /**
     * @Description: 实体类型：评论
     * @Date: 2023/3/13 14:59
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * @Description: 用户
     * @Date: 2023/3/15 20:44
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * @Description: 主题（系统通知）：点赞
     * @Date: 2023/3/20 14:55
     */
    String TOPIC_LIKE = "like";
    /**
     * @Description: 主题：评论
     * @Date: 2023/3/20 14:55
     */
    String TOPIC_COMMENT = "comment";
    /**
     * @Description: 主题：关注
     * @Date: 2023/3/20 14:56
     */
    String TOPIC_FOLLOW = "follow";
    /**
    * @Description: 主题: 发帖
    * @Date: 2023/3/22 15:51
    */
    String TOPIC_PUBLISH = "publish";

    /**
    * @Description: 主题： 删帖
    * @Date: 2023/3/24 14:24
    */
    String TOPIC_DELETE = "delete";
    /**
     * @Description: 系统用户
     * @Date: 2023/3/20 14:59
     */
    int SYSTEM_USER_ID = 1;
    /**
     * @Description: 权限 普通用户
     * @Date: 2023/3/21 11:53
     */
    String AUTHORITY_USER = "user";
    /**
     * @Description: 权限 管理员
     * @Date: 2023/3/21 11:54
     */
    String AUTHORITY_ADMIN = "admin";
    /**
     * @Description: 权限 版主
     * @Date: 2023/3/21 11:55
     */
    String AUTHORITY_MODERATOR = "moderator";

}
