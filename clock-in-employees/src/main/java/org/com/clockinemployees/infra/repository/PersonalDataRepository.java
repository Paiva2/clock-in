package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalDataRepository extends JpaRepository<PersonalData, Long> {
    Optional<PersonalData> findByEmployeeId(Long employeeId);
}
