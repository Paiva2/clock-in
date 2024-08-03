package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.key.EmployeePositionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, EmployeePositionKey> {
}
