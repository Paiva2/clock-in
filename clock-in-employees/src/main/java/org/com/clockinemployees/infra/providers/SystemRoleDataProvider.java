package org.com.clockinemployees.infra.providers;

import lombok.AllArgsConstructor;
import org.com.clockinemployees.domain.entity.SystemRole;
import org.com.clockinemployees.domain.enums.Role;
import org.com.clockinemployees.infra.repository.SystemRoleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@AllArgsConstructor
@Component
public class SystemRoleDataProvider {
    private final SystemRoleRepository systemRoleRepository;

    public Optional<SystemRole> findByRole(Role role) {
        return systemRoleRepository.findByRole(role);
    }
}
