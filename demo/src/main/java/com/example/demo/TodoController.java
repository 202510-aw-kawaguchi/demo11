package com.example.demo;

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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;
import com.example.todo.entity.Category;

import com.example.demo.form.TodoForm;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.service.CategoryService;
import com.example.todo.service.TodoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;
    private final CategoryService categoryService;

    @GetMapping("/todos")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir,
            Model model) {
        Page<Todo> todoPage;
        String sortKey = normalizeSortKey(sort);
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortSpec = Sort.by(direction, sortKey);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), 10, sortSpec);
        if (StringUtils.hasText(keyword) && categoryId != null) {
            String trimmed = keyword.trim();
            todoPage = todoService.searchByTitleAndCategory(trimmed, categoryId, pageable);
            model.addAttribute("keyword", trimmed);
        } else if (StringUtils.hasText(keyword)) {
            String trimmed = keyword.trim();
            todoPage = todoService.searchByTitle(trimmed, pageable);
            model.addAttribute("keyword", trimmed);
        } else if (categoryId != null) {
            todoPage = todoService.findByCategory(categoryId, pageable);
            model.addAttribute("keyword", "");
        } else {
            todoPage = todoService.findAll(pageable);
            model.addAttribute("keyword", "");
        }
        for (Todo todo : todoPage.getContent()) {
            if (todo.getPriority() == null) {
                todo.setPriority(Priority.MEDIUM);
            }
        }
        model.addAttribute("todos", todoPage.getContent());
        model.addAttribute("page", todoPage);
        model.addAttribute("resultCount", todoPage.getTotalElements());
        long total = todoPage.getTotalElements();
        int numberOfElements = todoPage.getNumberOfElements();
        long start = total == 0 ? 0 : (long) todoPage.getNumber() * todoPage.getSize() + 1;
        long end = total == 0 ? 0 : (long) todoPage.getNumber() * todoPage.getSize() + numberOfElements;
        model.addAttribute("resultStart", start);
        model.addAttribute("resultEnd", end);
        model.addAttribute("sort", sortKey);
        model.addAttribute("dir", direction.name().toLowerCase());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("categoryId", categoryId);
        return "todo/list";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/todos";
    }

    @GetMapping("/todos/new")
    public String newTodo(Model model) {
        model.addAttribute("todoForm", new TodoForm());
        model.addAttribute("categories", categoryService.findAll());
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
        model.addAttribute("categories", categoryService.findAll());
        return "todo/edit";
    }

    @PostMapping("/todos/confirm")
    public String confirmTodo(
            @Valid @ModelAttribute("todoForm") TodoForm todoForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "todo/form";
        }
        Category selectedCategory = categoryService.findById(todoForm.getCategoryId()).orElse(null);
        model.addAttribute("selectedCategory", selectedCategory);
        return "todo/confirm";
    }

    @PostMapping("/todos/complete")
    public String completeTodo(
            @Valid @ModelAttribute("todoForm") TodoForm todoForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "todo/form";
        }
        todoService.create(todoForm.getTitle(), todoForm.getDetail(), todoForm.getPriority(), todoForm.getDueDate(), todoForm.getCategoryId());
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @PostMapping("/todos/create")
    public String create(
            @Valid @ModelAttribute("todoForm") TodoForm todoForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "todo/form";
        }
        todoService.create(todoForm.getTitle(), todoForm.getDetail(), todoForm.getPriority(), todoForm.getDueDate(), todoForm.getCategoryId());
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/update")
    public String update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate dueDate,
            RedirectAttributes redirectAttributes) {

        todoService.update(id, title, description, priority, dueDate, categoryId);
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
        redirectAttributes.addFlashAttribute("error", "指定されたToDoが見つかりません");
        redirectAttributes.addFlashAttribute("messageType", "danger");
        return "redirect:/todos";
    }

    @ExceptionHandler(TodoNotFoundException.class)
    public String handleTodoNotFound(TodoNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "指定されたToDoが見つかりません");
        redirectAttributes.addFlashAttribute("messageType", "danger");
        return "redirect:/todos";
    }

    private String normalizeSortKey(String sort) {
        if ("title".equalsIgnoreCase(sort)) {
            return "title";
        }
        if ("priority".equalsIgnoreCase(sort)) {
            return "priority";
        }
        if ("completed".equalsIgnoreCase(sort) || "status".equalsIgnoreCase(sort)) {
            return "completed";
        }
        return "createdAt";
    }
}



