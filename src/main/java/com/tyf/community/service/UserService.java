package com.tyf.community.service;

import com.google.code.kaptcha.Producer;
import com.tyf.community.dao.LoginTicketMapper;
import com.tyf.community.dao.UserMapper;
import com.tyf.community.entity.LoginTicket;
import com.tyf.community.entity.User;
import com.tyf.community.util.CommunityConstant;
import com.tyf.community.util.CommunityUtil;
import com.tyf.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int userId){
        return userMapper.selectById(userId);
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //控制处理
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
             map.put("userNameMsg","账号不能为空");
             return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("userNameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // http://localhost:8080/community/activation/101/code
        /*
        这个userId跟mybatis的配置有关，我们这里设置了mybatis id回填，所以在inserr后user里是有id的
        我之前是在数据库id自增，但是这样如果我想获取用户id的时候就只能再一次获取id
        效率较低
         */
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    /**
     * 邮箱激活
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            //已被激活
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }

    }

    /**
     * 登录验证
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    //更新用户头像路径
    public int updateHeader(int userId, String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }


    /**
     * 忘记密码后获取验证码验证
     * @param email
     * @return
     */
    public Map<String,Object> emailForgetPassword(String email){
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱为空");
            return map;
        }
        //查询对应的用户
        User user = userMapper.selectByEmail(email);
        System.out.println(user);
        if(user == null){
            System.out.println("123");
            map.put("emailMsg","该邮箱未被注册，请先注册账号");
            return map;
        }

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",email);
        /*
         将用户的验证码发到用户邮箱
         */
        String text = user.getSalt();
        context.setVariable("text",text);
        String content = templateEngine.process("/mail/forget",context);
        mailClient.sendMail(email, "激活账号", content);

        return map;
    }

    /**
     * 忘记密码
     * @param email
     * @param salt
     * @param password
     * @return
     */
    public Map<String,Object> forgetPassword(String email, String salt, String password){
        Map<String,Object> map = new HashMap<>();
        User user = userMapper.selectByEmail(email);
        if(StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        if(user == null){
            map.put("emailMsg","该邮箱未被注册，请先注册账号");
            return map;
        }
        if(StringUtils.isBlank(salt)){
            map.put("saltMsg","验证码不能为空");
            return map;
        }
        if(!salt.equals(user.getSalt())){
            map.put("saltMsg","验证码不正确");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        userMapper.updatePassword(user.getId(),CommunityUtil.md5(password + user.getSalt()));

        return map;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

}
