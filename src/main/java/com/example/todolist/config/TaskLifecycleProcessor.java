package com.example.todolist.config;

import com.example.todolist.repository.TaskRepository;
import com.example.todolist.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Процессор жизненного цикла бинов
 * Логирует этапы создания и инициализации бинов TaskService и TaskRepository
 *
 * @author anikanova a.a
 * @version 1.0
 */
@Component
public class TaskLifecycleProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(TaskLifecycleProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TaskService || bean instanceof TaskRepository) {
            log.info("Before init: {}", beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TaskService || bean instanceof TaskRepository) {
            log.info("After init: {}", beanName);
        }
        return bean;
    }
}