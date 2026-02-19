package com.innowise.apigateway.api.dto;

import java.time.LocalDate;

public record CreateRegistrationDto(
    String email,
    String password,

    String name,
    String surname,
    LocalDate birthDate
) {}