package org.com.clockingateway.infra.clients.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmployeeLoginInput {
    @Email
    private String email;

    @Size(min = 6)
    private String password;
}
