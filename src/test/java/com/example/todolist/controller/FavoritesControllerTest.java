package com.example.todolist.controller;

import com.example.todolist.mapper.TaskMapper;
import com.example.todolist.service.FavoritesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoritesController.class)
class FavoritesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoritesService favoritesService;

    @MockBean
    private TaskMapper taskMapper;

    @Test
    void addToFavorites_Ok() throws Exception {
        mockMvc.perform(post("/api/favorites/123"))
                .andExpect(status().isOk());
    }

    @Test
    void removeFromFavorites_NoContent() throws Exception {
        mockMvc.perform(delete("/api/favorites/123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getFavorites_Ok() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk());
    }
}