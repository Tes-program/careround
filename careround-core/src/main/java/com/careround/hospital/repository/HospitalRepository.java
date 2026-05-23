package com.careround.hospital.repository;

import com.careround.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, String> {

    boolean existsByContactEmail(String contactEmail);

    boolean existsByCode(String code);

    java.util.Optional<Hospital> findByCodeIgnoreCaseAndIsActiveTrue(String code);
}
