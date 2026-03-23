package com.example.todolist.service;

import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.model.Task;
import com.example.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Сервисный слой для управления задачами.
 * Содержит бизнес-логику приложения.
 *
 * @author anikanova a.a
 * @version 2.0
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

    @Transactional
    public Task createTask(Task task) {
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
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
                    task.getDueDate().isBefore(existing.getCreatedAt().toLocalDate())) {
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
}