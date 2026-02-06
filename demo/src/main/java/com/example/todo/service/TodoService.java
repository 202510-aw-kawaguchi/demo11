package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Category;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;

    public Todo create(String title, String description, Priority priority, LocalDate dueDate, Long categoryId, String author) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setAuthor(author);
        todo.setDescription(description);
        todo.setPriority(priority != null ? priority : Priority.MEDIUM);
        todo.setDueDate(dueDate);
        todo.setCategory(resolveCategory(categoryId));
        return todoRepository.save(todo);
    }

    public List<Todo> findAll() {
        return todoRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Todo> findAll(Sort sort) {
        return todoRepository.findAll(sort);
    }

    public Page<Todo> findAll(Pageable pageable) {
        return todoRepository.findAll(pageable);
    }

    public List<Todo> searchByTitle(String keyword) {
        return todoRepository.findByTitleContaining(keyword, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Todo> searchByTitle(String keyword, Sort sort) {
        return todoRepository.findByTitleContaining(keyword, sort);
    }

    public Page<Todo> searchByTitle(String keyword, Pageable pageable) {
        return todoRepository.findByTitleContaining(keyword, pageable);
    }

    public Page<Todo> findByCategory(Long categoryId, Pageable pageable) {
        return todoRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Todo> searchByTitleAndCategory(String keyword, Long categoryId, Pageable pageable) {
        return todoRepository.findByTitleContainingAndCategoryId(keyword, categoryId, pageable);
    }

    public List<Todo> findByCategory(Long categoryId, Sort sort) {
        return todoRepository.findByCategoryId(categoryId, sort);
    }

    public List<Todo> searchByTitleAndCategory(String keyword, Long categoryId, Sort sort) {
        return todoRepository.findByTitleContainingAndCategoryId(keyword, categoryId, sort);
    }

    public List<Todo> findByCompleted(boolean completed) {
        return todoRepository.findByCompleted(completed);
    }

    public Todo findById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    @Transactional
    public Todo toggleCompleted(Long id) {
        Todo todo = findById(id);
        todo.setCompleted(!Boolean.TRUE.equals(todo.getCompleted()));
        return todoRepository.save(todo);
    }

    @Transactional
    public Todo update(Long id, String title, String description, Priority priority, LocalDate dueDate, Long categoryId) {
        Todo todo = findById(id);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority != null ? priority : Priority.MEDIUM);
        todo.setDueDate(dueDate);
        todo.setCategory(resolveCategory(categoryId));
        return todoRepository.save(todo);
    }

    @Transactional
    public void delete(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new IllegalArgumentException("指定されたToDoが見つかりません: " + id);
        }
        todoRepository.deleteById(id);
    }

    @Transactional
    public void deleteAllByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        todoRepository.deleteByIdIn(ids);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId).orElse(null);
    }
}


