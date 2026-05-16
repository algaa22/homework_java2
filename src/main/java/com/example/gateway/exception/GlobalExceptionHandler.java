package com.example.gateway.exception;

import com.example.gateway.dto.ApiError;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiError> handleTaskNotFound(TaskNotFoundException e) {
        log.warn("Task not found: id={}", e.getTaskId());
        ApiError error = new ApiError(
                "NOT_FOUND",
                e.getMessage(),
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiError> handleExternalApiError(ExternalApiException e) {
        log.error("External API error: {}", e.getMessage());
        ApiError error = new ApiError(
                "EXTERNAL_API_ERROR",
                e.getMessage(),
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ApiError> handleCircuitBreakerOpen(CallNotPermittedException e) {
        log.warn("Circuit breaker is OPEN: {}", e.getMessage());
        ApiError error = new ApiError(
                "SERVICE_UNAVAILABLE",
                "External service is temporarily unavailable. Please try again later.",
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        ApiError error = new ApiError(
                "UNAUTHORIZED",
                "Authentication failed: " + e.getMessage(),
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        ApiError error = new ApiError(
                "FORBIDDEN",
                "You don't have permission to access this resource",
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiError> handleHttpClientError(HttpClientErrorException e) {
        log.warn("HTTP client error: status={}, body={}",
                e.getStatusCode(), safeGetBody(e));

        HttpStatus status = (HttpStatus) e.getStatusCode();
        ApiError error = new ApiError(
                "CLIENT_ERROR_" + status.value(),
                e.getMessage(),
                MDC.get("traceId")
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiError> handleHttpServerError(HttpServerErrorException e) {
        log.error("HTTP server error: status={}, body={}",
                e.getStatusCode(), safeGetBody(e));

        ApiError error = new ApiError(
                "EXTERNAL_SERVER_ERROR",
                "External service returned an error",
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiError> handleRestClientError(RestClientResponseException e) {
        log.error("RestClient error: status={}, body={}",
                e.getStatusCode(), safeGetBody(e));

        ApiError error = new ApiError(
                "HTTP_ERROR_" + e.getStatusCode().value(),
                "External API request failed: " + e.getMessage(),
                MDC.get("traceId")
        );

        int statusCode = e.getStatusCode().value();
        if (statusCode >= 400 && statusCode < 500) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    // ==================== VALIDATION ERRORS ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");

        log.warn("Validation failed: {}", errors);
        ApiError error = new ApiError(
                "VALIDATION_ERROR",
                errors,
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: parameter={}, value={}", e.getName(), e.getValue());
        ApiError error = new ApiError(
                "INVALID_PARAMETER",
                "Parameter '" + e.getName() + "' has invalid value: " + e.getValue(),
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getParameterName());
        ApiError error = new ApiError(
                "MISSING_PARAMETER",
                "Required parameter '" + e.getParameterName() + "' is missing",
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("Malformed JSON request: {}", e.getMessage());
        ApiError error = new ApiError(
                "MALFORMED_REQUEST",
                "Request body is malformed or invalid",
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getResourcePath());
        ApiError error = new ApiError(
                "ENDPOINT_NOT_FOUND",
                "The requested endpoint does not exist",
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        ApiError error = new ApiError(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                MDC.get("traceId")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String safeGetBody(RestClientResponseException e) {
        byte[] body = e.getResponseBodyAsByteArray();
        if (body == null || body.length == 0) {
            return "[no body]";
        }
        if (body.length > 500) {
            return new String(body, 0, 500, java.nio.charset.StandardCharsets.UTF_8) + "... [truncated]";
        }
        return new String(body, java.nio.charset.StandardCharsets.UTF_8);
    }
}