package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.model.Report;
import com.example.demo.model.Statistics;
import com.example.todo.entity.Todo;
import com.example.todo.mapper.TodoMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TodoMapper todoMapper;

    /**
     * 非同期でレポートを生成し、結果を返す
     */
    @Async
    public CompletableFuture<Report> generateReportAsync(Integer userId) {
        List<Todo> todos = findTodos(userId);

        Report report = new Report();
        report.setTotalCount(todos.size());
        report.setCompletedCount((int) todos.stream().filter(t -> Boolean.TRUE.equals(t.getCompleted())).count());
        report.setGeneratedAt(LocalDateTime.now());

        return CompletableFuture.completedFuture(report);
    }

    /**
     * 複数の非同期処理を並列実行
     */
    @Async
    public CompletableFuture<Statistics> calculateStatisticsAsync(Integer userId) {
        List<Todo> todos = findTodos(userId);
        Statistics stats = calculateStats(todos);
        return CompletableFuture.completedFuture(stats);
    }

    private List<Todo> findTodos(Integer userId) {
        if (userId == null) {
            return List.of();
        }
        return todoMapper.findByUserId(userId.longValue());
    }

    private Statistics calculateStats(List<Todo> todos) {
        int total = todos.size();
        int completed = (int) todos.stream().filter(t -> Boolean.TRUE.equals(t.getCompleted())).count();

        Statistics stats = new Statistics();
        stats.setTotalCount(total);
        stats.setCompletedCount(completed);
        stats.setPendingCount(Math.max(total - completed, 0));
        return stats;
    }
}
