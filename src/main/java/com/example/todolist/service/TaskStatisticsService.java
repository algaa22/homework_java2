package com.example.todolist.service;

import com.example.todolist.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Сервис для демонстрации работы с несколькими бинами одного типа.
 * Показывает использование @Primary и @Qualifier.
 *
 * @author Student
 * @version 1.0
 */
@Service
public class TaskStatisticsService {

    private final TaskRepository primaryRepository;
    private final TaskRepository stubRepository;

    @Autowired
    public TaskStatisticsService(TaskRepository primaryRepository,
                                 @Qualifier("stubTaskRepository") TaskRepository stubRepository) {
        this.primaryRepository = primaryRepository;
        this.stubRepository = stubRepository;
    }

    public void printStats() {
        System.out.println("Primary repo size: " + primaryRepository.findAll().size());
        System.out.println("Stub repo size: " + stubRepository.findAll().size());
    }
}