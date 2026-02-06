package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.entity.Priority;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    public Todo create(String title, String description, Priority priority, LocalDate dueDate) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority != null ? priority : Priority.MEDIUM);
        todo.setDueDate(dueDate);
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
    public Todo update(Long id, String title, String description, Priority priority, LocalDate dueDate) {
        Todo todo = findById(id);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority != null ? priority : Priority.MEDIUM);
        todo.setDueDate(dueDate);
        return todoRepository.save(todo);
    }

    @Transactional
    public void delete(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new IllegalArgumentException("指定されたToDoが見つかりません: " + id);
        }
        todoRepository.deleteById(id);
    }
}
