package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Date: create in 16:02 2023/3/27
 * @describe: 帖子分数刷新
 */

@Slf4j
public class PostScoreRefreshJob implements CommunityConstant , Job {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Date epoch;
    static{
        try {
            epoch=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 12:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败",e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        String redisKey = RedisKeyUtil.getPostScore();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if(operations.size() == 0){
            log.info("任务取消，没有需要刷新的帖子");
            return ;
        }

        log.info("任务开始，正在刷新帖子分数" + operations.size());

        while(operations.size() > 0){
            this.refresh((Integer)operations.pop());
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("任务结束，帖子分数刷新完毕");
    }

    private void refresh(Integer postId){
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if(post == null || post.getStatus() == 2){
            log.error("该帖子不存在 id =  " + postId);
            return;
        }
        //得到是否加精     点赞数量    评论数量
        boolean flag = post.getStatus() == 1;

        int commentCount = post.getCommentCount();

        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        //计算权重
        double w = (flag? 75:0) + commentCount *2 + likeCount * 2;

        //分数:   权重+ 距离天数   不能让权重 <= 0
        double score = Math.log10(Math.max(w,1)) + (post.getCreateTime().getTime() - epoch.getTime()) / (3600 * 1000 * 24);

        //更新帖子分数
        discussPostService.updateScore(postId,score);

        //同步搜索的数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}





