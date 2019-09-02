package com.tyf.community.dao;

import com.tyf.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(@Param("id")int id, @Param("status") int status);

    int updateHeader(@Param("id")int id, @Param("headUrl")String headUrl);

    int updatePassword(@Param("id")int id, @Param("password")String password);
}
