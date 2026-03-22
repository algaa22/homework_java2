package com.example.todolist.model.dto;

import jakarta.annotation.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class TaskUpdateDto {
    @Size(min = 3, max = 100, groups = OnUpdate.class)
    private String title;

    @Size(max = 500, groups = OnUpdate.class)
    private String description;

    private Boolean completed;

    @FutureOrPresent(groups = OnUpdate.class)
    private LocalDate dueDate;

    private Priority priority;

    @Size(max = 5, groups = OnUpdate.class)
    private Set<String> tags;
}