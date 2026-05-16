package com.example.todolist.controller;

import com.example.todolist.model.dto.OnCreate;
import com.example.todolist.model.dto.TaskCreateDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TaskValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validTaskCreateDto_HaveNoViolations() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Valid Title");
        dto.setDueDate(LocalDate.now().plusDays(1));
        dto.setPriority(com.example.todolist.model.enums.Priority.HIGH);

        Set<ConstraintViolation<TaskCreateDto>> violations = validator.validate(dto, OnCreate.class);

        assertThat(violations).isEmpty();
    }

    @Test
    void taskWithBlankTitle_HaveViolation() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("   ");
        dto.setPriority(com.example.todolist.model.enums.Priority.HIGH);

        Set<ConstraintViolation<TaskCreateDto>> violations = validator.validate(dto, OnCreate.class);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("title");
    }

    @Test
    void taskWithPastDueDate_HaveViolation() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Valid Title");
        dto.setDueDate(LocalDate.now().minusDays(1));
        dto.setPriority(com.example.todolist.model.enums.Priority.HIGH);

        Set<ConstraintViolation<TaskCreateDto>> violations = validator.validate(dto, OnCreate.class);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("dueDate");
    }

    @Test
    void taskWithMoreThan5Tags_HaveViolation() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Valid Title");
        dto.setPriority(com.example.todolist.model.enums.Priority.HIGH);
        dto.setTags(List.of("1", "2", "3", "4", "5", "6"));

        Set<ConstraintViolation<TaskCreateDto>> violations = validator.validate(dto, OnCreate.class);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("tags");
    }
}