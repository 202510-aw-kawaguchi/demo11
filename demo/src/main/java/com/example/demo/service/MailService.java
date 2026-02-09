package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.example.todo.entity.Todo;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String fromAddress;

    public MailService(JavaMailSender mailSender,
            SpringTemplateEngine templateEngine,
            @Value("${app.mail.from:noreply@example.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
    }

    /** テキストメール送信（非同期） */
    @Async("emailExecutor")
    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /** HTMLメール送信（テキスト代替を含むマルチパート） */
    @Async("emailExecutor")
    public void sendHtmlMail(String to, String subject, String htmlContent, String textFallback) {
        MimeMessage mime = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textFallback, htmlContent);
            mailSender.send(mime);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Failed to send HTML mail", ex);
        }
    }

    /** ThymeleafテンプレートでHTML本文を生成して送信 */
    @Async("emailExecutor")
    public void sendTemplatedMail(String to, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process(templateName, context);
        String text = variables.getOrDefault("textFallback", "このメールはHTML対応が必要です。").toString();
        sendHtmlMail(to, subject, html, text);
    }

    /** ToDo作成通知（テキスト） */
    public void sendTodoCreatedNotification(String to, Todo todo) {
        String subject = "【ToDo作成】" + todo.getTitle();
        String text = """
                ToDoを作成しました。
                タイトル: %s
                詳細: %s
                期限: %s

                ToDoアプリで確認してください。
                """.formatted(
                todo.getTitle(),
                nullToDash(todo.getDescription()),
                todo.getDueDate() != null ? todo.getDueDate().toString() : "未設定");
        sendSimpleMail(to, subject, text);
    }

    /** ToDo作成通知（HTML） */
    public void sendTodoCreatedMail(String to, Todo todo) {
        Map<String, Object> vars = Map.of(
                "todo", todo,
                "textFallback", "ToDoを作成しました: " + todo.getTitle());
        sendTemplatedMail(to, "【ToDo作成】" + todo.getTitle(), "mail/todo-created", vars);
    }

    /** 期限リマインダー（単体） */
    public void sendDeadlineReminder(String to, Todo todo) {
        Map<String, Object> vars = Map.of(
                "todo", todo,
                "textFallback", "期限が近いToDoがあります: " + todo.getTitle());
        sendTemplatedMail(to, "【期限リマインダー】" + todo.getTitle(), "mail/reminder-single", vars);
    }

    /** 毎日期限ToDoリマインダー（HTML） */
    public void sendDailyReminderMail(String to, List<Todo> todos) {
        Map<String, Object> vars = Map.of(
                "todos", todos,
                "textFallback", "毎日期限ToDoは" + todos.size() + "件です。");
        sendTemplatedMail(to, "毎日期限ToDoリマインダー", "mail/reminder", vars);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "未設定" : value;
    }

    /** テキストメール送信（別名） */
    @Async("emailExecutor")
    public void sendMailAsync(String to, String subject, String text) {
        sendSimpleMail(to, subject, text);
    }
}
