package org.com.clockingateway.infra.clients.keycloack;

import org.com.clockingateway.infra.clients.dto.EmployeeLoginInput;
import org.com.clockingateway.infra.clients.dto.KeycloackTokenOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class KeycloackHttpClient {
    @Value("${keycloak.realm.token-url}")
    private String KEYCLOACK_TOKEN_REALM_URL;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String KEYCLOACK_CLIENT_ID;

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<KeycloackTokenOutput> login(EmployeeLoginInput loginInput) {
        MultiValueMap<String, String> formValue = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        formValue.add("client_id", KEYCLOACK_CLIENT_ID);
        formValue.add("username", loginInput.getEmail());
        formValue.add("password", loginInput.getPassword());
        formValue.add("grant_type", "password");

        return restTemplate.exchange(
            KEYCLOACK_TOKEN_REALM_URL,
            HttpMethod.POST,
            new HttpEntity<>(formValue, headers),
            KeycloackTokenOutput.class
        );
    }
}
