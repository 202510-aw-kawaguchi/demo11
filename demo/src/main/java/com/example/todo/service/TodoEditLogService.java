package com.example.todo.service;

import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;
import com.example.todo.entity.TodoEditLog;
import com.example.todo.entity.User;
import com.example.todo.mapper.TodoEditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TodoEditLogService {

    private final TodoEditLogMapper todoEditLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordAdminEdit(Todo before,
                                String title,
                                String description,
                                Priority priority,
                                LocalDate dueDate,
                                Long categoryId,
                                User admin) {
        TodoEditLog log = new TodoEditLog();
        log.setTodoId(before.getId());
        log.setAdminUserId(admin.getId());
        log.setTargetUserId(before.getUser() != null ? before.getUser().getId() : null);
        log.setBeforeTitle(before.getTitle());
        log.setBeforeDescription(before.getDescription());
        log.setBeforePriority(before.getPriority() != null ? before.getPriority().name() : null);
        log.setBeforeDueDate(before.getDueDate());
        log.setBeforeCategoryId(before.getCategoryId());
        log.setAfterTitle(title);
        log.setAfterDescription(description);
        log.setAfterPriority((priority != null ? priority : Priority.MEDIUM).name());
        log.setAfterDueDate(dueDate);
        log.setAfterCategoryId(categoryId);
        todoEditLogMapper.insert(log);
    }
}
