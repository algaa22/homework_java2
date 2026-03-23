package com.example.todolist.repository;

import com.example.todolist.model.Task;

import java.util.Collection;
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
    Optional<Task> findById(Long id);
    Task save(Task task);

    void deleteById(Long id);
    boolean existsById(Long id);
    List<Task> findAllById(Collection<Long> ids);
    long count();
}