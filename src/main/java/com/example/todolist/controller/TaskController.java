package com.example.todolist.controller;

import com.example.todolist.model.Task;
import com.example.todolist.service.RequestScopedBean;
import com.example.todolist.service.TaskService;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST контроллер для управления задачами
 * Предоставляет API для CRUD операций
 *
 * @author anikanova a.a
 * @version 1.0
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ObjectFactory<RequestScopedBean> requestScopedBeanFactory;

    public TaskController(TaskService taskService,
                          ObjectFactory<RequestScopedBean> requestScopedBeanFactory) {
        this.taskService = taskService;
        this.requestScopedBeanFactory = requestScopedBeanFactory;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable String id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task created = taskService.createTask(task);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id, @RequestBody Task task) {
        Task updated = taskService.updateTask(id, task);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/demo/scopes")
    public ResponseEntity<String> demonstrateScopes() {
        taskService.demonstrateScopes();
        RequestScopedBean requestBean = requestScopedBeanFactory.getObject();
        return ResponseEntity.ok("Scopes demonstrated. Request ID: " + requestBean.getRequestId());
    }
}