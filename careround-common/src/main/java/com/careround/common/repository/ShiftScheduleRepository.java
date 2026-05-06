package com.careround.common.repository;

import com.careround.common.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, String> {
    List<ShiftSchedule> findByHospitalId(String hospitalId);
    List<ShiftSchedule> findByHospitalIdAndIsActiveTrue(String hospitalId);
    List<ShiftSchedule> findByIsActiveTrue();
}
