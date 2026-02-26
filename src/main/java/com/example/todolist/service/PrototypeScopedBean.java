package com.example.todolist.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.UUID;

/**
 * Бин с областью видимости prototype.
 * Создается новый экземпляр при каждом обращении.
 * Служит генератором уникальных идентификаторов для задач.
 *
 * @author Student
 * @version 1.0
 */
@Component
@Scope("prototype")
public class PrototypeScopedBean {

    private static final Logger log = LoggerFactory.getLogger(PrototypeScopedBean.class);

    private final String instanceId;
    private int counter;

    public PrototypeScopedBean() {
        this.instanceId = UUID.randomUUID().toString();
        this.counter = 0;
    }

    @PostConstruct
    public void init() {
        log.info("🔷 New prototype instance created. ID: {}", instanceId);
    }

    @PreDestroy
    public void destroy() {
        log.info("🔶 Prototype instance destroyed. ID: {}, Generated: {}",
                instanceId, counter);
    }

    public String generateTaskId() {
        counter++;
        return UUID.randomUUID().toString();
    }

    public String getInstanceId() {
        return instanceId;
    }
}