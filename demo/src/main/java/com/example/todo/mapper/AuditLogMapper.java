package com.example.todo.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.todo.entity.AuditLog;

@Mapper
public interface AuditLogMapper {
    int insert(AuditLog log);

    List<AuditLog> findWithFilters(
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("username") String username,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countWithFilters(
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("username") String username,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
