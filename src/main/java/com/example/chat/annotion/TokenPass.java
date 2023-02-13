package com.example.chat.annotion;


import java.lang.annotation.*;


/**
 * 用于标识不需要登录的请求
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TokenPass{
}
