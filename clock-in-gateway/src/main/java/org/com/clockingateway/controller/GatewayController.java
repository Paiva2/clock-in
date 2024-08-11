package org.com.clockingateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GatewayController {
    private final static String RESOURCE_SERVER_LOGIN_URL = "http://localhost:9000/oauth2/authorization/keycloak";

    @GetMapping("/auth/login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(RESOURCE_SERVER_LOGIN_URL))
            .build();
    }
}