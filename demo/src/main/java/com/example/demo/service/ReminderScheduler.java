package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.todo.entity.Todo;
import com.example.todo.mapper.TodoMapper;

@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final TodoMapper todoMapper;
    private final MailService mailService;
    private final String mailDomain;
    private final String defaultMailTo;
    private final int reminderDays;

    public ReminderScheduler(TodoMapper todoMapper,
            MailService mailService,
            @Value("${app.mail.domain:example.com}") String mailDomain,
            @Value("${app.mail.to:}") String defaultMailTo,
            @Value("${app.reminder.days:3}") int reminderDays) {
        this.todoMapper = todoMapper;
        this.mailService = mailService;
        this.mailDomain = mailDomain;
        this.defaultMailTo = defaultMailTo;
        this.reminderDays = reminderDays;
    }

    /**
     * 毎日9時に期限リマインダー送信
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Tokyo")
    public void sendDailyReminders() {
        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(Math.max(reminderDays, 0));
        List<Todo> dueTodos = todoMapper.findDueBetween(today, until);
        if (dueTodos.isEmpty()) {
            log.info("No due todos within {} days", reminderDays);
            return;
        }

        Map<String, List<Todo>> byUser = dueTodos.stream()
                .filter(todo -> todo.getUser() != null && todo.getUser().getUsername() != null)
                .collect(Collectors.groupingBy(todo -> todo.getUser().getUsername()));

        byUser.forEach((username, todos) -> {
            String to = resolveRecipient(username);
            mailService.sendDailyReminderMail(to, todos);
        });
    }

    private String resolveRecipient(String username) {
        if (defaultMailTo != null && !defaultMailTo.isBlank()) {
            return defaultMailTo;
        }
        String safeUser = (username == null || username.isBlank()) ? "user" : username;
        return safeUser + "@" + mailDomain;
    }
}
