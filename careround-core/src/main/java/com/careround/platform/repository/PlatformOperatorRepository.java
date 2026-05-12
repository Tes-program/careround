package com.careround.platform.repository;

import com.careround.platform.entity.PlatformOperator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformOperatorRepository extends JpaRepository<PlatformOperator, String> {

    Optional<PlatformOperator> findByEmailAndIsActiveTrue(String email);
}
