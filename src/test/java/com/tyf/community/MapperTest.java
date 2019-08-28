package com.tyf.community;

import com.tyf.community.dao.DiscussPostMapper;
import com.tyf.community.dao.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

   @Autowired
    private UserMapper userMapper;
    @Autowired
     private DiscussPostMapper discussPostMapper;

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


}
