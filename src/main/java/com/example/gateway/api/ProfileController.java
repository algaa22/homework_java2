package com.example.gateway.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProfileController {

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public Map<String, String> profile(Principal principal) {
        return Map.of("username", principal.getName(), "message", "This is your profile");
    }

    @GetMapping("/docs")
    @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
    public Map<String, String> docs() {
        return Map.of("message", "Secret documentation", "content", "This is protected content");
    }
}