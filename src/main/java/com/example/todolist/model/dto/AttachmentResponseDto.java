package com.example.todolist.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Response DTO for file attachment")
public class AttachmentResponseDto {

    @Schema(description = "Attachment ID", example = "1")
    private Long id;

    @Schema(description = "Original file name", example = "image.jpg")
    private String fileName;

    @Schema(description = "File size in bytes", example = "1024")
    private long size;

    @Schema(description = "Upload timestamp", example = "2026-03-22T09:30:00")
    private LocalDateTime uploadedAt;
}