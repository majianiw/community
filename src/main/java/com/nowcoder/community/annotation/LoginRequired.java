package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//声明在方法上
@Target(ElementType.METHOD)
//标记运行时使用
@Retention(RetentionPolicy.RUNTIME)
// LoginRequiredInterceptor 这个拦截器可以拦截到所有加了LoginRequired的注解的方法。
// 当这些方法被请求时，LoginRequiredInterceptor的proHandler方法就会被调用
public @interface LoginRequired {


}
