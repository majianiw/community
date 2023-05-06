package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * @Date: create in 16:50 2023/3/14
 * @describe: 这个类是一个基于Spring MVC的异常处理类，用于处理在控制器(Controller)中出现的异常。具体来说，
 *           它使用@ControllerAdvice注解来标记自己是一个全局控制器通知，
 *           意味着它将处理由@Controller注解标记的控制器抛出的异常。
 */
//控制器通知
@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class ExceptionAdvice {

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request , HttpServletResponse response) throws IOException {
        log.error("服务器发生异常: ",e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            log.error(element.toString());
        }

        // 对Ajax请求作出响应，如果请求是一个Ajax请求，
        // 它将返回一个带有错误消息的JSON格式字符串，否则将重定向到应用程序的错误页面。
        String xRequestWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestWith)){
            response.setContentType("application/plain;charSet=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }

    }

}
