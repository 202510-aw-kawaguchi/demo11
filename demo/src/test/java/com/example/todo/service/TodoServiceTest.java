package com.example.todo.service;

import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.mapper.CategoryMapper;
import com.example.todo.mapper.TodoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoMapper todoMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TodoEditLogService todoEditLogService;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("create: insertが呼ばれる")
    void create_calls_insert() {
        User user = new User();
        user.setId(1L);
        todoService.create("t", "d", Priority.MEDIUM, LocalDate.now(), null, "author", user);
        verify(todoMapper, times(1)).insert(any(Todo.class));
    }

    @Test
    @DisplayName("findById: 対象ToDoを取得できる")
    void findById_returns_todo() {
        User user = new User();
        user.setId(1L);
        user.setRole("USER");
        Todo todo = new Todo();
        todo.setId(10L);
        User owner = new User();
        owner.setId(1L);
        todo.setUser(owner);
        when(todoMapper.findById(10L)).thenReturn(todo);

        Todo result = todoService.findById(10L, user);

        verify(todoMapper, times(1)).findById(10L);
        org.junit.jupiter.api.Assertions.assertEquals(10L, result.getId());
    }

    @Test
    @DisplayName("delete: deleteByIdAndUserが呼ばれる")
    void delete_calls_deleteByIdAndUser() {
        User user = new User();
        user.setId(1L);
        user.setRole("USER");
        Todo todo = new Todo();
        todo.setId(10L);
        User owner = new User();
        owner.setId(1L);
        todo.setUser(owner);
        when(todoMapper.findById(10L)).thenReturn(todo);

        todoService.delete(10L, user);

        verify(todoMapper, times(1)).deleteByIdAndUser(10L, 1L);
    }

    @Test
    @DisplayName("update: updateが呼ばれる")
    void update_calls_update_for_owner() {
        User user = new User();
        user.setId(1L);
        user.setRole("USER");
        Todo todo = new Todo();
        todo.setId(10L);
        User owner = new User();
        owner.setId(1L);
        todo.setUser(owner);
        when(todoMapper.findById(10L)).thenReturn(todo);

        todoService.update(10L, "t", "d", Priority.HIGH, null, null, user);

        verify(todoMapper, times(1)).update(any(Todo.class));
    }
}
