package com.example.todolist.validator;

import com.example.todolist.model.dto.TaskUpdateDto;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DueDateNotBeforeCreationValidator.class)
public @interface DueDateNotBeforeCreation {
    String message() default "Due date cannot be before task creation date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

class DueDateNotBeforeCreationValidator implements ConstraintValidator<DueDateNotBeforeCreation, TaskUpdateDto> {

    @Override
    public boolean isValid(TaskUpdateDto dto, ConstraintValidatorContext context) {
        return true;
    }
}