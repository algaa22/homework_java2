package com.example.todolist.repository;

import com.example.todolist.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Optional<Task> findById(String id) {
        return stubData.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    @Override
    public Task save(Task task) {
        throw new UnsupportedOperationException("Stub repository doesn't support save");
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException("Stub repository doesn't support delete");
    }

    @Override
    public boolean existsById(String id) {
        return stubData.stream().anyMatch(task -> task.getId().equals(id));
    }
}