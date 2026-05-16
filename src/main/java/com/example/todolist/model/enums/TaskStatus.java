package com.example.todolist.model.enums;

public enum TaskStatus {
  PENDING("Ожидает"),
  IN_PROGRESS("В работе"),
  COMPLETED("Завершена"),
  CANCELLED("Отменена");

  private final String description;

  TaskStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}