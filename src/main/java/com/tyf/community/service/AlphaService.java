package com.tyf.community.service;

import com.tyf.community.dao.AlphaDao;
import com.tyf.community.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaService {
    @Autowired
    private AlphaDao alphaDao;
    @Autowired
    private UserMapper userMapper;

    public void AlphaService(){
        System.out.println("ConstructAlphaService");
    }
    //表明初始化方法在构造器后执行，管理初始化方法
    @PostConstruct
    public void init(){
        System.out.println("initAlphaService");
    }
    //用注解管理销毁方法
    @PreDestroy
    public void destroy(){
        System.out.println("destroyAlphaService");
    }

    public String find(){
       return alphaDao.select();
    }
}
