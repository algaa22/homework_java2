package com.example.todolist.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@Schema(description = "Error response DTO for API exceptions")
public class ErrorResponse {

    @Schema(description = "Timestamp of the error", example = "2026-03-22T09:30:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "Validation failed")
    private String message;

    @Schema(description = "Request path that caused the error", example = "/api/tasks")
    private String path;

    @Schema(description = "Additional error details (e.g., validation field errors)")
    private Map<String, Object> details;
}