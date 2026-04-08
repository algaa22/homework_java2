package com.example.todolist.repository;

import com.example.todolist.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findByTaskId(Long taskId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskAttachment a WHERE a.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);

    Optional<TaskAttachment> findByStoredFileName(String storedFileName);

    boolean existsByStoredFileName(String storedFileName);

    long countByTaskId(Long taskId);

    List<TaskAttachment> findByContentType(String contentType);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskAttachment a WHERE a.uploadedAt < :date")
    int deleteAttachmentsOlderThan(@Param("date") LocalDateTime date);

    @Query("SELECT COALESCE(SUM(a.size), 0) FROM TaskAttachment a WHERE a.task.id = :taskId")
    long getTotalSizeByTaskId(@Param("taskId") Long taskId);
}