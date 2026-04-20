package com.example.gateway.external;

import com.example.gateway.dto.ProblemDetails;
import com.example.gateway.dto.TaskCreateRequest;
import com.example.gateway.dto.TaskDto;
import com.example.gateway.dto.TaskPatchRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {

    private final Map<Long, TaskDto> taskStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(3);
    private String unstableMode = "normal";

    public ExternalApiController() {
        taskStore.put(1L, new TaskDto(1L, "Buy milk", "2 liters", false));
        taskStore.put(2L, new TaskDto(2L, "Read book", "Chapter 1-5", true));
    }


    @GetMapping("/unstable")
    public ResponseEntity<String> unstable(@RequestParam String mode) {
        this.unstableMode = mode;
        String message = switch (mode) {
            case "timeout" -> "Timeout mode activated (10s delay)";
            case "500" -> "500 error mode activated";
            case "429" -> "429 rate limit mode activated";
            case "html" -> "HTML error mode activated";
            default -> "Normal mode activated";
        };
        return ResponseEntity.ok(message);
    }


    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Integer limit) {

        if ("500".equals(unstableMode)) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }

        var stream = taskStore.values().stream();
        if (completed != null) {
            stream = stream.filter(t -> t.completed().equals(completed));
        }
        if (limit != null && limit > 0) {
            stream = stream.limit(limit);
        }
        return ResponseEntity.ok(stream.toList());
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTask(@PathVariable Long id) throws InterruptedException {

        if ("timeout".equals(unstableMode)) {
            Thread.sleep(10000);
        }

        if ("500".equals(unstableMode)) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }

        if ("429".equals(unstableMode)) {
            return ResponseEntity.status(429)
                    .header("Retry-After", "30")
                    .body("Too Many Requests");
        }

        if ("html".equals(unstableMode)) {
            return ResponseEntity.status(502)
                    .header("Content-Type", "text/html")
                    .body("<html><body><h1>Gateway Error</h1><p>Bad Gateway</p></body></html>");
        }

        TaskDto task = taskStore.get(id);
        if (task == null) {
            ProblemDetails error = new ProblemDetails(
                    "https://example.com/errors/not-found",
                    "Not Found",
                    404,
                    "Task " + id + " not found"
            );
            return ResponseEntity.status(404).body(error);
        }
        return ResponseEntity.ok(task);
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody TaskCreateRequest request) {

        if ("500".equals(unstableMode)) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }

        Long id = idGenerator.getAndIncrement();
        TaskDto task = new TaskDto(id, request.title(), request.description(), false);
        taskStore.put(id, task);
        return ResponseEntity
                .created(URI.create("/external/v1/tasks/" + id))
                .body(task);
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<?> patchTask(@PathVariable Long id, @RequestBody TaskPatchRequest request) {

        if ("500".equals(unstableMode)) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }

        TaskDto existing = taskStore.get(id);
        if (existing == null) {
            ProblemDetails error = new ProblemDetails(
                    "https://example.com/errors/not-found",
                    "Not Found",
                    404,
                    "Task " + id + " not found"
            );
            return ResponseEntity.status(404).body(error);
        }
        TaskDto updated = new TaskDto(
                existing.id(),
                existing.title(),
                existing.description(),
                request.completed() != null ? request.completed() : existing.completed()
        );
        taskStore.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {

        if ("500".equals(unstableMode)) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }

        taskStore.remove(id);
        return ResponseEntity.noContent().build();
    }
}