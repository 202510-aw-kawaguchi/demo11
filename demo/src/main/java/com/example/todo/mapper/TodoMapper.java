package com.example.todo.mapper;

import com.example.todo.entity.Todo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TodoMapper {
    List<Todo> findByUserWithFilters(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("sort") String sort,
            @Param("dir") String dir,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    List<Todo> findByUserWithFiltersNoPaging(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("sort") String sort,
            @Param("dir") String dir
    );

    long countByUserWithFilters(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId
    );

    Todo findByIdAndUser(
            @Param("id") Long id,
            @Param("userId") Long userId
    );
    Todo findById(@Param("id") Long id);

    List<Todo> findByUserId(@Param("userId") Long userId);
    List<Todo> findByUsername(@Param("username") String username);

    int insert(Todo todo);
    int update(Todo todo);
    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
    int deleteByIdsAndUser(@Param("ids") List<Long> ids, @Param("userId") Long userId);
    int toggleCompleted(@Param("id") Long id, @Param("userId") Long userId);
}
