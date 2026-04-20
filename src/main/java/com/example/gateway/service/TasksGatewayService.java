package com.example.gateway.service;

import com.example.gateway.client.ExternalTasksClient;
import com.example.gateway.dto.TaskCreateRequest;
import com.example.gateway.dto.TaskDto;
import com.example.gateway.exception.ExternalApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TasksGatewayService {

    private static final Logger log = LoggerFactory.getLogger(TasksGatewayService.class);
    private final ExternalTasksClient externalTasksClient;

    public TasksGatewayService(ExternalTasksClient externalTasksClient) {
        this.externalTasksClient = externalTasksClient;
    }

    @RateLimiter(name = "externalApi")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "createTaskFallback")
    public ExternalTasksClient.TaskResponse createTask(TaskCreateRequest request) {
        log.info("Creating task via external API: {}", request.title());
        return externalTasksClient.createTask(request);
    }

    @RateLimiter(name = "externalApi")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "getTaskFallback")
    public TaskDto getTask(Long id) {
        log.info("Getting task via external API: id={}", id);
        return externalTasksClient.getTask(id).orElseThrow(
                () -> new ExternalApiException("Task not found: " + id));
    }

    @RateLimiter(name = "externalApi")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "getTasksFallback")
    public List<TaskDto> getTasks(Boolean completed, Integer limit) {
        log.info("Getting tasks list via external API: completed={}, limit={}", completed, limit);
        return externalTasksClient.getTasks(completed, limit);
    }

    @RateLimiter(name = "externalApi")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "deleteTaskFallback")
    public void deleteTask(Long id) {
        log.info("Deleting task via external API: id={}", id);
        externalTasksClient.deleteTask(id);
    }

    private ExternalTasksClient.TaskResponse createTaskFallback(TaskCreateRequest request, Throwable t) {
        log.warn("Circuit breaker or rate limiter triggered for createTask: {}", t.getMessage());
        return new ExternalTasksClient.TaskResponse(null, null);
    }

    private TaskDto getTaskFallback(Long id, Throwable t) {
        log.warn("Circuit breaker or rate limiter triggered for getTask id={}: {}", id, t.getMessage());
        return new TaskDto(null, "Unavailable (fallback)", "Service temporarily unavailable", null);
    }

    private List<TaskDto> getTasksFallback(Boolean completed, Integer limit, Throwable t) {
        log.warn("Circuit breaker or rate limiter triggered for getTasks: {}", t.getMessage());
        return List.of();
    }

    private void deleteTaskFallback(Long id, Throwable t) {
        log.warn("Circuit breaker or rate limiter triggered for deleteTask id={}: {}", id, t.getMessage());
    }
}