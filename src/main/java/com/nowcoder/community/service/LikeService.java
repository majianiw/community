package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @Date: create in 14:08 2023/3/15
 * @describe: 点赞
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;


    //点赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        //拼出来要存的key
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        //判断是否点过赞了，后面的方法就是判断这个key下是否有这个用户ID，如果有就删除（就是取消点赞）,没有添加这个用户Id
//        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (isMember) {
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }

//        SessionCallback 对象，用于在 Redis 连接的上下文中执行一连串的 Redis 操作。
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                //被点赞
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean isMember = operations.opsForSet().isMember(entityLikeKey,userId);

                //开启事务
                operations.multi();
                //判断是否被赞
                if(isMember){
                    //取消点赞  并且减少这个实体（评论或者帖子）所属用户获赞的数量
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                }else{
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);

                }

                //operations.exec() 方法提交 Redis 事务
                return operations.exec();
            }
        });
    }

    //查询某实体(比如一个帖子 或者 一个评论)点赞数量
    public long findEntityLikeCount(int entityType, int entityId) {

        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某个实体点赞的状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //返回1 表示点赞，返回0表示没点赞
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }


    //查询某个用户获赞的数量
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0:count.intValue();
    }
}
