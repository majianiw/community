package com.nowcoder.community.util;

/**
 * @Date: create in 14:01 2023/3/15
 * @describe: redis 的键值   关于点赞  redis拼接key
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    //关注的人存进去
    private static final String PREFIX_FOLLOWER = "follower";

    //作为被关注的人的粉丝存进去
    private static final String PREFIX_FOLLOWEE = "followee";

    //验证码的前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";

    //登录凭证的前缀
    private static final String PREFIX_TICKET = "ticket";


    private static final String PREFIX_USER = "user";

    //独立访客，每次访问都被统计
    private static final String PREFIX_UV = "uv";

    //日活跃用户
    private static final String PREFIX_DAU = "dau";

    //帖子热度排序
    private static final String PREFIX_POST = "post";

    //某个实体的赞
    //存储格式: like:entity:entityType:entityId -> set(userId)
    //entityType -- 1: 给帖子的赞，   2: 给评论的赞
    //entityId  -- 被点赞的帖子或评论的Id  它的Value是用户的Id
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    //like:entity:userId  -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体
    //followee:userId:entityType  -> zset(entityId,now(当前时间，可以知道什么时间关注))
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    //follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 验证码的key
    // owner : 用户临时的凭证
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    //单日UV
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    //统计区别UV
    public static String getUVKey(String startDate, String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日活跃用户
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    //区间活跃用户
    public static String getDAUKey(String startDate, String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //帖子分数
    public static String getPostScore(){
        return PREFIX_POST + SPLIT + "score";
    }
}
