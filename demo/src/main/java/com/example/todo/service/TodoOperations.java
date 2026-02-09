package com.example.todo.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;

/**
 * Contract for ToDo operations.
 *
 * <p>See {@link TodoService} for the implementation.</p>
 *
 * @author Codex
 * @version 1.0
 * @since 1.0
 * @see TodoService
 */
public interface TodoOperations {

    /**
     * Create a new ToDo.
     *
     * @param title title
     * @param description description
     * @param priority priority (defaults to medium when {@code null})
     * @param dueDate due date
     * @param categoryId category ID
     * @param author author name
     * @param user executing user
     * @return created ToDo
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     * @throws com.example.todo.exception.BusinessException when a business rule fails
     */
    Todo create(String title, String description, Priority priority, LocalDate dueDate, Long categoryId, String author,
            User user);

    /**
     * Find ToDos with filters and sorting.
     *
     * @param user executing user
     * @param keyword keyword
     * @param categoryId category ID
     * @param sort sort key (e.g. {@code title})
     * @param dir sort direction ({@code asc}/{@code desc})
     * @param page page number (0-based)
     * @param size page size
     * @return ToDo list
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     */
    List<Todo> findByUserWithFilters(User user, String keyword, Long categoryId, String sort, String dir, int page,
            int size);

    /**
     * Find ToDos without paging.
     *
     * @param user executing user
     * @param keyword keyword
     * @param categoryId category ID
     * @param sort sort key
     * @param dir sort direction
     * @return ToDo list
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     */
    List<Todo> findByUserWithFiltersNoPaging(User user, String keyword, Long categoryId, String sort, String dir);

    /**
     * Count ToDos that match filters.
     *
     * @param user executing user
     * @param keyword keyword
     * @param categoryId category ID
     * @return count
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     */
    long countByUserWithFilters(User user, String keyword, Long categoryId);

    /**
     * Find a ToDo by ID.
     *
     * @param id ToDo ID
     * @param user executing user
     * @return ToDo
     * @throws com.example.todo.exception.TodoNotFoundException when not found
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     */
    Todo findById(Long id, User user);

    /**
     * Toggle completed status.
     *
     * @param id ToDo ID
     * @param user executing user
     * @return updated ToDo
     * @throws com.example.todo.exception.TodoNotFoundException when not found
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     * @throws com.example.todo.exception.BusinessException when a business rule fails
     */
    Todo toggleCompleted(Long id, User user);

    /**
     * Update a ToDo.
     *
     * @param id ToDo ID
     * @param title title
     * @param description description
     * @param priority priority
     * @param dueDate due date
     * @param categoryId category ID
     * @param user executing user
     * @return updated ToDo
     * @throws com.example.todo.exception.TodoNotFoundException when not found
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     * @throws com.example.todo.exception.BusinessException when a business rule fails
     */
    Todo update(Long id, String title, String description, Priority priority, LocalDate dueDate, Long categoryId,
            User user);

    /**
     * Delete a ToDo.
     *
     * @param id ToDo ID
     * @param user executing user
     * @throws com.example.todo.exception.TodoNotFoundException when not found
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     * @throws com.example.todo.exception.BusinessException when a business rule fails
     */
    void delete(Long id, User user);

    /**
     * Delete multiple ToDos.
     *
     * @param ids ToDo ID list
     * @param user executing user
     * @throws org.springframework.security.access.AccessDeniedException when access is denied
     * @throws com.example.todo.exception.BusinessException when a business rule fails
     */
    void deleteAllByIds(Collection<Long> ids, User user);
}
