package org.com.clockinemployees.infra.repository;

import org.com.clockinemployees.domain.entity.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalDataRepository extends JpaRepository<PersonalData, Long> {
}
