package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.todo.service.TodoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    // ToDo一覧画面を表示する
    @GetMapping("/todos")
    public String list(Model model) {
        List<?> todos = todoService.findAll();
        model.addAttribute("todos", todos);
        return "todo/list";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/todos";
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
            RedirectAttributes redirectAttributes) {
        todoService.create(title, description);
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
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

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "ToDoを削除しました");
        return "redirect:/todos";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleTodoNotFound(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "削除に失敗しました");
        return "redirect:/todos";
    }
}
