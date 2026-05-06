package com.careround.common.repository;

import com.careround.common.entity.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {
    Optional<SystemConfiguration> findByHospitalId(String hospitalId);
}
