package com.innowise.apigateway.api.dto;

public record CreateAuthServiceDto(
    String email,
    String password
) {}
