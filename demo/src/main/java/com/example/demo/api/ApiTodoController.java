package com.example.demo.api;

import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.mapper.UserMapper;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiTodoController {

    private final TodoService todoService;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApiTodoResponse>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir,
            @AuthenticationPrincipal UserDetails principal) {
        User user = requireUser(principal);
        String keywordValue = StringUtils.hasText(keyword) ? keyword.trim() : "";
        List<Todo> todos = todoService.findByUserWithFilters(user, keywordValue, categoryId, normalizeSortKey(sort), dir, page, size);
        List<ApiTodoResponse> response = new ArrayList<>();
        for (Todo todo : todos) {
            response.add(toResponse(todo));
        }
        return ResponseEntity.ok(ApiResponse.ok("OK", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiTodoResponse>> get(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        User user = requireUser(principal);
        Todo todo = todoService.findById(id, user);
        return ResponseEntity.ok(ApiResponse.ok("OK", toResponse(todo)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApiTodoResponse>> create(
            @Valid @RequestBody ApiTodoRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        User user = requireUser(principal);
        Todo created = todoService.create(
                request.getTitle(),
                request.getDetail(),
                request.getPriority(),
                request.getDueDate(),
                request.getCategoryId(),
                principal.getUsername(),
                user
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/api/todos/" + created.getId()));
        return new ResponseEntity<>(ApiResponse.ok("Created", toResponse(created)), headers, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiTodoResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ApiTodoRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        User user = requireUser(principal);
        Todo updated = todoService.update(
                id,
                request.getTitle(),
                request.getDetail(),
                request.getPriority(),
                request.getDueDate(),
                request.getCategoryId(),
                user
        );
        return ResponseEntity.ok(ApiResponse.ok("Updated", toResponse(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        User user = requireUser(principal);
        todoService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    private User requireUser(UserDetails principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        User user = userMapper.findByUsername(principal.getUsername());
        if (user == null) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return user;
    }

    private String normalizeSortKey(String sort) {
        if ("title".equalsIgnoreCase(sort)) {
            return "title";
        }
        if ("dueDate".equalsIgnoreCase(sort) || "deadline".equalsIgnoreCase(sort)) {
            return "dueDate";
        }
        if ("priority".equalsIgnoreCase(sort)) {
            return "priority";
        }
        if ("completed".equalsIgnoreCase(sort) || "status".equalsIgnoreCase(sort)) {
            return "completed";
        }
        return "createdAt";
    }

    private ApiTodoResponse toResponse(Todo todo) {
        ApiTodoResponse.CategoryInfo category = null;
        if (todo.getCategory() != null) {
            category = new ApiTodoResponse.CategoryInfo(
                    todo.getCategory().getId(),
                    todo.getCategory().getName(),
                    todo.getCategory().getColor()
            );
        }
        ApiTodoResponse.UserInfo user = null;
        if (todo.getUser() != null) {
            user = new ApiTodoResponse.UserInfo(
                    todo.getUser().getId(),
                    todo.getUser().getUsername(),
                    todo.getUser().getRole()
            );
        }
        String priority = todo.getPriority() != null ? todo.getPriority().name() : null;
        return new ApiTodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getAuthor(),
                todo.getDescription(),
                priority,
                todo.getCompleted(),
                todo.getDueDate(),
                todo.getCreatedAt(),
                todo.getUpdatedAt(),
                category,
                user
        );
    }
}
