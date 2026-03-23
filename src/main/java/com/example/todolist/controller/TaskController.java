package com.example.todolist.controller;

import com.example.todolist.model.dto.*;
import com.example.todolist.mapper.TaskMapper;
import com.example.todolist.model.Task;
import com.example.todolist.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Task Management", description = "API for tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @Value("${app.api.version:2.0.0}")
    private String apiVersion;

    @Operation(summary = "Get all tasks", description = "Returns a list of all tasks")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks")
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

    @Operation(summary = "Get task by ID", description = "Returns a single task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok()
                .header("X-API-Version", apiVersion)
                .body(taskMapper.toResponseDto(task));
    }

    @Operation(summary = "Create a new task", description = "Creates a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @Validated(OnCreate.class) @RequestBody TaskCreateDto createDto) {
        Task task = taskMapper.toEntity(createDto);
        Task created = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-API-Version", apiVersion)
                .body(taskMapper.toResponseDto(created));
    }

    @Operation(summary = "Update a task", description = "Updates an existing task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody TaskUpdateDto updateDto) {
        Task task = taskService.getTaskById(id);
        taskMapper.updateEntity(updateDto, task);
        Task updated = taskService.updateTask(id, task);
        return ResponseEntity.ok()
                .header("X-API-Version", apiVersion)
                .body(taskMapper.toResponseDto(updated));
    }

    @Operation(summary = "Delete a task", description = "Deletes a task by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent()
                .header("X-API-Version", apiVersion)
                .build();
    }
}