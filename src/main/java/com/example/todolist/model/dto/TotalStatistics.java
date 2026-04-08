package com.example.todolist.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalStatistics {
  private long totalTasks;
  private long completedTasks;
  private long pendingTasks;
  private long inProgressTasks;
  private double averageCompletionSeconds;

  public String getFormattedAverageCompletionTime() {
    if (averageCompletionSeconds <= 0) {
      return "N/A";
    }
    long hours = (long) (averageCompletionSeconds / 3600);
    long minutes = (long) ((averageCompletionSeconds % 3600) / 60);
    long seconds = (long) (averageCompletionSeconds % 60);

    if (hours > 0) {
      return String.format("%dч %dм %dс", hours, minutes, seconds);
    } else if (minutes > 0) {
      return String.format("%dм %dс", minutes, seconds);
    } else {
      return String.format("%dс", seconds);
    }
  }
}