package com.example.todolist.model.dto;

import com.example.todolist.model.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Schema(description = "Request DTO for the task")
public class TaskCreateDto {

    @NotBlank(groups = OnCreate.class, message = "Title is required")
    @Size(min = 3, max = 100, groups = OnCreate.class, message = "Title must be between 3 and 100 characters")
    @Schema(description = "Task title", minLength = 3, maxLength = 100)
    private String title;

    @Size(max = 500, groups = OnCreate.class, message = "Description must not exceed 500 characters")
    @Schema(description = "Task description", maxLength = 500)
    private String description;

    @FutureOrPresent(groups = OnCreate.class, message = "Due date must be in the present or future")
    @Schema(description = "Due date for the task", example = "2026-04-06")
    private LocalDate dueDate;

    @NotNull(groups = OnCreate.class, message = "Priority is required")
    @Schema(description = "Task priority", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private Priority priority;

    @Size(max = 5, groups = OnCreate.class, message = "Maximum 5 tags allowed")
    @Schema(description = "Task tags", example = "[\"homework\", \"java\", \"MWS\"]", maxLength = 5)
    private List<String> tags;
}