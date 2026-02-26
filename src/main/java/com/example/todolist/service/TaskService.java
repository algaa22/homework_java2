package com.example.todolist.service;

import com.example.todolist.model.Task;
import com.example.todolist.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервисный слой для управления задачами.
 * Содержит бизнес-логику приложения.
 * Демонстрирует жизненный цикл бина через @PostConstruct и @PreDestroy.
 *
 * @author Student
 * @version 1.0
 */
@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final Map<String, Task> taskCache = new ConcurrentHashMap<>();
    private final ObjectFactory<RequestScopedBean> requestScopedBeanFactory;
    private final ObjectFactory<PrototypeScopedBean> prototypeScopedBeanFactory;

    @Value("${app.name}")
    private String appName;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       ObjectFactory<RequestScopedBean> requestScopedBeanFactory,
                       ObjectFactory<PrototypeScopedBean> prototypeScopedBeanFactory) {
        this.taskRepository = taskRepository;
        this.requestScopedBeanFactory = requestScopedBeanFactory;
        this.prototypeScopedBeanFactory = prototypeScopedBeanFactory;
        log.info("TaskService constructor");
    }

    @PostConstruct
    public void init() {
        log.info("@PostConstruct: Initializing cache for {}", appName);
        taskRepository.findAll().stream()
                .limit(2)
                .forEach(task -> taskCache.put(task.getId(), task));
        log.info("Cache initialized with {} tasks", taskCache.size());
    }

    @PreDestroy
    public void destroy() {
        log.info("@PreDestroy: Cleaning up. Cache size: {}", taskCache.size());
        taskCache.clear();
    }

    public List<Task> getAllTasks() {
        RequestScopedBean requestBean = requestScopedBeanFactory.getObject();
        log.info("Processing request ID: {}", requestBean.getRequestId());
        return taskRepository.findAll();
    }

    public Task getTaskById(String id) {
        RequestScopedBean requestBean = requestScopedBeanFactory.getObject();
        log.info("Getting task by ID: {} (request: {})", id, requestBean.getRequestId());

        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    public Task createTask(Task task) {
        if (task.getId() == null || task.getId().isEmpty()) {
            PrototypeScopedBean prototype = prototypeScopedBeanFactory.getObject();
            String newId = prototype.generateTaskId();
            log.info("Generated new task ID using prototype {}: {}",
                    prototype.getInstanceId(), newId);
        }

        Task saved = taskRepository.save(task);
        taskCache.put(saved.getId(), saved);
        return saved;
    }

    public Task updateTask(String id, Task task) {
        Task existing = getTaskById(id);
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setCompleted(task.isCompleted());
        Task updated = taskRepository.save(existing);
        taskCache.put(id, updated);
        return updated;
    }

    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
        taskCache.remove(id);
    }
    public void demonstrateScopes() {
        log.info("========== SCOPE DEMONSTRATION ==========");

        RequestScopedBean req1 = requestScopedBeanFactory.getObject();
        RequestScopedBean req2 = requestScopedBeanFactory.getObject();
        log.info("Request beans - same instance? {}", req1 == req2);
        log.info("  req1 ID: {}", req1.getRequestId());
        log.info("  req2 ID: {}", req2.getRequestId());

        PrototypeScopedBean proto1 = prototypeScopedBeanFactory.getObject();
        PrototypeScopedBean proto2 = prototypeScopedBeanFactory.getObject();
        log.info("Prototype beans - same instance? {}", proto1 == proto2);
        log.info("  proto1 ID: {}", proto1.getInstanceId());
        log.info("  proto2 ID: {}", proto2.getInstanceId());

        log.info("==========================================");
    }
}