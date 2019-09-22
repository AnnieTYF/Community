package com.tyf.community.controller;

import com.tyf.community.annotation.LoginRequired;
import com.tyf.community.entity.User;
import com.tyf.community.service.UserService;
import com.tyf.community.util.CommunityUtil;
import com.tyf.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /**
     * 上传图片接口
     * @param headerImage
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
         if(headerImage == null){
             model.addAttribute("error", "未选择图片");
             return "/site/setting";
         }

         String filename = headerImage.getOriginalFilename();
         String suffix = filename.substring(filename.lastIndexOf("."));
         if(StringUtils.isBlank(suffix)){
             model.addAttribute("error", "文件的格式不正确");
             return "/site/setting";
         }
         //生成随机文件名
          filename = CommunityUtil.generateUUID()+ suffix;
         //确定文件存放路径
        File file = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常", e);
        }
        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /**
     * 获取图片接口
     * @param fileName
     * @param response
     */
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //响应图片
        response.setContentType("image/" + suffix);

        //这是java7的一个特殊语法，用来自动关闭输入流，输出流由springmvc控制关闭
        //当然也可以选择在finally里关闭，不过这样需要 FileInputStream在异常外定义
        try(  //文件的输出流
              OutputStream outputStream = response.getOutputStream();
              //文件的输入流
              FileInputStream fileInputStream = new FileInputStream(fileName);)
        {

            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,b);
            }

        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }finally {

        }


    }

}
