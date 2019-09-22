package com.tyf.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，拦截登录请求，就是不登陆不能访问页面
 * 但是我们这里有一个游客机制，所以有些接口不登陆也可以访问
 * 比如说查看用户头像
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

}
