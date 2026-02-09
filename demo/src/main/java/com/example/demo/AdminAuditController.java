package com.example.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.AuditLogService;
import com.example.todo.entity.AuditLog;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model) {

        int pageSize = 20;
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : null;

        List<AuditLog> logs = auditLogService.findWithFilters(action, entityType, username, fromDt, toDt, page, pageSize);
        long total = auditLogService.countWithFilters(action, entityType, username, fromDt, toDt);

        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / pageSize);
        PageInfo pageInfo = new PageInfo(Math.max(page, 0), totalPages);

        model.addAttribute("logs", logs);
        model.addAttribute("page", pageInfo);
        model.addAttribute("total", total);
        model.addAttribute("action", action);
        model.addAttribute("entityType", entityType);
        model.addAttribute("username", username);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/audit-logs";
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
