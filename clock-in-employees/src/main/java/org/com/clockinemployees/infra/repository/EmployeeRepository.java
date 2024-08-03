package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByFirstNameAndLastName(String firstName, String lastName);
}
