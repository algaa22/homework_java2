package com.example.todolist.controller;

import com.example.todolist.model.dto.TaskResponseDto;
import com.example.todolist.mapper.TaskMapper;
import com.example.todolist.service.FavoritesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "API for managing favorite tasks (session-based)")
public class FavoritesController {

    private final FavoritesService favoritesService;
    private final TaskMapper taskMapper;

    @Operation(summary = "Add to favorites", description = "Adds a task to user's favorites (stored in session)")
    @ApiResponse(responseCode = "200", description = "Task added to favorites")
    @PostMapping("/{taskId}")
    public ResponseEntity<Void> addToFavorites(@PathVariable Long taskId, HttpSession session) {
        favoritesService.addToFavorites(taskId, session);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove from favorites", description = "Removes a task from user's favorites")
    @ApiResponse(responseCode = "204", description = "Task removed from favorites")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long taskId, HttpSession session) {
        favoritesService.removeFromFavorites(taskId, session);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get favorites", description = "Returns all favorite tasks for the current session")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved favorites")
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
        List<TaskResponseDto> favorites = favoritesService.getFavoriteTasks(session).stream()
                .map(taskMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(favorites);
    }
}