package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.EmployeeManager;
import org.com.clockinemployees.domain.entity.key.EmployeeManagerKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query(value = """
            SELECT epm.* FROM tb_employees_managers epm
            INNER JOIN tb_employees ep ON ep.em_id = epm.em_employee_id
            LEFT JOIN tb_itineraries it ON ep.em_id = it.iti_employee_id
            WHERE epm.em_manager_id = :managerId
            AND (:employeeName IS NULL OR lower(concat(ep.em_first_name, ' ', ep.em_last_name)) LIKE concat('%', trim(lower(:employeeName)), '%'))
            AND ep.em_disabled_at IS NULL
            ORDER BY concat(ep.em_first_name, ' ', ep.em_last_name) ASC
        """, nativeQuery = true)
    Page<EmployeeManager> findAllByManagerId(@Param("managerId") Long managerId, @Param("employeeName") String employeeName, Pageable pageable);

    @Query(value = """
        DELETE FROM tb_employees_managers epm
        WHERE epm.em_manager_id = :managerId
        AND epm.em_employee_id = :employeeId
        """, nativeQuery = true)
    @Modifying
    void deleteEmployeeManager(@Param("employeeId") Long employeeId, @Param("managerId") Long managerId);
}
