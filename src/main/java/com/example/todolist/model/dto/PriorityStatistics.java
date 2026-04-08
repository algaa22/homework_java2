package com.example.todolist.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityStatistics {
  private String priority;
  private long totalCount;
  private long completedCount;
  private long pendingCount;
  private double completionRate;
}