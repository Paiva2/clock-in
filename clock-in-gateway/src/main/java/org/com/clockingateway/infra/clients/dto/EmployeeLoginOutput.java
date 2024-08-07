package org.com.clockingateway.infra.clients.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
public class EmployeeLoginOutput {
    private Long id;
    private String email;
    private KeycloackTokenOutput token;
}
