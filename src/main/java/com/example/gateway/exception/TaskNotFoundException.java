package com.example.gateway.exception;

public class TaskNotFoundException extends RuntimeException {
    private final Long taskId;

    public TaskNotFoundException(String message, Long taskId) {
        super(message);
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }
}
