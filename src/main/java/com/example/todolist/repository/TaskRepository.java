package com.example.todolist.repository;

import com.example.todolist.model.Task;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с задачами
 * Расширяет JpaRepository, предоставляя все CRUD операции
 *
 * @author anikanova a.a
 * @version 3.0
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(Priority priority);

    List<Task> findByStatusAndPriority(TaskStatus status, Priority priority);

    default List<Task> findByCompleted(boolean completed) {
        return findByStatus(completed ? TaskStatus.COMPLETED : TaskStatus.PENDING);
    }

    default List<Task> findByCompletedAndPriority(boolean completed, Priority priority) {
        TaskStatus status = completed ? TaskStatus.COMPLETED : TaskStatus.PENDING;
        return findByStatusAndPriority(status, priority);
    }

    List<Task> findByDueDateBeforeAndStatusNot(LocalDateTime date, TaskStatus status);

    List<Task> findByDueDateAfter(LocalDateTime date);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    long countByStatus(TaskStatus status);

    long countByPriority(Priority priority);

    long countByIdIn(List<Long> ids);

    boolean existsByTitle(String title);

    List<Task> findByTitleContainingIgnoreCase(String title);

    List<Task> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :start AND :end")
    List<Task> findTasksDueWithinNextSevenDays(@Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = :status WHERE t.id IN :ids")
    int updateStatusByIds(@Param("status") TaskStatus status, @Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.status = 'COMPLETED' AND t.updatedAt < :date")
    int deleteCompletedTasksOlderThan(@Param("date") LocalDateTime date);

    @EntityGraph(attributePaths = {"attachments"})
    @Query("SELECT t FROM Task t")
    List<Task> findAllWithAttachments();

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.attachments WHERE t.id = :id")
    Optional<Task> findByIdWithAttachments(@Param("id") Long id);

}