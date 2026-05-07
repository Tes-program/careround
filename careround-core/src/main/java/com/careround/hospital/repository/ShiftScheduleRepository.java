package com.careround.hospital.repository;

import com.careround.hospital.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, String> {

    List<ShiftSchedule> findAllByHospitalIdAndIsActiveTrue(String hospitalId);
}
