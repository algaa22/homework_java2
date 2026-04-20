package com.example.gateway.client;

import com.example.gateway.dto.*;
import com.example.gateway.exception.ExternalApiException;
import com.example.gateway.exception.TaskNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
public class ExternalTasksClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalTasksClient.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ExternalTasksClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public TaskResponse createTask(TaskCreateRequest request) {
        try {
            ResponseEntity<TaskDto> response = restClient.post()
                    .uri("/external/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toEntity(TaskDto.class);

            String location = Optional.ofNullable(response.getHeaders().getLocation())
                    .map(Object::toString)
                    .orElse(null);

            return new TaskResponse(response.getBody(), location);
        } catch (RestClientResponseException e) {
            throw handleError(e, "Failed to create task");
        }
    }

    public Optional<TaskDto> getTask(Long id) {
        try {
            TaskDto task = restClient.get()
                    .uri("/external/v1/tasks/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(TaskDto.class);
            return Optional.ofNullable(task);
        } catch (HttpClientErrorException.NotFound e) {
            ProblemDetails pd = parseProblemDetails(e);
            throw new TaskNotFoundException(
                    pd != null && pd.detail() != null ? pd.detail() : "Task not found: " + id, id);
        } catch (RestClientResponseException e) {
            throw handleError(e, "Failed to get task " + id);
        }
    }

    public List<TaskDto> getTasks(Boolean completed, Integer limit) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/external/v1/tasks");
                        if (completed != null) builder.queryParam("completed", completed);
                        if (limit != null) builder.queryParam("limit", limit);
                        return builder.build();
                    })
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TaskDto>>() {});
        } catch (RestClientResponseException e) {
            throw handleError(e, "Failed to get tasks list");
        }
    }

    public void deleteTask(Long id) {
        try {
            restClient.delete()
                    .uri("/external/v1/tasks/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw handleError(e, "Failed to delete task " + id);
        }
    }

    private ProblemDetails parseProblemDetails(RestClientResponseException e) {
        try {
            byte[] body = e.getResponseBodyAsByteArray();
            if (body != null && body.length > 0) {
                return objectMapper.readValue(body, ProblemDetails.class);
            }
        } catch (Exception ignore) {}
        return null;
    }

    private ExternalApiException handleError(RestClientResponseException e, String context) {
        String safeBody = getSafeBody(e);
        log.error("{}: status={}, body={}", context, e.getStatusCode(), safeBody);
        return new ExternalApiException("External API error: " + e.getStatusCode());
    }

    private String getSafeBody(RestClientResponseException e) {
        byte[] body = e.getResponseBodyAsByteArray();
        if (body == null || body.length == 0) return "";
        if (body.length > 500) {
            return new String(body, 0, 500, StandardCharsets.UTF_8) + "... [truncated]";
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    public record TaskResponse(TaskDto task, String location) {}
}