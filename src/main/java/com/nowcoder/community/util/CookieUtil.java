package com.nowcoder.community.util;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Date: create in 15:34 2023/3/10
 * @describe:
 */

@Component
public class CookieUtil {

        public String getValue(HttpServletRequest request,String name){
            if(request == null || name == null){
                throw new RuntimeException("参数为空");
            }
            Cookie[] cookies = request.getCookies();
            if(cookies != null){
                for (Cookie cookie : cookies) {
                    if(cookie.getName().equals(name)){
                        return cookie.getValue();
                    }
                }
            }
            return null;
        }

}
