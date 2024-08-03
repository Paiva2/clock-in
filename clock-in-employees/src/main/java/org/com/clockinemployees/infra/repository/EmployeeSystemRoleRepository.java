package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.EmployeeSystemRole;
import org.com.clockinemployees.domain.entity.key.EmployeeSystemRoleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeSystemRoleRepository extends JpaRepository<EmployeeSystemRole, EmployeeSystemRoleKey> {
}
