package com.example.todolist.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Модель данных
 * Содержит основную информацию о задаче: идентификатор, заголовок, описание и статус выполнения
 *
 * @author anikanova a.a
 * @version 1.0
 */
public class Task {
    private String id;
    private String title;
    private String description;
    private boolean completed;

    public Task() {
        this.id = UUID.randomUUID().toString();
    }

    public Task(String title, String description, boolean completed) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.completed = completed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                '}';
    }
}