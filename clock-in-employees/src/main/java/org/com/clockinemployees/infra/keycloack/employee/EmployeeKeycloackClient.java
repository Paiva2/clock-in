package org.com.clockinemployees.infra.keycloack.employee;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.config.KeyloackBuilderConfig;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.EmailAlreadyUsedException;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
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
    private static final String DEFAULT_USER_ROLE = "user";
    private static final String USER_ATTRIBUTE_ID = "user_application_id";

    private final KeyloackBuilderConfig keyloackBuilderConfig;

    public void registerUser(Employee employee, String rawPassword) {
        RealmResource kcRealmResource = getInstance();
        UsersResource kcUsers = kcRealmResource.users();

        Response response = kcUsers.create(mountUserRepresentation(employee, rawPassword));

        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            throw new EmailAlreadyUsedException();
        } else if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new ResponseStatusException(HttpStatus.valueOf(response.getStatus()));
        }

        String userKcId = CreatedResponseUtil.getCreatedId(response);

        RoleRepresentation userRealmRole = kcRealmResource.roles().get(DEFAULT_USER_ROLE).toRepresentation();
        kcUsers.get(userKcId).roles().realmLevel().add(List.of(userRealmRole));

        response.close();
    }

    private RealmResource getInstance() {
        return keyloackBuilderConfig.getInstance().realm(keyloackBuilderConfig.getRealm());
    }

    private UserRepresentation mountUserRepresentation(Employee employee, String rawPassword) {
        Map<String, List<String>> userAttrs = new LinkedHashMap<>();
        userAttrs.put(USER_ATTRIBUTE_ID, List.of(employee.getId().toString()));

        CredentialRepresentation credentialRepresentation = createUserPasswordCredentials(rawPassword);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(employee.getEmail());
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        userRepresentation.setFirstName(employee.getFirstName());
        userRepresentation.setLastName(employee.getLastName());
        userRepresentation.setEmail(employee.getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setAttributes(userAttrs);

        return userRepresentation;
    }

    private CredentialRepresentation createUserPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();

        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);

        return passwordCredentials;
    }
}
