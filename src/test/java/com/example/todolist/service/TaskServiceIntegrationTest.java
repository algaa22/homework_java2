package com.example.todolist.service;

import com.example.todolist.exception.BulkOperationException;
import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.model.Task;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.enums.TaskStatus;
import com.example.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceIntegrationTest {

  @Autowired
  private TaskService taskService;

  @Autowired
  private TaskRepository taskRepository;

  private Task task1;
  private Task task2;
  private Task task3;

  @BeforeEach
  void setUp() {
    taskRepository.deleteAll();

    task1 = Task.builder()
        .title("Task 1")
        .priority(Priority.HIGH)
        .status(TaskStatus.PENDING)
        .build();
    task1.setCompleted(false);

    task2 = Task.builder()
        .title("Task 2")
        .priority(Priority.MEDIUM)
        .status(TaskStatus.PENDING)
        .build();
    task2.setCompleted(false);

    task3 = Task.builder()
        .title("Task 3")
        .priority(Priority.LOW)
        .status(TaskStatus.IN_PROGRESS)
        .build();
    task3.setCompleted(false);

    task1.setDueDate(LocalDateTime.now().plusDays(3));
    task2.setDueDate(LocalDateTime.now().plusDays(5));
    task3.setDueDate(LocalDateTime.now().plusDays(10));

    taskRepository.saveAll(List.of(task1, task2, task3));
  }

  @Test
  void getTaskById_ShouldReturnTask_WhenExists() {
    Task found = taskService.getTaskById(task1.getId());

    assertThat(found).isNotNull();
    assertThat(found.getTitle()).isEqualTo("Task 1");
  }

  @Test
  void getTaskById_ShouldThrowException_WhenNotFound() {
    assertThatThrownBy(() -> taskService.getTaskById(999L))
        .isInstanceOf(TaskNotFoundException.class)
        .hasMessageContaining("Task not found with id: 999");
  }

  @Test
  void createTask_ShouldSaveTask() {
    Task newTask = Task.builder()
        .title("New Task")
        .priority(Priority.HIGH)
        .build();

    Task saved = taskService.createTask(newTask);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getTitle()).isEqualTo("New Task");
    assertThat(saved.getStatus()).isEqualTo(TaskStatus.PENDING);
  }

  @Test
  void bulkCompleteTasks_WhenAllIdsExist_ShouldCompleteAllTasks() {
    List<Long> ids = List.of(task1.getId(), task2.getId(), task3.getId());

    taskService.bulkCompleteTasks(ids);

    Task updated1 = taskService.getTaskById(task1.getId());
    Task updated2 = taskService.getTaskById(task2.getId());
    Task updated3 = taskService.getTaskById(task3.getId());

    assertThat(updated1.isCompleted()).isTrue();
    assertThat(updated2.isCompleted()).isTrue();
    assertThat(updated3.isCompleted()).isTrue();
  }

  @Test
  void bulkCompleteTasks_WhenSomeIdMissing_ShouldRollbackAllUpdates() {
    boolean initialCompleted1 = task1.isCompleted();
    boolean initialCompleted2 = task2.isCompleted();

    List<Long> idsWithMissing = List.of(task1.getId(), task2.getId(), 999L);

    assertThatThrownBy(() -> taskService.bulkCompleteTasks(idsWithMissing))
        .isInstanceOf(BulkOperationException.class)
        .hasMessageContaining("Some tasks were not found");

    Task refreshed1 = taskService.getTaskById(task1.getId());
    Task refreshed2 = taskService.getTaskById(task2.getId());

    assertThat(refreshed1.isCompleted()).isEqualTo(initialCompleted1);
    assertThat(refreshed2.isCompleted()).isEqualTo(initialCompleted2);
  }

  @Test
  void bulkCompleteTasks_WhenEmptyList_ShouldThrowException() {
    assertThatThrownBy(() -> taskService.bulkCompleteTasks(List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Task IDs list cannot be null or empty");
  }

  @Test
  void getTaskWithAttachments_ShouldReturnTaskWithAttachments() {
    Task taskWithAttachments = taskService.getTaskWithAttachments(task1.getId());

    assertThat(taskWithAttachments).isNotNull();
    assertThat(taskWithAttachments.getAttachments()).isNotNull();
  }

  @Test
  void getTasksDueWithinNextSevenDays_ShouldReturnCorrectTasks() {
    List<Task> tasks = taskService.getTasksDueWithinNextSevenDays();

    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(2);
    assertThat(tasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("Task 1", "Task 2");
  }
}