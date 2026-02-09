package com.example.demo;

import com.example.todo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userMapper.findAll());
        return "admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        var user = userMapper.findById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/user-edit";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id,
            String role,
            Boolean enabled,
            RedirectAttributes redirectAttributes) {
        var user = userMapper.findById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("message", "指定されたユーザーが見つかりません");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/admin/users";
        }
        user.setRole(role != null ? role : user.getRole());
        user.setEnabled(Boolean.TRUE.equals(enabled));
        userMapper.update(user);
        redirectAttributes.addFlashAttribute("message", "ユーザー情報を更新しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin/users";
    }
}
