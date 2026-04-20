package com.example.gateway.external;

import com.example.gateway.dto.ProblemDetails;
import com.example.gateway.dto.TaskCreateRequest;
import com.example.gateway.dto.TaskDto;
import com.example.gateway.dto.TaskPatchRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {

    private final Map<Long, TaskDto> taskStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ExternalApiController() {
        taskStore.put(1L, new TaskDto(1L, "Buy milk", "2 liters", false));
        taskStore.put(2L, new TaskDto(2L, "Read book", "Chapter 1-5", true));
    }

    @GetMapping("/tasks")
    public List<TaskDto> getTasks(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Integer limit) {

        var stream = taskStore.values().stream();
        if (completed != null) {
            stream = stream.filter(t -> t.completed().equals(completed));
        }
        if (limit != null && limit > 0) {
            stream = stream.limit(limit);
        }
        return stream.toList();
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTask(@PathVariable Long id) {
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
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskCreateRequest request,
                                              HttpServletResponse response) {
        Long id = idGenerator.getAndIncrement();
        TaskDto task = new TaskDto(id, request.title(), request.description(), false);
        taskStore.put(id, task);
        return ResponseEntity
                .created(URI.create("/external/v1/tasks/" + id))
                .body(task);
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<?> patchTask(@PathVariable Long id, @RequestBody TaskPatchRequest request) {
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
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskStore.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unstable")
    public ResponseEntity<?> unstable(@RequestParam String mode) throws InterruptedException {
        switch (mode) {
            case "timeout":
                Thread.sleep(10000);
                return ResponseEntity.ok("This should not be seen");
            case "500":
                return ResponseEntity.status(500).body("Internal Server Error");
            case "429":
                return ResponseEntity.status(429)
                        .header("Retry-After", "30")
                        .body("Too Many Requests");
            case "html":
                return ResponseEntity.status(502)
                        .header("Content-Type", "text/html")
                        .body("<html><body>Gateway Error</body></html>");
            default:
                return ResponseEntity.ok("Stable response");
        }
    }
}