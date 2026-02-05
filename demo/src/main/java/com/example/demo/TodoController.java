package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.todo.service.TodoService;

@Controller
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // ToDo一覧画面を表示する
    @GetMapping("/todos")
    public String listTodos(Model model) {
        return "todo/list";
    }

    // ToDo新規登録画面を表示する
    @GetMapping("/todos/new")
    public String newTodo(Model model) {
        return "todo/form";
    }

    // 詳細画面を表示する
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

    @PostMapping("/todos/create")
    public String create(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        todoService.create(title, description);

        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        return "redirect:/todos";
    }
}
