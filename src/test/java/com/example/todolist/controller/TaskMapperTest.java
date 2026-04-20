package com.example.todolist.controller;

import com.example.todolist.mapper.TaskMapper;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.Task;
import com.example.todolist.model.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    @Test
    void toEntity_CreateDtoToTask() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("Test Task");
        dto.setDescription("Test Description");
        dto.setDueDate(LocalDate.of(2025, 11, 10));
        dto.setPriority(Priority.HIGH);
        dto.setTags(List.of("test", "java"));

        Task task = taskMapper.toEntity(dto);

        assertThat(task).isNotNull();
        assertThat(task.getTitle()).isEqualTo("Test Task");
        assertThat(task.getDescription()).isEqualTo("Test Description");
        assertThat(task.getDueDate()).isEqualTo(LocalDateTime.of(2025, 11, 10, 0, 0, 0));
        assertThat(task.getPriority()).isEqualTo(Priority.HIGH);

        assertThat(task.getTags()).containsExactlyInAnyOrder("test", "java");
        assertThat(task.isCompleted()).isFalse();
        assertThat(task.getId()).isNull();
        assertThat(task.getCreatedAt()).isNull();
    }

    @Test
    void updateEntity_UpdateOnlyNonNullFields() {
        Task task = new Task();
        task.setTitle("Original Title");
        task.setDescription("Original Description");
        task.setCompleted(false);

        TaskUpdateDto updateDto = new TaskUpdateDto();
        updateDto.setTitle("Updated Title");
        updateDto.setCompleted(true);

        taskMapper.updateEntity(updateDto, task);

        assertThat(task.getTitle()).isEqualTo("Updated Title");
        assertThat(task.getDescription()).isEqualTo("Original Description");
        assertThat(task.isCompleted()).isTrue();
    }

    @Test
    void toResponseDto_TaskToResponseDto() {
        Task task = new Task();
        task.setId(123L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCompleted(true);
        task.setPriority(Priority.MEDIUM);

        List<String> tags = new ArrayList<>();
        tags.add("test");
        task.setTags(tags);
        task.setDueDate(LocalDateTime.of(2025, 11, 10, 12, 0));

        TaskResponseDto dto = taskMapper.toResponseDto(task);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(123L);
        assertThat(dto.getTitle()).isEqualTo("Test Task");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.isCompleted()).isTrue();
        assertThat(dto.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(dto.getTags()).containsExactly("test");
    }
}