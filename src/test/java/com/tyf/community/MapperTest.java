package com.tyf.community;

import com.tyf.community.dao.DiscussPostMapper;
import com.tyf.community.dao.MessageMapper;
import com.tyf.community.dao.UserMapper;
import com.tyf.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

   @Autowired
    private UserMapper userMapper;
    @Autowired
     private DiscussPostMapper discussPostMapper;

    @Autowired
    private MessageMapper messageMapper;

 /*   @Test
    public void testInsert(){
         System.out.println(userMapper.selectById(1));
    }
*/

    @Test
    public void testDiscussPost(){
        System.out.println(discussPostMapper.selectDiscussPostRows(0));
        System.out.println(discussPostMapper.selectDiscussPosts(0,1,2));
    }

    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);

    }


}
