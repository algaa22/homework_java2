package com.example.gateway.dto;

public record ApiError(String code, String message, String traceId) {}
