package com.example.todo.repository;

import com.example.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 完了状態でフィルタリング
    List<Todo> findByCompleted(Boolean completed);

    // タイトルで部分一致検索
    List<Todo> findByTitleContaining(String keyword);
    List<Todo> findByTitleContaining(String keyword, Sort sort);
    Page<Todo> findByTitleContaining(String keyword, Pageable pageable);
    Page<Todo> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Todo> findByTitleContainingAndCategoryId(String keyword, Long categoryId, Pageable pageable);

    void deleteByIdIn(Collection<Long> ids);

    // 期限日が指定日以前のもの
    List<Todo> findByDueDateLessThanEqual(LocalDate date);

    // 優先度でソート（降順）
    List<Todo> findAllByOrderByPriorityDesc();

    // @Queryを使ったカスタムクエリ
    @Query("SELECT t FROM Todo t WHERE t.completed = false ORDER BY t.dueDate ASC")
    List<Todo> findUncompletedOrderByDueDate();

    // 作成日時の新しい順
    List<Todo> findAllByOrderByCreatedAtDesc();
}
