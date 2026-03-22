package com.example.todolist.controller;

import com.example.todolist.dto.*;
import com.example.todolist.mapper.TaskMapper;
import com.example.todolist.model.Task;
import com.example.todolist.model.dto.OnCreate;
import com.example.todolist.model.dto.TaskCreateDto;
import com.example.todolist.model.dto.TaskResponseDto;
import com.example.todolist.model.dto.TaskUpdateDto;
import com.example.todolist.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления задачами
 *
 * @author anikanova a.a
 * @version 2.0
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @Value("${app.api.version:2.0.0}")
    private String apiVersion;

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        List<TaskResponseDto> tasks = taskService.getAllTasks().stream()
                .map(taskMapper::toResponseDto)
                .toList();

        long totalCount = taskService.count();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(totalCount))
                .header("X-API-Version", apiVersion)
                .body(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable String id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok()
                .header("X-API-Version", apiVersion)
                .body(taskMapper.toResponseDto(task));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @Validated(OnCreate.class) @RequestBody TaskCreateDto createDto) {
        Task task = taskMapper.toEntity(createDto);
        Task created = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-API-Version", apiVersion)
                .body(taskMapper.toResponseDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable String id,
            @Validated(OnUpdate.class) @RequestBody TaskUpdateDto updateDto) {
        Task task = taskService.getTaskById(id);
        taskMapper.updateEntity(updateDto, task);
        Task updated = taskService.updateTask(id, task);
        return ResponseEntity.ok()
                .header("X-API-Version", apiVersion)
                .body(taskMapper.toResponseDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent()
                .header("X-API-Version", apiVersion)
                .build();
    }
}