package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.SystemRole;
import org.com.clockinemployees.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {
    Optional<SystemRole> findByRole(Role role);
}
