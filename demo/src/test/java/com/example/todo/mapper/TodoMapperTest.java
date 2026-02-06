package com.example.todo.mapper;

import com.example.todo.entity.Todo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TodoMapperTest {

    @Autowired
    private TodoMapper todoMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findByUserWithFilters_returns_rows() {
        jdbcTemplate.update("INSERT INTO users (username, password, role, enabled) VALUES ('u1','p','USER', true)");
        jdbcTemplate.update("INSERT INTO categories (name, color) VALUES ('仕事', '#dc3545')");
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE username='u1'", Long.class);
        Long categoryId = jdbcTemplate.queryForObject("SELECT id FROM categories WHERE name='仕事'", Long.class);
        jdbcTemplate.update(
                "INSERT INTO todos (title, description, author, user_id, category_id, priority, completed, created_at, updated_at) " +
                        "VALUES ('t1','d1','u1', ?, ?, 'MEDIUM', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                userId, categoryId
        );

        List<Todo> results = todoMapper.findByUserWithFilters(userId, "", null, "createdAt", "desc", 10, 0);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).isEqualTo("t1");
    }

    @Test
    void insert_update_delete_crud() {
        jdbcTemplate.update("INSERT INTO users (username, password, role, enabled) VALUES ('u2','p','USER', true)");
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE username='u2'", Long.class);
        Todo todo = new Todo();
        todo.setTitle("t2");
        todo.setDescription("d2");
        todo.setAuthor("u2");
        todo.setPriority(com.example.todo.entity.Priority.MEDIUM);
        com.example.todo.entity.User user = new com.example.todo.entity.User();
        user.setId(userId);
        todo.setUser(user);

        todoMapper.insert(todo);
        assertThat(todo.getId()).isNotNull();

        todo.setTitle("t2-updated");
        todoMapper.update(todo);
        Todo found = todoMapper.findById(todo.getId());
        assertThat(found.getTitle()).isEqualTo("t2-updated");

        todoMapper.deleteById(todo.getId());
        Todo deleted = todoMapper.findById(todo.getId());
        assertThat(deleted).isNull();
    }
}
