package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @Description: 给主界面的帖子分页
* @Date: 2023/3/13 17:55
*/
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;
    @GetMapping("/index")
    public String getIndexPage(Model model, Page page,@RequestParam(name = "orderMode", defaultValue = "0") int orderMode  ) {
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode="+orderMode);
        model.addAttribute("orderMode",orderMode);

        //显示帖子
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);

                long  likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST ,post.getId());

                map.put("likeCount",likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }
    // 拒绝访问时的提示页面
    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }
}
