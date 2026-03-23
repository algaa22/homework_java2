package com.example.todolist.model.dto;

import com.example.todolist.model.enums.Priority;
import com.example.todolist.validator.DueDateNotBeforeCreation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@DueDateNotBeforeCreation(groups = OnUpdate.class)
public class TaskUpdateDto {
    @Size(min = 3, max = 100, groups = OnUpdate.class)
    @Schema(description = "Task title", example = "Updated task title", minLength = 3, maxLength = 100)
    private String title;

    @Size(max = 500, groups = OnUpdate.class)
    @Schema(description = "Task description", example = "Updated description", maxLength = 500)
    private String description;

    @Schema(description = "Completion status", example = "true")
    private Boolean completed;

    @FutureOrPresent(groups = OnUpdate.class)
    @Schema(description = "Due date for the task", example = "2026-03-22")
    private LocalDate dueDate;

    @Schema(description = "Task priority", example = "LOW", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private Priority priority;

    @Size(max = 5, groups = OnUpdate.class)
    @Schema(description = "Task tags", example = "[\"new\", \"hometask\"]", maxLength = 5)
    private Set<String> tags;
}