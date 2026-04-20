package com.example.todolist.repository;

import com.example.todolist.model.Task;
import com.example.todolist.model.TaskAttachment;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TestEntityManager entityManager;

  private Task testTask1;
  private Task testTask2;
  private Task testTask3;

  @BeforeEach
  void setUp() {
    taskRepository.deleteAll();
    entityManager.flush();

    testTask1 = Task.builder()
        .title("High Priority Task")
        .description("Description 1")
        .priority(Priority.HIGH)
        .status(TaskStatus.PENDING)
        .dueDate(LocalDateTime.now().plusDays(3))
        .tags(List.of("high", "important"))
        .build();

    testTask2 = Task.builder()
        .title("Completed Task")
        .description("Description 2")
        .priority(Priority.MEDIUM)
        .status(TaskStatus.COMPLETED)
        .dueDate(LocalDateTime.now().minusDays(5))
        .tags(List.of("done"))
        .build();

    testTask3 = Task.builder()
        .title("High Task")
        .description("Description 3")
        .priority(Priority.LOW)
        .status(TaskStatus.PENDING)
        .dueDate(LocalDateTime.now().plusDays(1))
        .tags(List.of("high", "deadline"))
        .build();

    taskRepository.saveAll(List.of(testTask1, testTask2, testTask3));
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void findByStatus_ShouldReturnTasksWithGivenStatus() {
    List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);

    assertThat(pendingTasks).hasSize(2);
    assertThat(pendingTasks).allMatch(task -> task.getStatus() == TaskStatus.PENDING);
    assertThat(pendingTasks).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("High Priority Task", "High Task");
  }

  @Test
  void findByPriority_ShouldReturnTasksWithGivenPriority() {
    List<Task> highPriorityTasks = taskRepository.findByPriority(Priority.HIGH);

    assertThat(highPriorityTasks).hasSize(1);
    assertThat(highPriorityTasks.get(0).getTitle()).isEqualTo("High Priority Task");
  }

  @Test
  void findByStatusAndPriority_ShouldReturnTasksWithBothCriteria() {
    List<Task> tasks = taskRepository.findByStatusAndPriority(TaskStatus.PENDING, Priority.HIGH);

    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getTitle()).isEqualTo("High Priority Task");
  }

  @Test
  void findByCompletedAndPriority_ShouldWorkWithDefaultMethod() {
    List<Task> completedTasks = taskRepository.findByCompletedAndPriority(true, Priority.MEDIUM);

    assertThat(completedTasks).hasSize(1);
    assertThat(completedTasks.get(0).getTitle()).isEqualTo("Completed Task");
  }

  @Test
  void findByDueDateBeforeAndStatusNot_ShouldReturnOverdueTasks() {
    List<Task> overdueTasks = taskRepository.findByDueDateBeforeAndStatusNot(
        LocalDateTime.now(), TaskStatus.COMPLETED);

    assertThat(overdueTasks).isEmpty();
  }

  @Test
  void countByStatus_ShouldReturnCorrectCount() {
    long pendingCount = taskRepository.countByStatus(TaskStatus.PENDING);
    long completedCount = taskRepository.countByStatus(TaskStatus.COMPLETED);

    assertThat(pendingCount).isEqualTo(2);
    assertThat(completedCount).isEqualTo(1);
  }

  @Test
  void findTasksDueWithinNextSevenDays_ShouldReturnTasks() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime sevenDaysLater = now.plusDays(7);

    List<Task> tasksDueSoon = taskRepository.findTasksDueWithinNextSevenDays(now, sevenDaysLater);

    assertThat(tasksDueSoon).hasSize(2);
    assertThat(tasksDueSoon).extracting(Task::getTitle)
        .containsExactlyInAnyOrder("High Priority Task", "High Task");
  }

  @Test
  void updateStatusByIds_ShouldUpdateMultipleTasks() {
    List<Long> ids = List.of(testTask1.getId(), testTask3.getId());

    int updatedCount = taskRepository.updateStatusByIds(TaskStatus.COMPLETED, ids);

    assertThat(updatedCount).isEqualTo(2);

    Task updatedTask1 = taskRepository.findById(testTask1.getId()).orElseThrow();
    Task updatedTask3 = taskRepository.findById(testTask3.getId()).orElseThrow();

    assertThat(updatedTask1.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    assertThat(updatedTask3.getStatus()).isEqualTo(TaskStatus.COMPLETED);
  }

  @Test
  void findByIdWithAttachments_ShouldLoadAttachmentsInOneQuery() {
    TaskAttachment attachment = TaskAttachment.builder()
        .task(testTask1)
        .fileName("test.txt")
        .storedFileName("uuid-test.txt")
        .contentType("text/plain")
        .size(1024L)
        .build();

    entityManager.persist(attachment);
    entityManager.flush();
    entityManager.clear();

    var taskWithAttachments = taskRepository.findByIdWithAttachments(testTask1.getId());

    assertThat(taskWithAttachments).isPresent();
    assertThat(taskWithAttachments.get().getAttachments()).hasSize(1);
    assertThat(taskWithAttachments.get().getAttachments().get(0).getFileName()).isEqualTo("test.txt");
  }

  @Test
  void findAllWithAttachments_ShouldLoadAllTasksWithAttachments() {
    List<Task> tasks = taskRepository.findAllWithAttachments();

    assertThat(tasks).hasSize(3);
    tasks.forEach(task -> assertThat(task.getAttachments()).isNotNull());
  }

  @Test
  void existsByTitle_ShouldReturnTrueForExistingTitle() {
    boolean exists = taskRepository.existsByTitle("High Priority Task");
    assertThat(exists).isTrue();
  }

  @Test
  void findByTitleContainingIgnoreCase_ShouldReturnMatchingTasks() {
    List<Task> tasks = taskRepository.findByTitleContainingIgnoreCase("priority");

    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getTitle()).isEqualTo("High Priority Task");
  }
}