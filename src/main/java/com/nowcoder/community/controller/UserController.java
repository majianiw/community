package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @Date: create in 16:40 2023/3/10
 * @describe: 用户Controller
 */
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController implements CommunityConstant {

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

//    @Value("${qiniu.key.access}")
//    private String accessKey;
//
//    @Value("${qiniu.key.secret}")
//    private String secretKey;
//
//    @Value("${qiniu.bucket.header.name}")
//    private String headerBucketName;
//
//    @Value("${qiniu.bucket.header.url}")
//    private String headerBucketUrl;

    @LoginRequired
    @GetMapping("/setting")
    //跳转到注册页
    public String getSettingPage(Model model) {

        //上传到七牛云服务器
//        //生成上传文件的名称
//        String fileName = CommunityUtil.generateUUID();
//
//        //设置响应信息
//        StringMap policy = new StringMap();
//        policy.put("returnBody", CommunityUtil.getJSONString(0));
//
//        //生成上传的凭证
//        Auth auth = Auth.create(accessKey, secretKey);
//        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
//
//        model.addAttribute("uploadToken", uploadToken);
//        model.addAttribute("fileName", fileName);

        return "site/setting";
    }

    //更新头像的路径（七牛云）
//    @PostMapping("/header/url")
//    @ResponseBody
//    public String updateHeaderUrl(String fileName) {
//        if (fileName == null) {
//            return CommunityUtil.getJSONString(1, "文件名不能为空!");
//        }
//
//        String url = headerBucketUrl + "/" + fileName;
//        userService.updateHeader(hostHolder.getUser().getId(), url);
//        return CommunityUtil.getJSONString(0);
//    }


    //上传头像
    //这个自定义注解 用来判断是否登录
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHandler(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isEmpty(suffix)) {
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("上传文件失败", e.getMessage());
        }
        //更新当前用户头像路径(web访问路径)
        //localhost:8080/community/user/header/xxx.png;
        User user = hostHolder.getUser();
        //localhost:8080/community/user/header/
        String headUrl = domain + contextPath + "/user/header/" + fileName;
        int row = userService.updateHeader(user.getId(), headUrl);
        if (row == 0) {
            throw new RuntimeException("头像更新失败");
        }

        return "redirect:/index";
    }

    @Deprecated
    @GetMapping("/header/{fileName}")
    //获取自身头像
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器上存放的路径
        fileName = uploadPath + "/" + fileName;
        // 文件的后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片文件
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("读取头像失败: ", e.getMessage());
        }
    }

    //更改密码
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map.containsKey("user")) {
            //更改了就要重新登陆
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            //更新失败就重新设置
            return "/site/setting";
        }
    }

    //个人主页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        //用户
        model.addAttribute("user", user);

        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);

        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //查询是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";

    }
}
