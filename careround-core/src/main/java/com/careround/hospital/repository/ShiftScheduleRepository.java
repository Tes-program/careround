package com.careround.hospital.repository;

import com.careround.hospital.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, String> {

    List<ShiftSchedule> findAllByHospitalIdAndIsActiveTrue(String hospitalId);

    List<ShiftSchedule> findAllByIsActiveTrue();

    Optional<ShiftSchedule> findByIdAndHospitalId(String id, String hospitalId);
}
