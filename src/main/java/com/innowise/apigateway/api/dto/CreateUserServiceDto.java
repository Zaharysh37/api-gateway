package com.innowise.apigateway.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateUserServiceDto(
    UUID sub,
    String name,
    String surname,
    String email,
    LocalDate birthDate
) {}
