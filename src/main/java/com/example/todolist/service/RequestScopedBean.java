package com.example.todolist.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Бин с областью видимости HTTP-запроса (request scope).
 * Создается новый экземпляр для каждого HTTP-запроса.
 *
 * @author Student
 * @version 1.0
 */
@Component
@RequestScope
public class RequestScopedBean {

    private static final Logger log = LoggerFactory.getLogger(RequestScopedBean.class);

    private final String requestId;
    private final LocalDateTime startTime;

    public RequestScopedBean() {
        this.requestId = UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
    }

    @PostConstruct
    public void init() {
        log.info("🚀 New request started. ID: {}, Time: {}",
                requestId, startTime.format(DateTimeFormatter.ISO_TIME));
    }

    @PreDestroy
    public void destroy() {
        log.info("🏁 Request finished. ID: {}", requestId);
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}