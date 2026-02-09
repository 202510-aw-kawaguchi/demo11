package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.todo.entity.Todo;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final MailService mailService;

    public NotificationService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * 非同期でメール送信（テキスト）
     */
    @Async("emailExecutor")
    public void sendEmailAsync(String to, String subject, String body) {
        log.info("Send text mail async on {}", Thread.currentThread().getName());
        mailService.sendSimpleMail(to, subject, body);
    }

    /**
     * ToDo作成通知（テキスト）
     */
    @Async("emailExecutor")
    public void sendTodoCreatedTextMail(String to, Todo todo) {
        try {
            mailService.sendTodoCreatedNotification(to, todo);
        } catch (Exception ex) {
            log.warn("Failed to send text mail for todo {}: {}", todo.getId(), ex.getMessage());
        }
    }

    /**
     * HTMLメール送信（ToDo作成）
     */
    @Async("emailExecutor")
    public void sendTodoCreatedMail(String to, Todo todo) {
        try {
            mailService.sendTodoCreatedMail(to, todo);
        } catch (Exception ex) {
            log.warn("Failed to send HTML mail for todo {}: {}", todo.getId(), ex.getMessage());
        }
    }

    /**
     * デフォルトのtaskExecutorを使用
     */
    @Async
    public void processAsync() {
        log.info("processAsync started on {}", Thread.currentThread().getName());
    }

    @Async("taskExecutor")
    public CompletableFuture<String> generateTodoReport(String username) {
        String report = "Todo report for " + username + " generated at " + LocalDateTime.now();
        return CompletableFuture.completedFuture(report);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> generateTodoSummary(String username) {
        String summary = "Todo summary for " + username + " generated at " + LocalDateTime.now();
        return CompletableFuture.completedFuture(summary);
    }
}
