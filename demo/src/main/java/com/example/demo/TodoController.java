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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
        todoService.create(
                todoForm.getTitle(),
                todoForm.getDetail(),
                todoForm.getPriority(),
                todoForm.getDueDate(),
                todoForm.getCategoryId(),
                todoForm.getAuthor()
        );
        return "todo/complete";
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
        todoService.create(
                todoForm.getTitle(),
                todoForm.getDetail(),
                todoForm.getPriority(),
                todoForm.getDueDate(),
                todoForm.getCategoryId(),
                todoForm.getAuthor()
        );
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @GetMapping("/todos/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir) {
        String sortKey = normalizeSortKey(sort);
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortSpec = Sort.by(direction, sortKey);

        List<Todo> todos;
        if (StringUtils.hasText(keyword) && categoryId != null) {
            todos = todoService.searchByTitleAndCategory(keyword.trim(), categoryId, sortSpec);
        } else if (StringUtils.hasText(keyword)) {
            todos = todoService.searchByTitle(keyword.trim(), sortSpec);
        } else if (categoryId != null) {
            todos = todoService.findByCategory(categoryId, sortSpec);
        } else {
            todos = todoService.findAll(sortSpec);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF");
        sb.append("ID,タイトル,登録者,カテゴリ,優先度,ステータス,作成日,期限日\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        for (Todo todo : todos) {
            String status = Boolean.TRUE.equals(todo.getCompleted()) ? "完了" : "未完了";
            LocalDateTime created = todo.getCreatedAt() != null ? todo.getCreatedAt() : todo.getUpdatedAt();
            String createdAt = created != null ? created.format(dtf) : "";
            String createdAtExcel = createdAt.isEmpty() ? "" : "=\"" + createdAt + "\"";
            String dueDate = todo.getDueDate() != null ? todo.getDueDate().format(df) : "";
            String dueDateExcel = dueDate.isEmpty() ? "" : "=\"" + dueDate + "\"";
            String category = todo.getCategory() != null ? todo.getCategory().getName() : "";
            String priority = todo.getPriority() != null ? todo.getPriority().getLabel() : "";
            sb.append(csv(todo.getId() != null ? todo.getId().toString() : ""))
              .append(",")
              .append(csv(todo.getTitle()))
              .append(",")
              .append(csv(todo.getAuthor()))
              .append(",")
              .append(csv(category))
              .append(",")
              .append(csv(priority))
              .append(",")
              .append(csv(status))
              .append(",")
              .append(csv(createdAtExcel))
              .append(",")
              .append(csv(dueDateExcel))
              .append("\n");
        }

        String filename = "todo_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        boolean needQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
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

    @PostMapping("/todos/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Long> ids, RedirectAttributes redirectAttributes) {
        todoService.deleteAllByIds(ids);
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "削除する項目が選択されていません");
            redirectAttributes.addFlashAttribute("messageType", "warning");
        } else {
            redirectAttributes.addFlashAttribute("message", "選択したToDoを削除しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
        }
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
        if ("dueDate".equalsIgnoreCase(sort) || "deadline".equalsIgnoreCase(sort)) {
            return "dueDate";
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



