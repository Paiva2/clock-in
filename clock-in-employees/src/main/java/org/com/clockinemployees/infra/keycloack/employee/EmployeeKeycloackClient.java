package org.com.clockinemployees.infra.keycloack.employee;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.config.KeyloackBuilderConfig;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.EmailAlreadyUsedException;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class EmployeeKeycloackClient {
    private final KeyloackBuilderConfig keyloackBuilderConfig;

    public void registerUser(Employee employee, String rawPassword) {
        UsersResource kcInstance = getInstance();
        Response response = kcInstance.create(mountRepresentation(employee, rawPassword));

        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            throw new EmailAlreadyUsedException();
        } else if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new ResponseStatusException(HttpStatus.valueOf(response.getStatus()));
        }

        response.close();
    }

    private UsersResource getInstance() {
        return keyloackBuilderConfig.getInstance().realm(keyloackBuilderConfig.getRealm()).users();
    }

    private UserRepresentation mountRepresentation(Employee employee, String rawPassword) {
        Map<String, List<String>> userAttrs = new LinkedHashMap<>();
        userAttrs.put("user_application_id", List.of(employee.getId().toString()));

        CredentialRepresentation credentialRepresentation = createPasswordCredentials(rawPassword);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(employee.getEmail());
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        userRepresentation.setFirstName(employee.getFirstName());
        userRepresentation.setLastName(employee.getLastName());
        userRepresentation.setEmail(employee.getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setAttributes(userAttrs);
        userRepresentation.setRealmRoles(List.of("user"));

        return userRepresentation;
    }

    private CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();

        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);

        return passwordCredentials;
    }
}
