package com.example.todolist.repository;

import com.example.todolist.model.Task;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация репозитория задач, хранящая данные в оперативной памяти
 * Является основным (Primary) репозиторием приложения
 *
 * @author anikanova a.a
 * @version 1.0
 */
@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<String, Task> storage = new ConcurrentHashMap<>();

    @Override
    public List<Task> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Task save(Task task) {
        storage.put(task.getId(), task);
        return task;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public List<Task> findAllById(Collection<Long> ids) {
        return List.of();
    }

    @Override
    public long count() {
        return storage.size();
    }
}