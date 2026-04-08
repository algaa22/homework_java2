package com.example.todolist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Preferences", description = "API for user preferences")
public class PreferencesController {

    private static final String VIEW_PREFERENCE_COOKIE_NAME = "viewPreference";
    private static final String DEFAULT_VIEW_MODE = "detailed";

    @Operation(summary = "Get view preference")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved preference")
    @GetMapping("/view")
    public ResponseEntity<String> getViewPreference(@CookieValue(value = VIEW_PREFERENCE_COOKIE_NAME, defaultValue = DEFAULT_VIEW_MODE) String mode) {
        return ResponseEntity.ok(mode);
    }

    @Operation(summary = "Set view preference")
    @ApiResponse(responseCode = "200", description = "Preference saved successfully")
    @PostMapping("/view")
    public ResponseEntity<Void> setViewPreference(
            @RequestParam String mode,
            HttpServletResponse response) {

        Cookie cookie = new Cookie(VIEW_PREFERENCE_COOKIE_NAME, mode);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }
}