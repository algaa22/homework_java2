package com.example.todolist.repository;

import com.example.todolist.model.Task;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    private LocalDateTime now;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        task1 = Task.builder()
                .title("Task 1")
                .description("Description 1")
                .status(TaskStatus.PENDING)
                .priority(Priority.HIGH)
                .dueDate(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        task2 = Task.builder()
                .title("Task 2")
                .description("Description 2")
                .status(TaskStatus.IN_PROGRESS)
                .priority(Priority.MEDIUM)
                .dueDate(now.plusDays(5))
                .createdAt(now)
                .updatedAt(now)
                .build();

        task3 = Task.builder()
                .title("Task 3")
                .description("Description 3")
                .status(TaskStatus.PENDING)
                .priority(Priority.LOW)
                .dueDate(now.plusDays(6))
                .createdAt(now)
                .updatedAt(now)
                .build();

        taskRepository.deleteAll();
        taskRepository.saveAll(List.of(task1, task2, task3));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findTasksDueWithinNextSevenDays_ShouldReturnTasksWithDueDateInRange() {
        LocalDateTime start = now;
        LocalDateTime end = now.plusDays(7);

        List<Task> tasks = taskRepository.findTasksDueWithinNextSevenDays(start, end);

        assertThat(tasks).isNotEmpty();
        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(Task::getDueDate)
                .allMatch(dueDate ->
                        !dueDate.isBefore(start) && !dueDate.isAfter(end)
                );
    }

    @Test
    void findTasksDueWithinNextSevenDays_WhenNoTasksInRange_ShouldReturnEmptyList() {
        LocalDateTime start = now.plusDays(10);
        LocalDateTime end = now.plusDays(14);

        List<Task> tasks = taskRepository.findTasksDueWithinNextSevenDays(start, end);

        assertThat(tasks).isEmpty();
    }
}