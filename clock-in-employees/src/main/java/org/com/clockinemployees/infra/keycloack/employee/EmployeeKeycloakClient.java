package org.com.clockinemployees.infra.keycloack.employee;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.config.KeyloackBuilderConfig;
import org.com.clockinemployees.domain.entity.Employee;
import org.com.clockinemployees.domain.entity.Position;
import org.com.clockinemployees.domain.enums.EnterprisePosition;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.dto.RegisterEmployeeInput;
import org.com.clockinemployees.domain.usecase.employee.registerEmployeeUsecase.exception.EmailAlreadyUsedException;
import org.com.clockinemployees.infra.keycloack.employee.exception.UserResourceNotFoundException;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.core.Response;
import java.util.*;

@Component
@AllArgsConstructor
public class EmployeeKeycloakClient {
    private static final Map<EnterprisePosition, KeycloakRelatedRoles> rolesMapper;

    private final KeyloackBuilderConfig keyloackBuilderConfig;

    static {
        rolesMapper = new HashMap<>() {{
            put(EnterprisePosition.EMPLOYEE, KeycloakRelatedRoles.USER);
            put(EnterprisePosition.HUMAN_RESOURCES, KeycloakRelatedRoles.HUMAN_RESOURCES);
            put(EnterprisePosition.MANAGER, KeycloakRelatedRoles.MANAGER);
            put(EnterprisePosition.CEO, KeycloakRelatedRoles.ADMIN);
        }};
    }

    private RealmResource getInstance() {
        return keyloackBuilderConfig.getInstance().realm(keyloackBuilderConfig.getRealm());
    }

    public String registerUser(RegisterEmployeeInput employeeInput, String rawPassword) {
        RealmResource kcRealmResource = getInstance();
        UsersResource kcUsers = kcRealmResource.users();

        Response response = kcUsers.create(mountUserRepresentation(employeeInput, rawPassword));

        if (response.getStatus() == HttpStatus.CONFLICT.value()) {
            throw new EmailAlreadyUsedException();
        } else if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new ResponseStatusException(HttpStatus.valueOf(response.getStatus()));
        }

        String userKcId = CreatedResponseUtil.getCreatedId(response);

        String userRole = rolesMapper.get(EnterprisePosition.EMPLOYEE).roleName();

        RoleRepresentation userRealmRole = kcRealmResource.roles().get(userRole).toRepresentation();
        kcUsers.get(userKcId).roles().realmLevel().add(List.of(userRealmRole));

        response.close();

        return userKcId;
    }

    public void updateUserRoles(Employee employee, Position position, Position oldPosition) {
        RealmResource kcRealmResource = getInstance();
        UsersResource kcUsers = kcRealmResource.users();

        UserResource user = kcUsers.get(employee.getKeycloakId());

        if (Objects.isNull(user)) {
            throw new UserResourceNotFoundException(employee.getKeycloakId());
        }

        String oldUserRole = rolesMapper.get(oldPosition.getName()).roleName();
        String userRole = rolesMapper.get(position.getName()).roleName();

        RoleRepresentation userRealmRole = kcRealmResource.roles().get(userRole).toRepresentation();
        user.roles().realmLevel().add(List.of(userRealmRole));

        RoleRepresentation userRealmRoleToRemove = kcRealmResource.roles().get(oldUserRole).toRepresentation();
        user.roles().realmLevel().remove(List.of(userRealmRoleToRemove));
    }

    private UserRepresentation mountUserRepresentation(RegisterEmployeeInput employeeInput, String rawPassword) {
        Map<String, List<String>> userAttrs = new LinkedHashMap<>();

        CredentialRepresentation credentialRepresentation = createUserPasswordCredentials(rawPassword);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(employeeInput.getEmail());
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        userRepresentation.setFirstName(employeeInput.getFirstName());
        userRepresentation.setLastName(employeeInput.getLastName());
        userRepresentation.setEmail(employeeInput.getEmail());
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

    public enum KeycloakRelatedRoles {
        USER("USER"),
        HUMAN_RESOURCES("HUMAN_RESOURCES"),
        MANAGER("MANAGER"),
        ADMIN("ADMIN");

        private final String role;

        KeycloakRelatedRoles(String role) {
            this.role = role;
        }

        public String roleName() {
            return this.role.toLowerCase(Locale.ROOT);
        }
    }
}
