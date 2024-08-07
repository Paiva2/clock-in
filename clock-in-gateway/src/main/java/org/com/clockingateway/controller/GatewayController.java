package org.com.clockingateway.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.clockingateway.infra.clients.dto.EmployeeLoginInput;
import org.com.clockingateway.infra.clients.dto.EmployeeLoginOutput;
import org.com.clockingateway.infra.clients.dto.KeycloackTokenOutput;
import org.com.clockingateway.infra.clients.employee.EmployeeHttpClient;
import org.com.clockingateway.infra.clients.keycloack.KeycloackHttpClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GatewayController {
    private final KeycloackHttpClient keycloackHttpClient;
    private final EmployeeHttpClient employeeHttpClient;

    @PostMapping("/login")
    public ResponseEntity<?> login(
        @RequestBody @Valid EmployeeLoginInput input
    ) {
        try {
            ResponseEntity<EmployeeLoginOutput> employeeLoginOutput = employeeHttpClient.login(input);

            if (employeeLoginOutput.getStatusCode() == HttpStatus.OK && Objects.nonNull(employeeLoginOutput.getBody())) {
                ResponseEntity<KeycloackTokenOutput> keycloackResponse = keycloackHttpClient.getToken(input);

                if (keycloackResponse.getStatusCode() != HttpStatus.OK) {
                    return new ResponseEntity<>(keycloackResponse.getStatusCode());
                }

                employeeLoginOutput.getBody().setToken(keycloackResponse.getBody());

                return new ResponseEntity<>(employeeLoginOutput.getBody(), HttpStatus.OK);
            } else {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
            }
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getStatusCode());
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>("Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}