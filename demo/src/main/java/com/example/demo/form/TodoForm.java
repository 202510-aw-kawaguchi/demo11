package com.example.demo.form;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TodoForm {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer priority = 1;
}
