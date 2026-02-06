package com.example.demo;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.example.demo.form.TodoForm;
import com.example.todo.exception.TodoNotFoundException;
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
        model.addAttribute("todoForm", new TodoForm());
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
            @Valid @ModelAttribute("todoForm") TodoForm todoForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "todo/form";
        }
        return "todo/confirm";
    }

    @PostMapping("/todos/complete")
    public String completeTodo(
            @Valid @ModelAttribute("todoForm") TodoForm todoForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "todo/form";
        }
        todoService.create(todoForm.getTitle(), todoForm.getDetail(), todoForm.getPriority(), todoForm.getDueDate());
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @PostMapping("/todos/create")
    public String create(
            @Valid @ModelAttribute("todoForm") TodoForm todoForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "todo/form";
        }
        todoService.create(todoForm.getTitle(), todoForm.getDetail(), todoForm.getPriority(), todoForm.getDueDate());
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/update")
    public String update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate dueDate,
            RedirectAttributes redirectAttributes) {

        todoService.update(id, title, description, dueDate);
        redirectAttributes.addFlashAttribute("message", "譖ｴ譁ｰ縺悟ｮ御ｺ・＠縺ｾ縺励◆");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "ToDo繧貞炎髯､縺励∪縺励◆");
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
        redirectAttributes.addFlashAttribute("message", "螳御ｺ・憾諷九ｒ譖ｴ譁ｰ縺励∪縺励◆");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleTodoNotFound(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "蜑企勁縺ｫ螟ｱ謨励＠縺ｾ縺励◆");
        redirectAttributes.addFlashAttribute("messageType", "danger");
        return "redirect:/todos";
    }

    @ExceptionHandler(TodoNotFoundException.class)
    public String handleTodoNotFound(TodoNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "指定されたToDoが見つかりません");
        redirectAttributes.addFlashAttribute("messageType", "danger");
        return "redirect:/todos";
    }
}


