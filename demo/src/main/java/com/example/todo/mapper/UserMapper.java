package com.example.todo.mapper;

import com.example.todo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findById(@Param("id") Long id);
    java.util.List<User> findAll();
    int insert(User user);
    int update(User user);
}
