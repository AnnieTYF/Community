package com.tyf.community.dao;

import com.tyf.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MessageMapper {

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信
    List<Message> selectConversations(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表
    List<Message> selectLetters(@Param("conversationId") String conversationId,@Param("offset") int offset,@Param("limit") int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(@Param("userId")int userId, @Param("conversationId")String conversationId);

    //新增消息
    int insertMessage(Message message);

    //修改消息状态
    int updateStatus(@Param("ids") List<Integer> ids,@Param("status") int status);

    //删除消息
    boolean deleteStatus(@Param("id")int id,@Param("status") int status);

}
