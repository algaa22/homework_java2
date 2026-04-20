package com.example.gateway.api;

import com.example.gateway.dto.AuthResponse;
import com.example.gateway.dto.LoginRequest;
import com.example.gateway.security.JwtUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public AuthController(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        var userDetails = userDetailsService.loadUserByUsername(request.username());

        String role = userDetails.getAuthorities().stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .map(a -> a.getAuthority().substring(5))
                .findFirst()
                .orElse("USER");

        String authority = userDetails.getAuthorities().stream()
                .filter(a -> !a.getAuthority().startsWith("ROLE_"))
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse(null);

        String token = jwtUtils.generateToken(request.username(), role, authority);
        return new AuthResponse(token);
    }
}