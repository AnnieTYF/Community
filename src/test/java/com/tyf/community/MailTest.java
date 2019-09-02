package com.tyf.community;

import com.tyf.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
 /*   @Test
    public void testMail(){
        mailClient.sendMail("2601042086@qq.com","TO 蔡荣镔:","TEST 1.0");
    }*/
    @Test
    public void testHTMLMail(){
        Context context = new Context();
        context.setVariable("username","it's a testing message from Annie");
        String content = templateEngine.process("/mail/demo",context);
        mailClient.sendMail("454890065@qq.com","TO Helen:",content);
    }
}
