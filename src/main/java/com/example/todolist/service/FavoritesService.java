package com.example.todolist.service;

import com.example.todolist.model.Task;
import com.example.todolist.repository.TaskRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FavoritesService {

    private static final String FAVORITES_SESSION_KEY = "favoriteTaskIds";
    private final TaskRepository taskRepository;

    @SuppressWarnings("unchecked")
    private Set<Long> getFavoritesSet(HttpSession session) {
        Set<Long> favorites = (Set<Long>) session.getAttribute(FAVORITES_SESSION_KEY);
        if (favorites == null) {
            favorites = new HashSet<>();
            session.setAttribute(FAVORITES_SESSION_KEY, favorites);
        }
        return favorites;
    }

    public void addToFavorites(Long taskId, HttpSession session) {
        Set<Long> favorites = getFavoritesSet(session);
        favorites.add(taskId);
        session.setAttribute(FAVORITES_SESSION_KEY, favorites);
    }

    public void removeFromFavorites(Long taskId, HttpSession session) {
        Set<Long> favorites = getFavoritesSet(session);
        favorites.remove(taskId);
        session.setAttribute(FAVORITES_SESSION_KEY, favorites);
    }

    public Set<Long> getFavoriteIds(HttpSession session) {
        return getFavoritesSet(session);
    }

    public List<Task> getFavoriteTasks(HttpSession session) {
        Set<Long> favoriteIds = getFavoritesSet(session);
        if (favoriteIds.isEmpty()) {
            return Collections.emptyList();
        }
        return taskRepository.findAllById(favoriteIds);
    }
}