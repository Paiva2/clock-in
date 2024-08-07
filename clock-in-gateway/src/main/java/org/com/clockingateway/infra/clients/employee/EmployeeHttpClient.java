package org.com.clockingateway.infra.clients.employee;

import org.com.clockingateway.infra.clients.dto.EmployeeLoginInput;
import org.com.clockingateway.infra.clients.dto.EmployeeLoginOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmployeeHttpClient {
    @Value("${gateway.url}")
    private String EMPLOYEE_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<EmployeeLoginOutput> login(EmployeeLoginInput loginInput) {
        return restTemplate.exchange(
            EMPLOYEE_URL.concat("/employee/auth"),
            HttpMethod.POST, new HttpEntity<>(loginInput),
            EmployeeLoginOutput.class
        );
    }
}
