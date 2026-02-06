package com.example.todo.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long id;

    private String name;

    private String color;
}
