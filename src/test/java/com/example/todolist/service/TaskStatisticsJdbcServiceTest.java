package com.example.todolist.service;

import com.example.todolist.model.dto.PriorityStatistics;
import com.example.todolist.model.dto.TotalStatistics;
import com.example.todolist.model.Task;
import com.example.todolist.model.enums.Priority;
import com.example.todolist.model.enums.TaskStatus;
import com.example.todolist.repository.TaskRepository;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskStatisticsJdbcServiceTest {

  @Autowired
  private TaskStatisticsJdbcService statisticsService;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;


  @BeforeEach
  void setUp() throws SQLException {
    jdbcTemplate.execute("DELETE FROM tasks");
    taskRepository.deleteAll();
    taskRepository.flush();

    createAndSaveTask("High Task 1", Priority.HIGH, TaskStatus.COMPLETED, true);
    createAndSaveTask("High Task 2", Priority.HIGH, TaskStatus.PENDING, false);
    createAndSaveTask("Medium Task", Priority.MEDIUM, TaskStatus.PENDING, false);
    createAndSaveTask("Low Task", Priority.LOW, TaskStatus.COMPLETED, true);
  }

  private void createAndSaveTask(String title, Priority priority, TaskStatus status, boolean completed) {
    Task task = Task.builder().title(title).priority(priority).status(status).build();
    task.setCompleted(completed);
    taskRepository.saveAndFlush(task);
  }

  @Test
  void getTasksCountByPriority_ShouldReturnCorrectStatistics() {
    List<PriorityStatistics> stats = statisticsService.getTasksCountByPriority();

    assertThat(stats).isNotEmpty();

    assertPriorityStats(stats, "HIGH", 2, 1, 1);
    assertPriorityStats(stats, "MEDIUM", 1, 0, 1);
    assertPriorityStats(stats, "LOW", 1, 1, 0);
  }

  private void assertPriorityStats(List<PriorityStatistics> stats, String priority,
      long total, long completed, long pending) {
    var stat = stats.stream().filter(s -> priority.equals(s.getPriority())).findFirst().orElse(null);
    assertThat(stat).isNotNull();
    assertThat(stat.getTotalCount()).isEqualTo(total);
    assertThat(stat.getCompletedCount()).isEqualTo(completed);
    assertThat(stat.getPendingCount()).isEqualTo(pending);
  }

  @Test
  void getTotalStatistics_ShouldReturnCorrectTotals() {
    TotalStatistics stats = statisticsService.getTotalStatistics();

    assertThat(stats).isNotNull();
    assertThat(stats.getTotalTasks()).isEqualTo(4);
    assertThat(stats.getCompletedTasks()).isEqualTo(2);
    assertThat(stats.getPendingTasks()).isEqualTo(2);
  }
}