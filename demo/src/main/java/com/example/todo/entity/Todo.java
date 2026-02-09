package com.example.todo.entity;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ToDoエンティティ。
 *
 * <p>優先度は {@link Priority} を利用します。</p>
 *
 * @author Codex
 * @version 1.0
 * @since 1.0
 * @see Priority
 */
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

    /**
     * ユーザーIDを取得します。
     *
     * @return ユーザーID（未設定の場合は{@code null}）
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * カテゴリIDを取得します。
     *
     * @return カテゴリID（未設定の場合は{@code null}）
     */
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }

    /**
     * 詳細（{@link #getDetail()} の別名）を取得します。
     *
     * @return 詳細
     */
    public String getDetail() {
        return description;
    }

    /**
     * 詳細（{@link #setDetail(String)} の別名）を設定します。
     *
     * @param detail 詳細
     */
    public void setDetail(String detail) {
        this.description = detail;
    }
}
