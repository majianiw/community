package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @Date: create in 8:50 2023/3/11
 * @describe: 判断是否登录的拦截器
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;


    // 用于在请求被处理之前进行拦截和处理。
    // 具体来说，它的作用是检查用户是否已经登录，如果没有登录则重定向到登录页面。

    // 前两个参数表示客户端发送的请求和服务器返回的响应，可以通过这些对象获取请求和响应的相关信息，例如请求的URL、请求参数、请求头信息、响应状态码、响应头信息等等
    // handler 就是即将要执行的处理器方法 可以对该处理器方法进行检查和处理
    // 例如当一个controller被执行时(因为在WebMvcConfig中controller方法被拦截了) handler就是那个controller类，
    // 处理器方法（只是handler的一部分）就是controller类的某个方法
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //判断请求方法时否是HandlerMethod  类型
        if (handler instanceof HandlerMethod) {
            //将处理请求的方法强制转换成 HandlerMethod 类型。
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            //获取处理请求的方法上的 LoginRequired 注解。
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //判断 LoginRequired 注解是否存在，以及当前用户是否已经登录，如果未登录则进行下一步处理。
            if (loginRequired != null && hostHolder.getUser() == null) {
                // 重定向到登录页面。
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
