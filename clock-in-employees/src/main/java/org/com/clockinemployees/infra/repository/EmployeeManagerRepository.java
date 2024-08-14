package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.key.EmployeeManagerKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeManagerRepository extends JpaRepository<EmployeeManager, EmployeeManagerKey> {

    @Query(value = """ 
        SELECT epm.* FROM tb_employees_managers epm
        JOIN tb_employees ep ON ep.em_id = epm.em_employee_id
        JOIN tb_employees em ON em.em_id = epm.em_manager_id
        WHERE em.em_id = :managerId
        AND ep.em_id = :employeeId
        """, nativeQuery = true)
    Optional<EmployeeManager> findByManagerIdAndEmployeeId(@Param("managerId") Long managerId, @Param("employeeId") Long employeeId);

    List<EmployeeManager> findAllByEmployeeId(Long employeeId);
}
