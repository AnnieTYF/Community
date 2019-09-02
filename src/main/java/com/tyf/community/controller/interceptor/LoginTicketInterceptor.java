package com.tyf.community.controller.interceptor;

import com.tyf.community.entity.LoginTicket;
import com.tyf.community.entity.User;
import com.tyf.community.service.UserService;
import com.tyf.community.util.CookieUtil;
import com.tyf.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    //在controller之前调用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if(ticket != null){
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户，替代session
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    //在模板之前调用
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
            User user = hostHolder.getUser();
            if(user != null && modelAndView != null){
                modelAndView.addObject("loginUser",user);
            }
    }

    //模板之后,清楚用户数据
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler,
                                Exception ex) {
          hostHolder.clear();
    }
}
