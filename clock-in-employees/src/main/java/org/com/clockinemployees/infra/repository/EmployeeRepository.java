package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByFirstNameAndLastName(String firstName, String lastName);

    @Query(value = """
        SELECT * FROM tb_employees em
        JOIN tb_employee_positions ep ON ep.ep_employee_id = em.em_id
        JOIN tb_positions po ON po.ps_id = ep.ep_position_id
        WHERE (:employeeName IS NULL OR lower(concat(em.em_first_name, ' ', em.em_last_name)) LIKE concat('%', lower(:employeeName), '%'))
        AND (:email IS NULL OR lower(em.em_email) = lower(:email))
        AND (:enterprisePosition IS NULL OR po.ps_name = :enterprisePosition)
        AND em.em_deleted_at IS NULL
        """, nativeQuery = true)
    Page<Employee> findAllPaginated(@Param("employeeName") String employeeName, @Param("email") String email, @Param("enterprisePosition") String enterprisePosition, Pageable pageable);
}
