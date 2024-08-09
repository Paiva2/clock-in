package org.com.clockinemployees.config;

import org.com.clockinemployees.config.utils.KeycloakJwtRolesConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Value("${keycloack.issuer-uri}")
    private String TOKEN_ISSUER_URI;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        DelegatingJwtGrantedAuthoritiesConverter authoritiesConverter = new DelegatingJwtGrantedAuthoritiesConverter(
            new JwtGrantedAuthoritiesConverter(),
            new KeycloakJwtRolesConverter()
        );

        http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwt ->
            new JwtAuthenticationToken(jwt, authoritiesConverter.convert(jwt))
        );

        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(req ->
            req.requestMatchers("/employee/register", "/employee/auth").permitAll()
                .requestMatchers("/employee/list").hasAnyAuthority("ROLE_realm_user", "ROLE_realm_admin", "ROLE_realm_human_resources")
        );
        
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(TOKEN_ISSUER_URI);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
