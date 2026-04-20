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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskAttachmentRepositoryTest {

  @Autowired
  private TaskAttachmentRepository attachmentRepository;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TestEntityManager entityManager;

  private Task testTask;
  private TaskAttachment attachment1;
  private TaskAttachment attachment2;

  @BeforeEach
  void setUp() {
    testTask = Task.builder()
        .title("Task with Attachments")
        .priority(Priority.HIGH)
        .status(TaskStatus.PENDING)
        .build();

    taskRepository.save(testTask);
    entityManager.flush();

    attachment1 = TaskAttachment.builder()
        .task(testTask)
        .fileName("file1.pdf")
        .storedFileName("uuid-file1.pdf")
        .contentType("application/pdf")
        .size(102400L)
        .build();

    attachment2 = TaskAttachment.builder()
        .task(testTask)
        .fileName("file2.jpg")
        .storedFileName("uuid-file2.jpg")
        .contentType("image/jpeg")
        .size(204800L)
        .build();

    attachmentRepository.saveAll(List.of(attachment1, attachment2));
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void findByTaskId_ShouldReturnAllAttachmentsForTask() {
    List<TaskAttachment> attachments = attachmentRepository.findByTaskId(testTask.getId());

    assertThat(attachments).hasSize(2);
    assertThat(attachments).extracting(TaskAttachment::getFileName)
        .containsExactlyInAnyOrder("file1.pdf", "file2.jpg");
  }

  @Test
  void findByStoredFileName_ShouldReturnAttachment() {
    Optional<TaskAttachment> found = attachmentRepository.findByStoredFileName(attachment1.getStoredFileName());

    assertThat(found).isPresent();
    assertThat(found.get().getFileName()).isEqualTo("file1.pdf");
  }

  @Test
  void existsByStoredFileName_ShouldReturnTrueForExisting() {
    boolean exists = attachmentRepository.existsByStoredFileName(attachment1.getStoredFileName());
    assertThat(exists).isTrue();
  }

  @Test
  void countByTaskId_ShouldReturnCorrectCount() {
    long count = attachmentRepository.countByTaskId(testTask.getId());
    assertThat(count).isEqualTo(2);
  }

  @Test
  void deleteByTaskId_ShouldRemoveAllAttachments() {
    attachmentRepository.deleteByTaskId(testTask.getId());
    entityManager.flush();

    List<TaskAttachment> remaining = attachmentRepository.findByTaskId(testTask.getId());
    assertThat(remaining).isEmpty();
  }

  @Test
  void getTotalSizeByTaskId_ShouldReturnSumOfAllSizes() {
    long totalSize = attachmentRepository.getTotalSizeByTaskId(testTask.getId());
    assertThat(totalSize).isEqualTo(102400L + 204800L);
  }
}