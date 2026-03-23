package com.example.todolist.repository;

import com.example.todolist.model.Task;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Заглушка репозитория задач
 * Содержит предопределенные тестовые данные и не поддерживает операции изменения
 * Создается через @Bean в конфигурационном классе
 *
 * @author anikanova a.a
 * @version 1.0
 */
public class StubTaskRepository implements TaskRepository {

    private final List<Task> stubData = new ArrayList<>();

    public StubTaskRepository() {
        stubData.add(new Task("Stub Task 1", "Description 1", false));
        stubData.add(new Task("Stub Task 2", "Description 2", true));
    }

    @Override
    public List<Task> findAll() {
        return List.copyOf(stubData);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return stubData.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    @Override
    public Task save(Task task) {
        throw new UnsupportedOperationException("Stub repository doesn't support save");
    }

    @Override
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("Stub repository doesn't support delete");
    }

    @Override
    public boolean existsById(Long id) {
        return stubData.stream().anyMatch(task -> task.getId().equals(id));
    }

    @Override
    public long count() {
        return stubData.size();
    }

    @Override
    public List<Task> findAllById(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return stubData.stream()
                .filter(task -> ids.contains(task.getId()))
                .collect(Collectors.toList());
    }
}