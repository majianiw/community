package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Date: create in 20:15 2023/3/15
 * @describe: 关注和被关注
 */
@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //关注
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                //事务
                operations.multi();
                //添加
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());


                return operations.exec();
            }
        });
    }

    //取消关注
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);


                return operations.exec();
            }
        });
    }

    //查询某个用户关注的实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        //api的使用
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询实体的粉丝的数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //显示当前用户是否关注了某个用户
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询某个用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        //拼接key
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //排序,offset 是起始位置，下面的是结束位置
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if(targetIds == null){
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap();
            //查询关注用户
            User user = userService.findUserById(targetId);
            map.put("user",user);
            //查询关注时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;

    }


    //查询某个用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        //拼接key
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);

        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if(targetIds == null){
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap();
            //查询关注用户
            User user = userService.findUserById(targetId);
            map.put("user",user);
            //查询关注时间
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}

