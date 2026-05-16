package com.example.todolist.service;

import com.example.todolist.model.Task;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.enums.TaskStatus;
import com.example.todolist.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Обновление статуса существующей задачи - проверка взаимодействия")
    void givenExistingTask_whenUpdateTask_thenRepositorySaveIsCalledWithUpdatedStatus() {
        Long taskId = 1L;

        Task existingTask = Task.builder()
                .id(taskId)
                .title("Test Task")
                .status(TaskStatus.PENDING)
                .priority(Priority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .build();

        Task updateDetails = new Task();
        updateDetails.setCompleted(true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.updateTask(taskId, updateDetails);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    @DisplayName("Обновление несуществующей задачи")
    void givenNonExistingTask_whenUpdateTask_thenThrowsException() {
        Long taskId = 999L;
        Task updateDetails = new Task();
        updateDetails.setCompleted(true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                com.example.todolist.exception.TaskNotFoundException.class,
                () -> taskService.updateTask(taskId, updateDetails)
        );

        verify(taskRepository, never()).save(any());
    }
}