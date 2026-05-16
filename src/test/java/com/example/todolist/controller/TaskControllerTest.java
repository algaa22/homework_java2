package com.example.todolist.controller;

import com.example.todolist.mapper.TaskMapper;
import com.example.todolist.model.Task;
import com.example.todolist.model.dto.TaskCreateDto;
import com.example.todolist.model.dto.TaskResponseDto;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@ActiveProfiles("test")
@WithMockUser
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TaskMapper taskMapper;

    @Test
    @DisplayName("POST /api/tasks — Успешное создание задачи")
    void givenValidTaskCreateDto_whenCreateTask_thenReturn201AndJson() throws Exception {
        TaskCreateDto createDto = new TaskCreateDto();
        createDto.setTitle("Сделать домашку");
        createDto.setPriority(Priority.MEDIUM);
        createDto.setDescription("Дома");

        Task mockedTask = new Task();
        mockedTask.setId(1L);
        mockedTask.setTitle("Сделать домашку");

        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle("Сделать домашку");
        responseDto.setCompleted(false);

        when(taskMapper.toEntity(any(TaskCreateDto.class))).thenReturn(mockedTask);
        when(taskService.createTask(any(Task.class))).thenReturn(mockedTask);
        when(taskMapper.toResponseDto(any(Task.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-API-Version"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Сделать домашку"));
    }

    @Test
    @DisplayName("GET /api/tasks/{id} — Получение существующей задачи")
    void givenExistingTaskId_whenGetTaskById_thenReturn200AndJson() throws Exception {
        Long taskId = 1L;
        Task mockedTask = new Task();
        mockedTask.setId(taskId);

        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(taskId);

        when(taskService.getTaskById(taskId)).thenReturn(mockedTask);
        when(taskMapper.toResponseDto(mockedTask)).thenReturn(responseDto);

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-API-Version"))
                .andExpect(jsonPath("$.id").value(taskId));
    }
}