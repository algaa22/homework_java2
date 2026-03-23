package com.example.todolist.model.dto;

import com.example.todolist.model.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Schema(description = "Response DTO for task data")
public class TaskResponseDto {

    @Schema(description = "Task unique identifier")
    private Long id;

    @Schema(description = "Task title", example = "Complete MWS Cloud project", minLength = 3, maxLength = 100)
    private String title;

    @Schema(description = "Task description", example = "Implement all features for the ToDo List application", maxLength = 500)
    private String description;

    @Schema(description = "Completion status", example = "false")
    private boolean completed;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Due date for the task", example = "2026-03-22")
    private LocalDate dueDate;

    @Schema(description = "Task priority", example = "LOW", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private Priority priority;

    @Schema(description = "Task tags", example = "[\"spring\", \"MWS\", \"homework\"]", maxLength = 5)
    private Set<String> tags;
}