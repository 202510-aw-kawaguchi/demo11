package com.example.demo.form;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.example.todo.entity.Priority;

/**
 * ToDo入力フォーム。
 *
 * <p>期限日は {@code yyyy-MM-dd} 形式で扱います。</p>
 *
 * @author Codex
 * @version 1.0
 * @since 1.0
 * @see com.example.todo.entity.Todo
 */
@Data
public class TodoForm {
    private Long id;
    private Long version;

    @NotBlank(message = "{todo.author.required}")
    @Size(max = 50, message = "{todo.author.size}")
    private String author;

    @NotBlank(message = "{todo.title.required}")
    @Size(max = 100, message = "{todo.title.size}")
    private String title;

    @Size(max = 500, message = "{todo.detail.size}")
    private String detail;

    private Long categoryId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;
    private Priority priority = Priority.MEDIUM;
}
