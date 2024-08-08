package org.com.clockinemployees.config;

import lombok.Getter;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyloackBuilderConfig {
    private Keycloak keycloak = null;

    @Value("${keycloack.server-url}")
    private String serverUrl;

    @Getter
    @Value("${keycloack.realm.name}")
    private String realm;

    private String clientId = "admin-cli";

    @Value("${keycloack.client_secret}")
    private String clientSecret;

    @Value("${keycloack.username_admin}")
    private String userName;

    @Value("${keycloack.password_admin}")
    private String password;

    public Keycloak getInstance() {
        if (keycloak != null) return keycloak;

        return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .grantType(OAuth2Constants.PASSWORD)
            .username(userName)
            .password(password)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
            .build();
    }

}
