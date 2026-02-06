package com.example.todo.mapper;

import com.example.todo.entity.TodoEditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoEditLogMapper {
    int insert(TodoEditLog log);
}
