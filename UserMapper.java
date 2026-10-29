package com.studio.jwt.mapper;

import com.studio.jwt.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** DAO：数据访问，SQL 在 UserMapper.xml（#{ } 预编译，防注入） */
@Mapper
public interface UserMapper {

    int insert(User user);

    User selectByUsername(@Param("username") String username);

    User selectById(@Param("id") Long id);

    List<User> selectAll();
}
