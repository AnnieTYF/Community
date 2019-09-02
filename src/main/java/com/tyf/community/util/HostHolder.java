package com.tyf.community.util;

import com.tyf.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 用于线程隔离的工具类,以线程为key存取值，可以看一下它的底层源码，用的map存取
 * 持有用户信息，代替session对象
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User u){
        users.set(u);
    }

    public User getUser(){
        return users.get();
    }

    /*
    清除用户信息
     */
    public void clear(){
        users.remove();
    }
}
