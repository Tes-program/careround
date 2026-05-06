package com.careround.common.repository;

import com.careround.common.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, String> {
    Optional<Hospital> findByContactEmail(String contactEmail);
    boolean existsByContactEmail(String contactEmail);
}
