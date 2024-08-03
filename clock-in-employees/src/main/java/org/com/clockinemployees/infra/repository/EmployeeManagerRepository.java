package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.key.EmployeeManagerKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeManagerRepository extends JpaRepository<EmployeeManager, EmployeeManagerKey> {
}
