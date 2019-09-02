package com.tyf.community.controller;

import com.tyf.community.service.AlphaService;
import com.tyf.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/getData")
    @ResponseBody
    public String getData(){
       return alphaService.find();
    }

    @RequestMapping("/hello")
    @ResponseBody
    public  String sayHello(){
        return "HELLO";
    }

    // /students?current=1 & limit=2
    @RequestMapping(path = "/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@RequestParam(name = "current",required = false,defaultValue = "1") int current,
                              @RequestParam(name = "limit",required = false, defaultValue = "1") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // /students/123
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a students";
    }

    //test of post
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,  int age){
        System.out.println(name + age);
        return "save students";
    }

    //响应HTML
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name","lin");
        modelAndView.addObject("age","24");
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    //响应HTML2
    @RequestMapping(path = "/teacher2", method = RequestMethod.GET)
    public String getTeacher2(Model model){
        model.addAttribute("name","lin2");
        model.addAttribute("age","24");
        return "/demo/view";
    }

    //响应JSON（异步请求）部分刷新
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getEmp(){
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","Li");
        emp.put("age","12");
        return emp;
    }

    //cookie示例
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse httpServletResponse){
        // 创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置cookie生效的范围
        cookie.setPath("/community/alpha");
        // 设置cookie的生存时间
        cookie.setMaxAge(60 * 10);
        // 发送cookie
        httpServletResponse.addCookie(cookie);

        return "set cookie";
    }

    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code")String code){
        System.out.println(code);
        return "get cookie";
    }

    // session示例
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session) {
        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";
    }

    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id" ));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

}
