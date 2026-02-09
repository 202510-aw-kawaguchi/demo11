package com.example.demo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Report;
import com.example.demo.model.Statistics;
import com.example.demo.service.NotificationService;
import com.example.demo.service.ReportService;
import com.example.todo.entity.User;
import com.example.todo.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ReportService reportService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) throws Exception {
        Integer userId = getCurrentUserId(principal);

        CompletableFuture<Report> reportFuture = reportService.generateReportAsync(userId);
        CompletableFuture<Statistics> statsFuture = reportService.calculateStatisticsAsync(userId);

        CompletableFuture.allOf(reportFuture, statsFuture).join();

        model.addAttribute("report", reportFuture.get());
        model.addAttribute("stats", statsFuture.get());

        return "dashboard";
    }

    @PostMapping("/email")
    public String sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body,
            RedirectAttributes redirectAttributes) {
        notificationService.sendEmailAsync(to, subject, body);
        redirectAttributes.addFlashAttribute("message", "メール送信を開始しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/dashboard";
    }

    /**
     * タイムアウト付きで結果を待つ
     */
    @GetMapping("/quick")
    public String quickDashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        try {
            CompletableFuture<Report> future =
                    reportService.generateReportAsync(getCurrentUserId(principal));

            Report report = future.get(3, TimeUnit.SECONDS);
            model.addAttribute("report", report);
        } catch (TimeoutException ex) {
            model.addAttribute("error", "レポート生成がタイムアウトしました");
        } catch (Exception ex) {
            model.addAttribute("error", "エラーが発生しました");
        }

        return "dashboard";
    }

    private Integer getCurrentUserId(UserDetails principal) {
        if (principal == null) {
            return null;
        }
        User user = userMapper.findByUsername(principal.getUsername());
        return user == null ? null : user.getId().intValue();
    }
}
