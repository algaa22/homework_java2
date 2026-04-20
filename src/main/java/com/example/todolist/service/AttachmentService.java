package com.example.todolist.service;

import com.example.todolist.model.Task;
import com.example.todolist.model.dto.AttachmentResponseDto;
import com.example.todolist.exception.AttachmentNotFoundException;
import com.example.todolist.exception.TaskNotFoundException;
import com.example.todolist.model.TaskAttachment;
import com.example.todolist.repository.TaskAttachmentRepository;
import com.example.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final TaskAttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public TaskAttachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found with id: " + attachmentId));
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(Long attachmentId) throws IOException {
        TaskAttachment attachment = getAttachment(attachmentId);
        Path filePath = Paths.get(uploadDir).resolve(attachment.getStoredFileName());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("File not found: " + attachment.getStoredFileName());
        }

        return resource;
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        TaskAttachment attachment = getAttachment(attachmentId);
        try {
            Path filePath = Paths.get(uploadDir).resolve(attachment.getStoredFileName());
            Files.deleteIfExists(filePath);

            attachmentRepository.delete(attachment);
        } catch (IOException e) {
            log.error("Failed to delete file", e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> getAttachmentsByTaskId(Long taskId) {
        return attachmentRepository.findByTaskId(taskId).stream()
                .map(a -> AttachmentResponseDto.builder()
                        .id(a.getId())
                        .fileName(a.getFileName())
                        .size(a.getSize())
                        .uploadedAt(a.getUploadedAt())
                        .build())
                .toList();
    }

    @Transactional
    public AttachmentResponseDto storeAttachment(Long taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedFileName = UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storedFileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            TaskAttachment attachment = new TaskAttachment();
            attachment.setTask(task);
            attachment.setFileName(originalFilename);
            attachment.setStoredFileName(storedFileName);
            attachment.setContentType(file.getContentType());
            attachment.setSize(file.getSize());

            TaskAttachment saved = attachmentRepository.save(attachment);

            return AttachmentResponseDto.builder()
                .id(saved.getId())
                .fileName(saved.getFileName())
                .size(saved.getSize())
                .uploadedAt(saved.getUploadedAt())
                .build();

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }
}