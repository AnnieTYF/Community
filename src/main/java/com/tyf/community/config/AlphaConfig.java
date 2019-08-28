package com.tyf.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

//这是一个配置类
@Configuration
public class AlphaConfig {

    //这个方法返回的对象将被装配到名称为simpleDateFormat的容器里
    @Bean
    public SimpleDateFormat simpleDateFormat(){
        return new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
    }
}
