package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TodoController {

    // ToDo一覧画面を表示する
    @GetMapping("/todos")
    public String listTodos(Model model) {
        return "todo/list";
    }

    // ToDo新規作成画面を表示する
    @GetMapping("/todos/new")
    public String newTodo() {
        return "todo/form";
    }

    // 指定IDのToDo詳細画面を表示する
    @GetMapping("/todos/{id}")
    public String showTodo(@PathVariable Long id, Model model) {
        return "todo/show";
    }

    @PostMapping("/todos/confirm")
    public String confirmTodo(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "3") Integer priority,
            Model model) {
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("priority", priority);
        return "todo/confirm";
    }

    @PostMapping("/todos/complete")
    public String completeTodo(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "3") Integer priority,
            Model model) {
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("priority", priority);
        return "todo/complete";
    }
}
