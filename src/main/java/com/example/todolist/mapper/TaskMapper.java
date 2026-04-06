package com.example.todolist.mapper;

import com.example.todolist.model.dto.*;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskAttachment;
import com.example.todolist.model.enums.TaskStatus;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = {TaskStatus.class})
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", expression = "java(TaskStatus.PENDING)")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "version", ignore = true)
    Task toEntity(TaskCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

    @Mapping(target = "completed", source = "status", qualifiedByName = "statusToCompleted")
    @Mapping(target = "attachmentUrls", source = "attachments", qualifiedByName = "attachmentsToUrls")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    TaskResponseDto toResponseDto(Task task);

    List<TaskResponseDto> toResponseDtoList(List<Task> tasks);

    @Named("statusToCompleted")
    default boolean statusToCompleted(TaskStatus status) {
        return status == TaskStatus.COMPLETED;
    }

    default TaskStatus completedToStatus(boolean completed) {
        return completed ? TaskStatus.COMPLETED : TaskStatus.PENDING;
    }

    @Named("attachmentsToUrls")
    default List<String> attachmentsToUrls(List<TaskAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }
        return attachments.stream()
            .map(attachment -> "/api/attachments/" + attachment.getId())
            .collect(Collectors.toList());
    }
}