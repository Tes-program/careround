package com.careround.hospital.repository;

import com.careround.hospital.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, String> {

    List<Ward> findAllByHospitalId(String hospitalId);

    Optional<Ward> findByIdAndHospitalId(String id, String hospitalId);

    long countByHospitalId(String hospitalId);
}
