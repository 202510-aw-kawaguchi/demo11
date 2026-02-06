package com.example.demo.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TodoForm {
    private Long id;
    private Long version;

    @NotBlank
    @Size(max = 50)
    private String author;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String detail;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;
    private Integer priority = 1;
}
