package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Date: create in 14:19 2023/3/8
 * @describe:
 */
@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TemplateEngine templateEngine;

    //get请求到register.html文件，然后html中发起请求到@PostMapping("/register")
    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }


    @PostMapping("/register")
    public String register(Model model, User user) throws MessagingException {
        Map<String, Object> map = userService.register(user);
        //注册成功
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一份激活邮件,请尽快激活");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    //激活路径http://localhost:8080/community/activation/${userId}/code
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效的操作，该账号已经激活过了");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    //获取验证码
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response) {/**, HttpSession session**/
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        // session.setAttribute("kaptcha", text);

        //验证码的归属  临时给客户端一个凭证存入cookie
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, /*HttpSession session,*/
                        HttpServletResponse response,@CookieValue("kaptchaOwner")String kaptchaOwner ) {
        // 获取服务端的session中的验证码（也就是图片的验证码）(因为访问太慢 而且验证码只需要存储一会，所以下面采用redis)
        // String kaptcha = (String) session.getAttribute("kaptcha");
        //检查redis中的验证码
        String kaptcha = null;
        System.out.println(kaptchaOwner+"222");
        if(StringUtils.isNotBlank(kaptchaOwner) ){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(redisKey);
            System.out.println(kaptcha +"111");
        }
        //从redis取出验证码
        //比较redis读出的验证码和写入的验证码是否一样
        System.out.println(code +"333");
        if (StringUtils.isEmpty(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login.html";
        }

        //检查账号密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEAFULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    //退出
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    //忘记密码
    @GetMapping("/forget")
    public String getForgetPage(){
        return "/site/forget";
    }

    //获取邮箱验证码
    @GetMapping("/forget/code")
    public String getForgetCode(String email, HttpSession session) throws MessagingException {
        if(StringUtils.isBlank(email)){
            return CommunityUtil.getJSONString(1,"邮箱不能为空");
        }
        //发送邮件
        Context context = new Context();
        context.setVariable("eamil",email);
        String code = CommunityUtil.generateUUID().substring(0,4);
        context.setVariable("verifyCode",code);
        String content = templateEngine.process("/mail/forget",context);
        mailClient.sendMail(email,"找回密码",content);

        //保存验证码
        session.setAttribute("verifyCode",code);
        return CommunityUtil.getJSONString(0);
    }
    //重置密码
    @PostMapping("/forget/password")
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session){
        String code = (String)session.getAttribute("verifyCode");
        if(StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)){
            model.addAttribute("codeMsg","验证码不正确");
            return "site/forget";
        }
        Map<String,Object> map = userService.resetPassword(email,password);
        if(map.containsKey("user")){
            return "site/login";
        }else{
            return "site/forget";
        }
    }
}