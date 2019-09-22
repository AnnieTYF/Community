package com.tyf.community.controller;

import com.google.code.kaptcha.Producer;
import com.tyf.community.entity.User;
import com.tyf.community.service.UserService;
import com.tyf.community.util.CommunityConstant;
import com.tyf.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    /**
     * 获取登陆页面
     * @return
     */
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    /**
     * 获取忘记密码页面
     * @return
     */
    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String getForgetPage(){
        return "/site/forget";
    }

    @RequestMapping(path = "register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件,请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("userNameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 忘记密码功能
     * 这几个参数都是我瞎写的，因为我不知道前端该怎么传进来
     * @param model
     * @param email
     * @param code
     * @param password
     * @return
     */
    @RequestMapping(path = "forgetPassword", method = RequestMethod.POST)
    public String forgetPassword(Model model, String email, String code, String password){
         Map<String,Object> map = userService.forgetPassword(email,code,password);
         if(map == null || map.isEmpty()){
             model.addAttribute("msg","密码修改成功。请重新登录");
             model.addAttribute("target","/index");
             return "/site/operate-result";
         }else{
             model.addAttribute("saltMsg",map.get("saltMsg"));
             model.addAttribute("passwordMsg",map.get("passwordMsg"));
             model.addAttribute("emailMsg",map.get("emailMsg"));
             return "/site/forget";
         }
    }

    /**
     * 忘记密码后获取验证码验证
     * 我还是不会用前端的ajax传数据
     * @param email
     * @return
     */
    @RequestMapping(path = "/emailForgetPassword",method = RequestMethod.POST)
    @ResponseBody
    public String emailForgetPassword(String email){
        System.out.println(email);
        Map<String,Object> map = userService.emailForgetPassword(email);
        return CommunityUtil.getJSONString(0,"发布成功",map);
    }

    /**
     *  激活// http://localhost:8080/community/activation/101/code
     * @param model
     * @return
     */
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId,code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功");
            model.addAttribute("target","/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，重复激活");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    /**
     * 获取验证码
     * @return
     */
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入swssion
        session.setAttribute("kaptcha",text);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }

    }

    /**
     * 登录验证
     * @param username
     * @param password
     * @param code
     * @param rememberMe
     * @param model
     * @param response
     * @param session
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, HttpServletResponse response, HttpSession session){
        //验证码
        String kaptcha = (String)session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        //账号 + 密码
        int expiredTime = rememberMe ? REMEMBER_EXPIRED_SECONDS:DEFAULR_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,expiredTime);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredTime);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }

    }

    /**
     * 退出登录
     * @param ticket
     * @return
     */
    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket")String ticket){
         userService.logout(ticket);
         return "redirect:/login";
    }





}
