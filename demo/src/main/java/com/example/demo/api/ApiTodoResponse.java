package com.example.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiTodoResponse {
    private Long id;
    private String title;
    private String author;
    private String detail;
    private String priority;
    private Boolean completed;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoryInfo category;
    private UserInfo user;

    @Data
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String color;
    }

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String role;
    }
}
