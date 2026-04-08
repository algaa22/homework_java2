package com.example.todolist.controller;

import com.example.todolist.model.Task;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TaskRepository taskRepository;

  private Task task1;
  private Task task2;

  @BeforeEach
  void setUp() {
    taskRepository.deleteAll();

    task1 = Task.builder()
        .title("Test Task 1")
        .priority(Priority.HIGH)
        .build();

    task2 = Task.builder()
        .title("Test Task 2")
        .priority(Priority.MEDIUM)
        .build();

    taskRepository.saveAll(List.of(task1, task2));
  }

  @Test
  void getAllTasks_ShouldReturnAllTasks() throws Exception {
    mockMvc.perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Total-Count"))
        .andExpect(header().exists("X-API-Version"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].title").value("Test Task 1"))
        .andExpect(jsonPath("$[1].title").value("Test Task 2"));
  }

  @Test
  void getTaskById_WhenExists_ShouldReturnTask() throws Exception {
    mockMvc.perform(get("/api/tasks/{id}", task1.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(task1.getId()))
        .andExpect(jsonPath("$.title").value("Test Task 1"));
  }

  @Test
  void getTaskById_WhenNotFound_ShouldReturn404() throws Exception {
    mockMvc.perform(get("/api/tasks/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void createTask_ShouldCreateNewTask() throws Exception {
    Map<String, Object> newTask = Map.of(
        "title", "New Task",
        "priority", "HIGH"
    );

    mockMvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newTask)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("New Task"))
        .andExpect(jsonPath("$.priority").value("HIGH"));
  }

  @Test
  void updateTask_ShouldUpdateExistingTask() throws Exception {
    Map<String, Object> updateData = Map.of("title", "Updated Title");

    mockMvc.perform(put("/api/tasks/{id}", task1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateData)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Title"));
  }

  @Test
  void deleteTask_ShouldDeleteTask() throws Exception {
    mockMvc.perform(delete("/api/tasks/{id}", task1.getId()))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/tasks/{id}", task1.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  void bulkCompleteTasks_WhenAllExist_ShouldCompleteAll() throws Exception {
    List<Long> ids = List.of(task1.getId(), task2.getId());

    mockMvc.perform(post("/api/tasks/bulk-complete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ids)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.completedCount").value(2));
  }

  @Test
  void bulkCompleteTasks_WhenSomeMissing_ShouldReturnConflictAndRollback() throws Exception {
    List<Long> ids = List.of(task1.getId(), 999L);

    mockMvc.perform(post("/api/tasks/bulk-complete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ids)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.rollback").value(true))
        .andExpect(jsonPath("$.missingIds").exists());

    mockMvc.perform(get("/api/tasks/{id}", task1.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.completed").value(false));
  }

  @Test
  void bulkCompleteTasks_WhenEmptyList_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(post("/api/tasks/bulk-complete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(List.of())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAllTasksWithAttachments_ShouldReturnTasksWithAttachmentsHeader() throws Exception {
    mockMvc.perform(get("/api/tasks/with-attachments"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Query-Optimization"))
        .andExpect(header().string("X-Query-Optimization", containsString("EntityGraph")));
  }

  @Test
  void getTasksDueSoon_ShouldReturnTasks() throws Exception {
    mockMvc.perform(get("/api/tasks/due-soon"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Query-Type"))
        .andExpect(header().string("X-Query-Type", "JPQL Custom Query"));
  }
}