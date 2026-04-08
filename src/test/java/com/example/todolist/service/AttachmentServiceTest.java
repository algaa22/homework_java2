package com.example.todolist.service;

import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskAttachment;
import com.example.todolist.model.dto.AttachmentResponseDto;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.enums.TaskStatus;
import com.example.todolist.repository.TaskAttachmentRepository;
import com.example.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttachmentServiceTest {

  @Autowired
  private AttachmentService attachmentService;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private TaskAttachmentRepository attachmentRepository;

  private Task testTask;

  @BeforeEach
  void setUp() {
    attachmentRepository.deleteAll();
    taskRepository.deleteAll();

    testTask = Task.builder()
        .title("Test Task for Attachments")
        .priority(Priority.HIGH)
        .status(TaskStatus.PENDING)
        .build();

    taskRepository.save(testTask);
  }

  @Test
  void storeAttachment_ShouldSaveFileAndMetadata() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "test.txt",
        "text/plain",
        "Hello World".getBytes()
    );

    AttachmentResponseDto response = attachmentService.storeAttachment(testTask.getId(), file);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
    assertThat(response.getFileName()).isEqualTo("test.txt");
    assertThat(response.getSize()).isEqualTo(file.getSize());

    List<TaskAttachment> attachments = attachmentRepository.findByTaskId(testTask.getId());
    assertThat(attachments).hasSize(1);
    assertThat(attachments.get(0).getFileName()).isEqualTo("test.txt");
  }

  @Test
  void storeAttachment_WhenTaskNotFound_ShouldThrowException() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "example.txt",
        "text/plain",
        "Hello World".getBytes()
    );

    assertThatThrownBy(() -> attachmentService.storeAttachment(999L, file))
        .isInstanceOf(TaskNotFoundException.class);
  }

  @Test
  void getAttachmentsByTaskId_ShouldReturnAllAttachments() throws Exception {
    MockMultipartFile file1 = new MockMultipartFile("file", "file1.txt", "text/plain", "Content1".getBytes());
    MockMultipartFile file2 = new MockMultipartFile("file", "file2.txt", "text/plain", "Content2".getBytes());

    attachmentService.storeAttachment(testTask.getId(), file1);
    attachmentService.storeAttachment(testTask.getId(), file2);

    List<AttachmentResponseDto> attachments = attachmentService.getAttachmentsByTaskId(testTask.getId());

    assertThat(attachments).hasSize(2);
    assertThat(attachments).extracting(AttachmentResponseDto::getFileName)
        .containsExactlyInAnyOrder("file1.txt", "file2.txt");
  }
}