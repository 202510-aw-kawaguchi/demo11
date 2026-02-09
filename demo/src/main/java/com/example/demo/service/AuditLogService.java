package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.entity.AuditLog;
import com.example.todo.mapper.AuditLogMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditLog log) {
        auditLogMapper.insert(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findWithFilters(String action, String entityType, String username,
            LocalDateTime from, LocalDateTime to, int page, int size) {
        int offset = Math.max(page, 0) * size;
        return auditLogMapper.findWithFilters(action, entityType, username, from, to, size, offset);
    }

    @Transactional(readOnly = true)
    public long countWithFilters(String action, String entityType, String username,
            LocalDateTime from, LocalDateTime to) {
        return auditLogMapper.countWithFilters(action, entityType, username, from, to);
    }
}
