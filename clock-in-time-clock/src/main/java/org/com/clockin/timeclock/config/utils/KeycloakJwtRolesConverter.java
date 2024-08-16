package org.com.clockin.timeclock.config.utils;


import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class KeycloakJwtRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private String PREFIX_REALM_ROLE = "ROLE_realm_";
    private String CLAIM_REALM_ACCESS = "realm_access";
    private String CLAIM_ROLES = "roles";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        Map<String, Collection<String>> realmAccess = jwt.getClaim(CLAIM_REALM_ACCESS);

        if (Objects.nonNull(realmAccess) && !realmAccess.isEmpty()) {
            Collection<String> roles = realmAccess.get(CLAIM_ROLES);

            if (Objects.isNull(roles) || roles.isEmpty()) return grantedAuthorities;

            grantedAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(PREFIX_REALM_ROLE + role))
                .collect(Collectors.toList());
        }

        return grantedAuthorities;
    }
}