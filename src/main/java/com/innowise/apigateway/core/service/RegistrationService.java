package com.innowise.apigateway.core.service;

import com.innowise.apigateway.api.dto.CreateAuthServiceDto;
import com.innowise.apigateway.api.dto.CreateRegistrationDto;
import com.innowise.apigateway.api.dto.CreateUserServiceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RegistrationService {

    private final WebClient authWebClient;
    private final WebClient userWebClient;

    public RegistrationService(WebClient authWebClient, WebClient userWebClient) {
        this.authWebClient = authWebClient;
        this.userWebClient = userWebClient;
    }

    @Value("${internal.api-key}")
    private String internalKey;

    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

    public Mono<Void> registerUser(CreateRegistrationDto request) {
        logger.info("Starting registration for email: {}", request.email());

        CreateAuthServiceDto authRequest = new CreateAuthServiceDto(request.email(), request.password());

        return authWebClient.post()
            .uri("/api/auth/register")
            .bodyValue(authRequest)
            .retrieve()
            .bodyToMono(UUID.class)
            .flatMap(response -> {

                logger.info("Auth created successfully. Sub: {}", response);

                return createProfileInUserService(response, request)
                    .onErrorResume(e -> {
                        logger.error("Profile creation failed. Rolling back Auth. Error: {}", e.getMessage());
                        return rollbackAuthService(response)
                            .then(Mono.error(new RuntimeException("Registration failed: " + e.getMessage())));
                    });
            });
    }

    private Mono<Void> createProfileInUserService(UUID sub, CreateRegistrationDto request) {
        CreateUserServiceDto userRequest = new CreateUserServiceDto(
            sub,
            request.name(),
            request.surname(),
            request.email(),
            request.birthDate()
        );

        return userWebClient.post()
            .uri("/api/users/registration")
            .bodyValue(userRequest)
            .retrieve()
            .bodyToMono(Void.class);
    }

    private Mono<Void> rollbackAuthService(UUID sub) {
        return authWebClient.delete()
            .uri("/api/auth/internal/user/" + sub)
            .header("x-internal-key", internalKey)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> logger.info("Rollback successful for sub: {}", sub))
            .doOnError(e -> logger.error("Rollback FAILED for sub: {}. Manual intervention required!", sub));
    }
}