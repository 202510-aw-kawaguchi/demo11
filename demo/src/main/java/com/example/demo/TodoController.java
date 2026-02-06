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
import org.springframework.util.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.example.todo.entity.Priority;
import com.example.todo.entity.Todo;
import com.example.todo.entity.Category;
import com.example.todo.entity.User;

import com.example.demo.form.TodoForm;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.service.CategoryService;
import com.example.todo.service.TodoService;
import com.example.todo.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @GetMapping("/todos")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "desc") String dir,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {
        User user = requireUser(principal);
        int pageSize = 10;
        String sortKey = normalizeSortKey(sort);
        String dirValue = "asc".equalsIgnoreCase(dir) ? "asc" : "desc";
        String keywordValue = StringUtils.hasText(keyword) ? keyword.trim() : "";
        List<Todo> todos = todoService.findByUserWithFilters(user, keywordValue, categoryId, sortKey, dirValue, page, pageSize);
        long total = todoService.countByUserWithFilters(user, keywordValue, categoryId);

        for (Todo todo : todos) {
            if (todo.getPriority() == null) {
                todo.setPriority(Priority.MEDIUM);
            }
        }
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        int safePage = Math.max(page, 0);
        long start = total == 0 ? 0 : (long) safePage * pageSize + 1;
        long end = total == 0 ? 0 : Math.min(total, (long) safePage * pageSize + todos.size());
        PageInfo pageInfo = new PageInfo(safePage, totalPages);

        model.addAttribute("todos", todos);
        model.addAttribute("page", pageInfo);
        model.addAttribute("resultCount", total);
        model.addAttribute("resultStart", start);
        model.addAttribute("resultEnd", end);
        model.addAttribute("sort", sortKey);
        model.addAttribute("dir", dirValue);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("keyword", keywordValue);
        model.addAttribute("username", principal.getUsername());
        return "todo/list";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/todos";
    }

    @GetMapping("/todos/new")
    public String newTodo(@AuthenticationPrincipal UserDetails principal, Model model) {
        TodoForm form = new TodoForm();
        if (principal != null) {
            form.setAuthor(principal.getUsername());
        }
        model.addAttribute("todoForm", form);
        model.addAttribute("categories", categoryService.findAll());
        return "todo/form";
    }

    @GetMapping("/todos/{id}")
    public String showTodo(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model) {
        var todo = todoService.findById(id, requireUser(principal));
        model.addAttribute("todo", todo);
        return "todo/show";
    }

    @GetMapping("/todos/{id}/edit")
    public String editTodo(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model) {
        var todo = todoService.findById(id, requireUser(principal));
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
        Category selectedCategory = todoForm.getCategoryId() != null ? categoryService.findById(todoForm.getCategoryId()) : null;
        model.addAttribute("selectedCategory", selectedCategory);
        return "todo/confirm";
    }

    @PostMapping("/todos/complete")
    public String completeTodo(
            @Valid @ModelAttribute("todoForm") TodoForm todoForm,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal UserDetails principal,
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
                principal != null ? principal.getUsername() : todoForm.getAuthor(),
                requireUser(principal)
        );
        return "todo/complete";
    }

    @PostMapping("/todos/create")
    public String create(
            @Valid @ModelAttribute("todoForm") TodoForm todoForm,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal UserDetails principal,
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
                principal != null ? principal.getUsername() : todoForm.getAuthor(),
                requireUser(principal)
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
            @RequestParam(required = false, defaultValue = "desc") String dir,
            @AuthenticationPrincipal UserDetails principal) {
        String sortKey = normalizeSortKey(sort);
        String dirValue = "asc".equalsIgnoreCase(dir) ? "asc" : "desc";
        User user = requireUser(principal);

        String keywordValue = StringUtils.hasText(keyword) ? keyword.trim() : "";
        List<Todo> todos = todoService.findByUserWithFiltersNoPaging(user, keywordValue, categoryId, sortKey, dirValue);

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
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes) {

        todoService.update(id, title, description, priority, dueDate, categoryId, requireUser(principal));
        redirectAttributes.addFlashAttribute("message", "更新が完了しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, RedirectAttributes redirectAttributes) {
        todoService.delete(id, requireUser(principal));
        redirectAttributes.addFlashAttribute("message", "ToDoを削除しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    @PostMapping("/todos/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Long> ids, @AuthenticationPrincipal UserDetails principal, RedirectAttributes redirectAttributes) {
        todoService.deleteAllByIds(ids, requireUser(principal));
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
    public Object toggle(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        var updated = todoService.toggleCompleted(id, requireUser(principal));
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

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex) {
        return "error/403";
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

    private User requireUser(UserDetails principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        User user = userMapper.findByUsername(principal.getUsername());
        if (user == null) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }
        return user;
    }

    private static class PageInfo {
        private final int number;
        private final int totalPages;

        PageInfo(int number, int totalPages) {
            this.number = number;
            this.totalPages = totalPages;
        }

        public int getNumber() {
            return number;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean isFirst() {
            return number <= 0;
        }

        public boolean isLast() {
            return number >= totalPages - 1;
        }
    }
}



