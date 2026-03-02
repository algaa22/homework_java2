package com.example.todolist.controller.src.test.java.com.example.todolist.controller;

import com.example.todolist.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TaskControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/tasks";
    }

    private Task createTestTask(String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Test Description");
        task.setCompleted(false);
        return task;
    }

    @Test
    void getAllTasks() {
        ResponseEntity<Task[]> response = restTemplate.getForEntity(baseUrl, Task[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getTaskById_WithValidId() {
        Task newTask = createTestTask("Task for Get");
        ResponseEntity<Task> createResponse = restTemplate.postForEntity(baseUrl, newTask, Task.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Task created = createResponse.getBody();
        assertThat(created).isNotNull();
        ResponseEntity<Task> response = restTemplate.getForEntity(baseUrl + "/" + created.getId(), Task.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getTitle()).isEqualTo("Task for Get");
    }

    @Test
    void getTaskById_WithInvalidId() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/non-existent-id", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void createTask_WithValidData() {
        Task newTask = createTestTask("New Task");
        ResponseEntity<Task> response = restTemplate.postForEntity(baseUrl, newTask, Task.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("New Task");
    }

    @Test
    void createTask_WithNullTitle() {
        Task newTask = new Task();
        newTask.setTitle(null);
        newTask.setDescription("Test");
        newTask.setCompleted(false);
        ResponseEntity<Task> response = restTemplate.postForEntity(baseUrl, newTask, Task.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isNull();
    }

    @Test
    void updateTask_WithValidId() {
        Task newTask = createTestTask("Original Title");
        ResponseEntity<Task> createResponse = restTemplate.postForEntity(baseUrl, newTask, Task.class);
        Task created = createResponse.getBody();
        assertThat(created).isNotNull();
        created.setTitle("Updated Title");
        created.setCompleted(true);
        HttpEntity<Task> requestEntity = new HttpEntity<>(created);
        ResponseEntity<Task> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.PUT,
                requestEntity,
                Task.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Title");
        assertThat(response.getBody().isCompleted()).isTrue();
    }

    @Test
    void updateTask_WithInvalidId() {
        Task task = createTestTask("Task");
        HttpEntity<Task> requestEntity = new HttpEntity<>(task);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/non-existent-id",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteTask_WithValidId() {
        Task newTask = createTestTask("Task to Delete");
        ResponseEntity<Task> createResponse = restTemplate.postForEntity(baseUrl, newTask, Task.class);
        Task created = createResponse.getBody();
        assertThat(created).isNotNull();
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void deleteTask_WithInvalidId() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/non-existent-id",
                HttpMethod.DELETE,
                null,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void demonstrateScopes() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/demo/scopes", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Request ID:");
    }
}