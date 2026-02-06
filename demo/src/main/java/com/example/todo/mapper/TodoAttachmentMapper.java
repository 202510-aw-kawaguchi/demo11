package com.example.todo.mapper;

import com.example.todo.entity.TodoAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TodoAttachmentMapper {
    int insert(TodoAttachment attachment);
    List<TodoAttachment> findByTodoId(@Param("todoId") Long todoId);
    TodoAttachment findById(@Param("id") Long id);
    TodoAttachment findByIdAndTodoId(@Param("id") Long id, @Param("todoId") Long todoId);
    int deleteById(@Param("id") Long id);
}
