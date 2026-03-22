package com.example.todolist.model.dto;

import jakarta.annotation.Priority;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class TaskCreateDto {
    @NotBlank(groups = OnCreate.class, message = "Title is required")
    @Size(min = 3, max = 100, groups = OnCreate.class, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, groups = OnCreate.class, message = "Description must not exceed 500 characters")
    private String description;

    @FutureOrPresent(groups = OnCreate.class, message = "Due date must be in the present or future")
    private LocalDate dueDate;

    @NotNull(groups = OnCreate.class, message = "Priority is required")
    private Priority priority;

    @Size(max = 5, groups = OnCreate.class, message = "Maximum 5 tags allowed")
    private Set<String> tags;
}