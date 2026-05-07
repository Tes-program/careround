package com.careround.hospital.shiftschedule.repository;

import com.careround.hospital.shiftschedule.entity.ShiftSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, String> {
}

