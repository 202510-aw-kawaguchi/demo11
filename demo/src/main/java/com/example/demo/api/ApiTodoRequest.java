package com.example.demo.api;

import com.example.todo.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ApiTodoRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String detail;

    private Long categoryId;
    private LocalDate dueDate;
    private Priority priority = Priority.MEDIUM;
}
