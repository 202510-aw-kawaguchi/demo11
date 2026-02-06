package com.example.todo.entity;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoAttachment {
    private Long id;
    private Long todoId;
    private String originalName;
    private String storedName;
    private String contentType;
    private Long size;
    private LocalDateTime createdAt;
}
