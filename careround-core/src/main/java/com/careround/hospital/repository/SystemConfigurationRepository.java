package com.careround.hospital.repository;

import com.careround.hospital.entity.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {

    Optional<SystemConfiguration> findByHospitalId(String hospitalId);
}
