package com.example.todolist.config;

import com.example.todolist.repository.StubTaskRepository;
import com.example.todolist.repository.TaskRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс приложения
 * Содержит определения бинов, создаваемых вручную через @Bean
 *
 * @author anikanova a.a
 * @version 1.0
 */
@Configuration
public class AppConfig {
    @Bean(name = "stubTaskRepository")
    public TaskRepository stubTaskRepository() {
        return new StubTaskRepository();
    }
}