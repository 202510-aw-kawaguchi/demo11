package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.todo.entity.Todo;
import com.example.todo.mapper.TodoMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final EmailSender emailSender;
    private final TodoMapper todoMapper;

    /**
     * 非同期でメール送信（戻り値なし）
     * 呼び出し元はすぐに制御を返す
     */
    @Async("emailExecutor")
    public void sendEmailAsync(String to, String subject, String body) {
        log.info("メール送信開始: {}", Thread.currentThread().getName());
        try {
            Thread.sleep(5000);
            emailSender.send(to, subject, body);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        log.info("メール送信完了");
    }

    /**
     * 非同期でレポート生成
     */
    @Async
    public void generateReportAsync(Integer userId) {
        log.info("レポート生成開始");
        Long id = userId == null ? null : userId.longValue();
        List<Todo> todos = id == null ? List.of() : todoMapper.findByUserId(id);
        log.info("対象ToDo数: {}", todos.size());
        log.info("レポート生成完了");
    }

    /**
     * デフォルトのtaskExecutorを使用
     */
    @Async
    public void processAsync() {
        log.info("processAsync started on {}", Thread.currentThread().getName());
    }

    @Async("emailExecutor")
    public void sendTodoCreatedNotification(String username, String title) {
        simulateSlowWork(600);
        log.info("Notify {} about new todo: {}", username, title);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> generateTodoReport(String username) {
        simulateSlowWork(1200);
        String report = "Todo report for " + username + " generated at " + LocalDateTime.now();
        return CompletableFuture.completedFuture(report);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> generateTodoSummary(String username) {
        simulateSlowWork(900);
        String summary = "Todo summary for " + username + " generated at " + LocalDateTime.now();
        return CompletableFuture.completedFuture(summary);
    }

    @Async("emailExecutor")
    public void sendFailingNotification(String username) {
        throw new IllegalStateException("Simulated async notification failure for " + username);
    }

    private void simulateSlowWork(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
