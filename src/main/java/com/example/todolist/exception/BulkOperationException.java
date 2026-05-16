package com.example.todolist.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BulkOperationException extends RuntimeException {

  private final List<Long> missingIds;

  public BulkOperationException(String message, List<Long> missingIds) {
    super(message);
    this.missingIds = missingIds;
  }

  public BulkOperationException(String message, List<Long> missingIds, Throwable cause) {
    super(message, cause);
    this.missingIds = missingIds;
  }
}