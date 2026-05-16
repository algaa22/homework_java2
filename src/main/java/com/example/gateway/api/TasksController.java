package com.example.gateway.api;

import com.example.gateway.dto.TaskCreateRequest;
import com.example.gateway.dto.TaskDto;
import com.example.gateway.service.TasksGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TasksController {

    private final TasksGatewayService tasksGatewayService;

    public TasksController(TasksGatewayService tasksGatewayService) {
        this.tasksGatewayService = tasksGatewayService;
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskCreateRequest request) {
        var response = tasksGatewayService.createTask(request);
        if (response.task() == null) {
            return ResponseEntity.status(503).build();
        }
        return ResponseEntity.ok(response.task());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        TaskDto task = tasksGatewayService.getTask(id);
        if (task.id() == null) {
            return ResponseEntity.status(503).body(task);
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Integer limit) {
        List<TaskDto> tasks = tasksGatewayService.getTasks(completed, limit);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        tasksGatewayService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}