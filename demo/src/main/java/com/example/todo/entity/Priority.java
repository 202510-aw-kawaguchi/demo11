package com.example.todo.entity;

public enum Priority {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
