package com.example.todolist.mapper;

import com.example.todolist.model.dto.PriorityStatistics;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class PriorityStatisticsRowMapper implements RowMapper<PriorityStatistics> {

  @Override
  public PriorityStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
    long totalCount = rs.getLong("total_count");
    long completedCount = rs.getLong("completed_count");
    double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0.0;

    return PriorityStatistics.builder()
        .priority(rs.getString("priority"))
        .totalCount(totalCount)
        .completedCount(completedCount)
        .pendingCount(rs.getLong("pending_count"))
        .completionRate(Math.round(completionRate * 100.0) / 100.0)
        .build();
  }
}