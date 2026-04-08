package com.example.todolist.controller;

import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.dto.TaskCreateDto;
import com.example.todolist.model.dto.TaskResponseDto;
import com.example.todolist.model.dto.TaskUpdateDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

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

    private TaskCreateDto createTestTaskDto(String title) {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle(title);
        dto.setDescription("Test Description");
        dto.setDueDate(LocalDate.now().plusDays(7));
        dto.setPriority(Priority.MEDIUM);
        dto.setTags(List.of("test"));
        return dto;
    }

    @Test
    void getAllTasks_List() {
        ResponseEntity<TaskResponseDto[]> response = restTemplate.getForEntity(baseUrl, TaskResponseDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get("X-API-Version")).contains("3.0.0");
        assertThat(response.getHeaders().get("X-Total-Count")).isNotNull();
    }

    @Test
    void getTaskById_WithValidId() {
        TaskCreateDto createDto = createTestTaskDto("Task for Get");
        ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(baseUrl, createDto, TaskResponseDto.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TaskResponseDto created = createResponse.getBody();
        assertThat(created).isNotNull();

        ResponseEntity<TaskResponseDto> response = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(),
                TaskResponseDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getTitle()).isEqualTo("Task for Get");
        assertThat(response.getHeaders().get("X-API-Version")).contains("3.0.0");
    }

    @Test
    void getTaskById_WithInvalidId_NotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/999999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createTask_WithValidData_Created() {
        TaskCreateDto createDto = createTestTaskDto("New Task");

        ResponseEntity<TaskResponseDto> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(createDto),
                new ParameterizedTypeReference<TaskResponseDto>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("New Task");
    }

    @Test
    void createTask_WithBlankTitle_BadRequest() {
        TaskCreateDto createDto = createTestTaskDto("");
        createDto.setTitle("");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, createDto, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createTask_WithTitleTooShort_BadRequest() {
        TaskCreateDto createDto = createTestTaskDto("AB");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, createDto, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createTask_WithoutPriority_BadRequest() {
        TaskCreateDto createDto = createTestTaskDto("Task");
        createDto.setPriority(null);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, createDto, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createTask_WithPastDueDate_BadRequest() {
        TaskCreateDto createDto = createTestTaskDto("Task");
        createDto.setDueDate(LocalDate.now().minusDays(1));

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, createDto, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createTask_WithMoreThan5Tags_BadRequest() {
        TaskCreateDto createDto = createTestTaskDto("Task");
        createDto.setTags(List.of("1", "2", "3", "4", "5", "6"));

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, createDto, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateTask_WithValidData_Ok() {
        TaskCreateDto createDto = createTestTaskDto("Original Title");
        ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(baseUrl, createDto, TaskResponseDto.class);
        TaskResponseDto created = createResponse.getBody();
        assertThat(created).isNotNull();

        TaskUpdateDto updateDto = new TaskUpdateDto();
        updateDto.setTitle("Updated Title");
        updateDto.setCompleted(true);

        HttpEntity<TaskUpdateDto> requestEntity = new HttpEntity<>(updateDto);
        ResponseEntity<TaskResponseDto> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.PUT,
                requestEntity,
                TaskResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Title");
        assertThat(response.getBody().isCompleted()).isTrue();
        assertThat(response.getHeaders().get("X-API-Version")).contains("3.0.0");
    }

    @Test
    void updateTask_WithInvalidId_NotFound() {
        TaskUpdateDto updateDto = new TaskUpdateDto();
        updateDto.setTitle("Updated Title");

        HttpEntity<TaskUpdateDto> requestEntity = new HttpEntity<>(updateDto);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999999",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTask_WithTitleTooShort_BadRequest() {
        TaskCreateDto createDto = createTestTaskDto("Original Title");
        ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(baseUrl, createDto, TaskResponseDto.class);
        TaskResponseDto created = createResponse.getBody();
        assertThat(created).isNotNull();

        TaskUpdateDto updateDto = new TaskUpdateDto();
        updateDto.setTitle("AB");

        HttpEntity<TaskUpdateDto> requestEntity = new HttpEntity<>(updateDto);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.PUT,
                requestEntity,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteTask_WithValidId_NoContent() {
        TaskCreateDto createDto = createTestTaskDto("Task to Delete");
        ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(baseUrl, createDto, TaskResponseDto.class);
        TaskResponseDto created = createResponse.getBody();
        assertThat(created).isNotNull();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + created.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getHeaders().get("X-API-Version")).contains("3.0.0");

        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + created.getId(),
                String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteTask_WithInvalidId_NotFound() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999999",
                HttpMethod.DELETE,
                null,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void headers_ContainApiVersion() {
        ResponseEntity<TaskResponseDto[]> response = restTemplate.getForEntity(baseUrl, TaskResponseDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get("X-API-Version")).contains("3.0.0");
    }
}