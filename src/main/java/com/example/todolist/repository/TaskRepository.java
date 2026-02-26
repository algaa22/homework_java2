package com.example.todolist.repository;

import com.example.todolist.model.Task;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория
 * Определяет контракт для операций CRUD с хранилищем задач
 *
 * @author anikanova a.a
 * @version 1.0
 */
public interface TaskRepository {
    List<Task> findAll();
    Optional<Task> findById(String id);
    Task save(Task task);

    void deleteById(String id);
    boolean existsById(String id);
}