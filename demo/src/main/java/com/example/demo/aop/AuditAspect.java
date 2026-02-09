package com.example.demo.aop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.demo.service.AuditLogService;
import com.example.todo.entity.AuditLog;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.mapper.TodoMapper;
import com.example.todo.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final TodoMapper todoMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long entityIdBefore = resolveEntityId(null, args);
        Object oldValue = loadOldValue(auditable.entityType(), entityIdBefore);

        Object result = joinPoint.proceed();

        Long entityIdAfter = resolveEntityId(result, args);
        Object newValue = sanitizeObject(result);

        AuditLog log = new AuditLog();
        log.setAction(auditable.action());
        log.setEntityType(auditable.entityType());
        log.setEntityId(entityIdAfter != null ? entityIdAfter : entityIdBefore);
        log.setUserId(resolveUserId());
        log.setOldValue(toJson(oldValue != null ? oldValue : sanitizeArgs(args)));
        log.setNewValue(toJson(newValue));
        log.setIpAddress(resolveClientIp());
        log.setCreatedAt(LocalDateTime.now());

        auditLogService.record(log);
        return result;
    }

    private Object loadOldValue(String entityType, Long entityId) {
        if ("TODO".equalsIgnoreCase(entityType) && entityId != null) {
            Todo todo = todoMapper.findById(entityId);
            return sanitizeObject(todo);
        }
        return null;
    }

    private Long resolveEntityId(Object result, Object[] args) {
        if (result instanceof Todo todo) {
            return todo.getId();
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Long id) {
                    return id;
                }
                if (arg instanceof Todo todo) {
                    return todo.getId();
                }
            }
        }
        return null;
    }

    private Long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        User user = userMapper.findByUsername(auth.getName());
        return user != null ? user.getId() : null;
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Object sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> sanitized = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            String name = arg.getClass().getName();
            if (name.startsWith("org.springframework.ui.")
                    || name.startsWith("org.springframework.validation.")
                    || name.startsWith("org.springframework.web.servlet.mvc.support.")
                    || name.startsWith("jakarta.servlet.")) {
                continue;
            }
            sanitized.add(sanitizeObject(arg));
        }
        return sanitized;
    }

    private Object sanitizeObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Todo todo) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", todo.getId());
            map.put("title", todo.getTitle());
            map.put("description", todo.getDescription());
            map.put("author", todo.getAuthor());
            map.put("dueDate", todo.getDueDate());
            map.put("priority", todo.getPriority());
            map.put("completed", todo.getCompleted());
            map.put("categoryId", todo.getCategoryId());
            map.put("userId", todo.getUserId());
            return map;
        }
        if (value instanceof User user) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("role", user.getRole());
            map.put("enabled", user.getEnabled());
            return map;
        }
        if (value instanceof Collection<?> collection) {
            List<Object> list = new ArrayList<>();
            for (Object item : collection) {
                list.add(sanitizeObject(item));
            }
            return list;
        }
        return value;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"failed_to_serialize\"}";
        }
    }
}
