package com.example.demo;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import com.example.todo.service.TodoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

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

    @GetMapping("/todos/new")
    public String newTodo(Model model) {
        model.addAttribute("todoForm", new com.example.demo.form.TodoForm());
        return "todo/form";
    }

    @GetMapping("/todos/{id}")
    public String showTodo(@PathVariable Long id, Model model) {
        var todo = todoService.findById(id);
        model.addAttribute("todo", todo);
        return "todo/show";
    }

    @GetMapping("/todos/{id}/edit")
    public String editTodo(@PathVariable Long id, Model model) {
        var todo = todoService.findById(id);
        model.addAttribute("todo", todo);
        return "todo/edit";
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
        todoService.create(title, description, priority);
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @PostMapping("/todos/create")
    public String create(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "3") Integer priority,
            RedirectAttributes redirectAttributes) {
        todoService.create(title, description, priority);
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/update")
    public String update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        todoService.update(id, title, description);
        redirectAttributes.addFlashAttribute("message", "更新が完了しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "ToDoを削除しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/toggle")
    public Object toggle(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        var updated = todoService.toggleCompleted(id);
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return ResponseEntity.ok().body(java.util.Map.of(
                    "id", updated.getId(),
                    "completed", updated.getCompleted()
            ));
        }
        redirectAttributes.addFlashAttribute("message", "完了状態を更新しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleTodoNotFound(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "削除に失敗しました");
        redirectAttributes.addFlashAttribute("messageType", "danger");
        return "redirect:/todos";
    }
}
