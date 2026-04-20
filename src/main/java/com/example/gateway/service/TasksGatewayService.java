package com.example.gateway.service;

import com.example.gateway.client.ExternalTasksClient;
import com.example.gateway.dto.TaskCreateRequest;
import com.example.gateway.dto.TaskDto;
import com.example.gateway.exception.TaskNotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

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
                () -> new TaskNotFoundException("Task not found: " + id, id));
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
        log.warn("Fallback createTask: {}", t.getMessage());
        return new ExternalTasksClient.TaskResponse(null, null);
    }

    private TaskDto getTaskFallback(Long id, Throwable t) {
        log.warn("FALLBACK for getTask id={}: {} - {}", id, t.getClass().getSimpleName(), t.getMessage());

        if (t instanceof TaskNotFoundException) {
            throw (TaskNotFoundException) t;
        }

        String message = t.getMessage();
        if (message != null && (message.contains("timeout") || message.contains("I/O error") || message.contains("read"))) {
            throw new RuntimeException(t);
        }

        if (t instanceof RequestNotPermitted) {
            return new TaskDto(null, "Rate limit (fallback)", "Too many requests, please slow down", null);
        }

        if (t instanceof CallNotPermittedException) {
            return new TaskDto(null, "Circuit breaker (fallback)", "Service temporarily unavailable", null);
        }

        if (t instanceof RestClientResponseException) {
            RestClientResponseException rcre = (RestClientResponseException) t;
            if (rcre.getStatusCode().is5xxServerError()) {
                return new TaskDto(null, "Server error (fallback)", "External service error", null);
            }
            if (rcre.getStatusCode().value() == 404) {
                throw rcre;
            }
        }

        return new TaskDto(null, "Unavailable (fallback)", "Service temporarily unavailable: " + message, null);
    }

    private List<TaskDto> getTasksFallback(Boolean completed, Integer limit, Throwable t) {
        log.warn("Fallback getTasks: {}", t.getMessage());
        return List.of();
    }

    private void deleteTaskFallback(Long id, Throwable t) {
        log.warn("Fallback deleteTask id={}: {}", id, t.getMessage());
    }
}