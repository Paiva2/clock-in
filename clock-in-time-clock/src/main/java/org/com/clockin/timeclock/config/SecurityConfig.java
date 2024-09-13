package org.com.clockin.timeclock.config;

import jakarta.servlet.DispatcherType;
import org.com.clockin.timeclock.config.utils.KeycloakJwtRolesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    @Value("${keycloak.issuer-uri}")
    private String TOKEN_ISSUER_URI;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        DelegatingJwtGrantedAuthoritiesConverter authoritiesConverter = new DelegatingJwtGrantedAuthoritiesConverter(
            new JwtGrantedAuthoritiesConverter(),
            new KeycloakJwtRolesConverter()
        );

        http.csrf(AbstractHttpConfigurer::disable);

        http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwt ->
            new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt))
        );

        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(req ->
            req.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/time-clock/approve/pending/*").hasAnyAuthority("ROLE_realm_ceo", "ROLE_realm_human_resources", "ROLE_realm_manager")
                .requestMatchers("/time-clock/deny/pending/*").hasAnyAuthority("ROLE_realm_ceo", "ROLE_realm_human_resources", "ROLE_realm_manager")
                .requestMatchers("/time-clock/list/pending/*").hasAnyAuthority("ROLE_realm_ceo", "ROLE_realm_human_resources", "ROLE_realm_manager")
                .requestMatchers("/time-clock/employee/{employeeId}/extra-hours").hasAnyAuthority("ROLE_realm_ceo", "ROLE_realm_human_resources", "ROLE_realm_manager")
                .requestMatchers("/time-clock/employee/{employeeId}/list").hasAnyAuthority("ROLE_realm_ceo", "ROLE_realm_human_resources", "ROLE_realm_manager")
                .anyRequest().authenticated()
        );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(TOKEN_ISSUER_URI);
    }
}