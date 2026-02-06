package com.example.demo;

import com.example.todo.entity.User;
import com.example.todo.mapper.UserMapper;
import com.example.todo.service.CategoryService;
import com.example.todo.service.TodoAttachmentService;
import com.example.todo.service.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private TodoAttachmentService todoAttachmentService;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void list_returns_ok() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setRole("USER");

        when(userMapper.findByUsername("user")).thenReturn(user);
        when(todoService.findByUserWithFilters(any(), anyString(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(todoService.countByUserWithFilters(any(), anyString(), any()))
                .thenReturn(0L);
        when(categoryService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(view().name("todo/list"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void new_returns_ok() throws Exception {
        mockMvc.perform(get("/todos/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("todo/form"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void create_redirects() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setRole("USER");
        when(userMapper.findByUsername("user")).thenReturn(user);

        MockHttpServletRequestBuilder req = post("/todos/create")
                .param("author", "user")
                .param("title", "t1")
                .param("detail", "d1");
        mockMvc.perform(req)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/todos"));
    }
}
