package com.example.todo.entity;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Todo {

    private Long id;

    private String title;

    private String author;

    private String description;

    private Category category;

    private User user;

    private LocalDate dueDate;

    private Priority priority = Priority.MEDIUM;

    private Boolean completed = false;

    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }

    public String getDetail() {
        return description;
    }

    public void setDetail(String detail) {
        this.description = detail;
    }
}
