package com.careround.common.repository;

import com.careround.common.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, String> {
    List<Ward> findByHospitalId(String hospitalId);
    Optional<Ward> findByIdAndHospitalId(String id, String hospitalId);
}
