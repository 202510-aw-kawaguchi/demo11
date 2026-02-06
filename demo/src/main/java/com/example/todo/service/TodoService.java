package com.example.todo.service;

import com.example.todo.entity.Category;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.mapper.CategoryMapper;
import com.example.todo.mapper.TodoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoMapper todoMapper;
    private final CategoryMapper categoryMapper;

    public Todo create(String title, String description, Priority priority, LocalDate dueDate, Long categoryId, String author, User user) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setAuthor(author);
        todo.setDescription(description);
        todo.setPriority(priority != null ? priority : Priority.MEDIUM);
        todo.setDueDate(dueDate);
        todo.setCategory(resolveCategory(categoryId));
        todo.setUser(user);
        todo.setCompleted(false);
        todoMapper.insert(todo);
        return todo;
    }

    public List<Todo> findByUserWithFilters(User user, String keyword, Long categoryId, String sort, String dir, int page, int size) {
        int offset = Math.max(page, 0) * size;
        return todoMapper.findByUserWithFilters(user.getId(), keyword, categoryId, sort, dir, size, offset);
    }

    public List<Todo> findByUserWithFiltersNoPaging(User user, String keyword, Long categoryId, String sort, String dir) {
        return todoMapper.findByUserWithFiltersNoPaging(user.getId(), keyword, categoryId, sort, dir);
    }

    public long countByUserWithFilters(User user, String keyword, Long categoryId) {
        return todoMapper.countByUserWithFilters(user.getId(), keyword, categoryId);
    }

    public Todo findById(Long id, User user) {
        return getOwnedTodo(id, user);
    }

    @Transactional
    public Todo toggleCompleted(Long id, User user) {
        Todo todo = getOwnedTodo(id, user);
        todoMapper.toggleCompleted(id, user.getId());
        todo.setCompleted(!Boolean.TRUE.equals(todo.getCompleted()));
        return todo;
    }

    @Transactional
    public Todo update(Long id, String title, String description, Priority priority, LocalDate dueDate, Long categoryId, User user) {
        Todo todo = getOwnedTodo(id, user);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority != null ? priority : Priority.MEDIUM);
        todo.setDueDate(dueDate);
        todo.setCategory(resolveCategory(categoryId));
        todoMapper.update(todo);
        return todo;
    }

    @Transactional
    public void delete(Long id, User user) {
        Todo todo = getOwnedTodo(id, user);
        todoMapper.deleteByIdAndUser(todo.getId(), user.getId());
    }

    @Transactional
    public void deleteAllByIds(Collection<Long> ids, User user) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        todoMapper.deleteByIdsAndUser(List.copyOf(ids), user.getId());
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryMapper.findById(categoryId);
    }

    private Todo getOwnedTodo(Long id, User user) {
        Todo todo = todoMapper.findById(id);
        if (todo == null) {
            throw new TodoNotFoundException(id);
        }
        if (todo.getUser() == null || !todo.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return todo;
    }
}
