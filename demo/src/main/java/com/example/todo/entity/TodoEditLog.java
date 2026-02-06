package com.example.todo.entity;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoEditLog {

    private Long id;

    private Long todoId;
    private Long adminUserId;
    private Long targetUserId;

    private String beforeTitle;
    private String afterTitle;
    private String beforeDescription;
    private String afterDescription;
    private String beforePriority;
    private String afterPriority;
    private LocalDate beforeDueDate;
    private LocalDate afterDueDate;
    private Long beforeCategoryId;
    private Long afterCategoryId;

    private LocalDateTime editedAt;
}
