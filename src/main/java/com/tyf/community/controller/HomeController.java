package com.tyf.community.controller;

import com.tyf.community.entity.DiscussPost;
import com.tyf.community.entity.Page;
import com.tyf.community.entity.User;
import com.tyf.community.service.DiscussPostService;
import com.tyf.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    /**
     * 这是要返回一个首页(HTML页面的方法)所以返回的是路径或者ModelAndView
     * @param model
     * @return
     */
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        /*
        方法调用之前，springMVC会自动的实例化model + page，并将page注入model
        所以thymeleaf中可以直接访问page对象中的数据
         */
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> listDiscussPost = discussPostService.findDiscussPost(0,page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(listDiscussPost != null){
            for(DiscussPost discussPost :listDiscussPost){
                Map<String,Object> map = new HashMap<>();
                map.put("post",discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

}
