package com.example.chat.config;

import com.example.chat.interceptor.TokenInterceptor;
import com.example.chat.utils.ImgUtil;
import com.example.chat.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Autowired
    private RedisUtil redisUtil;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/**").addResourceLocations("file:" + ImgUtil.getLocalUrl() + File.separator);
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //token刷新
        registry.addInterceptor(new TokenInterceptor(redisUtil))
                .addPathPatterns("/**");
    }
}
