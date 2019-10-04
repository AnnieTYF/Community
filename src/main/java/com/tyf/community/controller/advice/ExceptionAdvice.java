package com.tyf.community.controller.advice;

import com.tyf.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * 用于统一处理Controller异常请求
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e , HttpServletRequest request, HttpServletResponse response) throws IOException {
          logger.error("服务器发生异常："+ e.getMessage());
          for(StackTraceElement element : e.getStackTrace()){
              //打印栈中的错误信息
              logger.error(element.toString());
          }

          //获取请求的方式 JSON/POST
          String xRequestedWith = request.getHeader("x-requested-with");
          if("XMLHttpRequest".equals(xRequestedWith)){
              //如果是json格式，我们返回的是普通字符串，让浏览器手动转化成json
              response.setContentType("application/plain;charset=utf-8");
              PrintWriter writer = response.getWriter();
              writer.write(CommunityUtil.getJSONString(1,"服务器异常"));
          }else{
              //普通的post访问，跳转到异常页面
              response.sendRedirect(request.getContextPath() + "/error");
          }
    }

}
