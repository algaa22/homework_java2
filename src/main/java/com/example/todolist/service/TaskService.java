package com.example.todolist.service;

import com.example.todolist.exception.BulkOperationException;
import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.model.Task;
import com.example.todolist.repository.TaskRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервисный слой для управления задачами.
 * Содержит бизнес-логику приложения.
 *
 * @author anikanova a.a
 * @version 3.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Task getTaskWithAttachments(Long id) {
        log.debug("Getting task with attachments, id: {}", id);
        return taskRepository.findByIdWithAttachments(id)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasksWithAttachments() {
        log.debug("Getting all tasks with attachments");
        return taskRepository.findAllWithAttachments();
    }

    @Transactional
    public Task createTask(Task task) {
        if (task.getDueDate() != null && task.getDueDate().toLocalDate()
            .isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long id, Task task) {
        Task existing = getTaskById(id);

        if (task.getTitle() != null) {
            existing.setTitle(task.getTitle());
        }
        if (task.getDescription() != null) {
            existing.setDescription(task.getDescription());
        }
        if (task.isCompleted() != existing.isCompleted()) {
            existing.setCompleted(task.isCompleted());
        }
        if (task.getDueDate() != null) {
            if (existing.getCreatedAt() != null &&
                task.getDueDate().toLocalDate().isBefore(existing.getCreatedAt().toLocalDate())) {
                throw new IllegalArgumentException("Due date cannot be before creation date");
            }
            existing.setDueDate(task.getDueDate());
        }
        if (task.getPriority() != null) {
            existing.setPriority(task.getPriority());
        }
        if (task.getTags() != null && !task.getTags().isEmpty()) {
            existing.setTags(task.getTags());
        }

        return taskRepository.save(existing);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return taskRepository.count();
    }

    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = {BulkOperationException.class, TaskNotFoundException.class, Exception.class},
        noRollbackFor = {IllegalArgumentException.class},
        timeout = 30
    )
    public void bulkCompleteTasks(List<Long> ids) {
        log.info("Starting bulk complete for {} tasks", ids.size());

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Task IDs list cannot be null or empty");
        }

        List<Task> tasks = taskRepository.findAllById(ids);

        if (tasks.size() != ids.size()) {
            List<Long> foundIds = tasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());

            List<Long> missingIds = ids.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

            log.error("Tasks not found: {}. Rolling back transaction!", missingIds);

            throw new BulkOperationException(
                "Some tasks were not found. Missing IDs: " + missingIds,
                missingIds
            );
        }

        int updatedCount = 0;
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                task.setCompleted(true);
                updatedCount++;
                log.debug("Marking task {} as completed", task.getId());
            }
        }

        taskRepository.saveAll(tasks);
        log.info("Successfully completed {} out of {} tasks", updatedCount, ids.size());
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksDueWithinNextSevenDays() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        return taskRepository.findTasksDueWithinNextSevenDays(now, sevenDaysLater);
    }
}