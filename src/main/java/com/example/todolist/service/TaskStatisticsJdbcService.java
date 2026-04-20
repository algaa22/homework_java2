package com.example.todolist.service;

import com.example.todolist.model.dto.PriorityStatistics;
import com.example.todolist.model.dto.TotalStatistics;
import com.example.todolist.mapper.PriorityStatisticsRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskStatisticsJdbcService {

  private final JdbcTemplate jdbcTemplate;
  private final PriorityStatisticsRowMapper priorityStatisticsRowMapper;

  public List<PriorityStatistics> getTasksCountByPriority() {
    String sql = """
            SELECT
                priority,
                COUNT(*) as total_count,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_count,
                COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count
            FROM tasks
            GROUP BY priority
            ORDER BY
                CASE priority
                    WHEN 'HIGH' THEN 1
                    WHEN 'MEDIUM' THEN 2
                    WHEN 'LOW' THEN 3
                    ELSE 5
                END
            """;

    log.debug("Executing statistics query: {}", sql);

    return jdbcTemplate.query(sql, priorityStatisticsRowMapper);
  }

  public List<PriorityStatistics> getTasksCountByPriorityWithAnonymousRowMapper() {
    String sql = """
            SELECT
                priority,
                COUNT(*) as total_count,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_count,
                COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count
            FROM tasks
            GROUP BY priority
            """;

    RowMapper<PriorityStatistics> rowMapper = new RowMapper<PriorityStatistics>() {
      @Override
      public PriorityStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
        long totalCount = rs.getLong("total_count");
        long completedCount = rs.getLong("completed_count");

        return PriorityStatistics.builder()
            .priority(rs.getString("priority"))
            .totalCount(totalCount)
            .completedCount(completedCount)
            .pendingCount(rs.getLong("pending_count"))
            .completionRate(totalCount > 0 ? (double) completedCount / totalCount * 100 : 0.0)
            .build();
      }
    };

    return jdbcTemplate.query(sql, rowMapper);
  }

  public List<PriorityStatistics> getTasksCountByPriorityWithLambda() {
    String sql = """
            SELECT
                priority,
                COUNT(*) as total_count,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_count,
                COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count
            FROM tasks
            GROUP BY priority
            """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      long totalCount = rs.getLong("total_count");
      long completedCount = rs.getLong("completed_count");

      return PriorityStatistics.builder()
          .priority(rs.getString("priority"))
          .totalCount(totalCount)
          .completedCount(completedCount)
          .pendingCount(rs.getLong("pending_count"))
          .completionRate(totalCount > 0 ? (double) completedCount / totalCount * 100 : 0.0)
          .build();
    });
  }

  public TotalStatistics getTotalStatistics() {
    String sql = """
            SELECT
                COUNT(*) as total_tasks,
                COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_tasks,
                COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_tasks,
                COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
                AVG(EXTRACT(EPOCH FROM (updated_at - created_at))) as avg_completion_seconds
            FROM tasks
            """;

    return jdbcTemplate.queryForObject(sql, new TotalStatisticsRowMapper());
  }

  private static class TotalStatisticsRowMapper implements RowMapper<TotalStatistics> {

    @Override
    public TotalStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
      return TotalStatistics.builder()
          .totalTasks(rs.getLong("total_tasks"))
          .completedTasks(rs.getLong("completed_tasks"))
          .pendingTasks(rs.getLong("pending_tasks"))
          .inProgressTasks(rs.getLong("in_progress_tasks"))
          .averageCompletionSeconds(rs.getDouble("avg_completion_seconds"))
          .build();
    }
  }
}