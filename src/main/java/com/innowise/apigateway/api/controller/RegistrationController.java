package com.innowise.apigateway.api.controller;

import com.innowise.apigateway.api.dto.CreateRegistrationDto;
import com.innowise.apigateway.core.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/gateway")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody CreateRegistrationDto registration) {
        return registrationService.registerUser(registration)
            .then(Mono.just(ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully")))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage())
                ));
    }
}
