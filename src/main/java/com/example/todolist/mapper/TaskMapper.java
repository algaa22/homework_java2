package com.example.todolist.mapper;

import com.example.todolist.model.dto.*;
import com.example.todolist.model.Task;
import com.example.todolist.model.enums.TaskStatus;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = {TaskStatus.class, LocalDateTime.class, LocalDate.class})
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", expression = "java(TaskStatus.PENDING)")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "dueDate", expression = "java(dto.getDueDate() != null ? dto.getDueDate().atStartOfDay() : null)")
    Task toEntity(TaskCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "dueDate", expression = "java(dto.getDueDate() != null ? dto.getDueDate().atStartOfDay() : null)")
    void updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

    @Mapping(target = "completed", source = "status", qualifiedByName = "statusToCompleted")
    @Mapping(target = "dueDate", expression = "java(task.getDueDate() != null ? task.getDueDate().toLocalDate() : null)")
    TaskResponseDto toResponseDto(Task task);

    List<TaskResponseDto> toResponseDtoList(List<Task> tasks);

    @Named("statusToCompleted")
    default boolean statusToCompleted(TaskStatus status) {
        return status == TaskStatus.COMPLETED;
    }
}