package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.EmployeePosition;
import org.com.clockinemployees.domain.entity.key.EmployeePositionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, EmployeePositionKey> {
    @Query("SELECT ep FROM EmployeePosition ep " +
        "JOIN FETCH ep.position epp " +
        "WHERE ep.employee.id = :employeeId " +
        "AND epp.name = 'HUMAN_RESOURCES'")
    Optional<EmployeePosition> findHrByEmployeeId(@Param("employeeId") Long employeeId);
}
