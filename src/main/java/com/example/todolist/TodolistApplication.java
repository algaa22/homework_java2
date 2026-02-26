package com.example.todolist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Главный класс приложения To-Do List
 * Запускает Spring Boot приложение и включает поддержку AOP
 *
 * @author anikanova a.a
 * @version 1.0
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class TodolistApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodolistApplication.class, args);
    }
}