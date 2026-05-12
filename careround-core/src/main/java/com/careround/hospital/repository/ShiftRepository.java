package com.careround.hospital.repository;

import com.careround.hospital.entity.Shift;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.enums.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, String> {

    Optional<Shift> findFirstByWardIdAndStatusOrderByStartTimeDesc(String wardId, ShiftStatus status);

    Optional<Shift> findFirstByWardIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeDesc(
            String wardId, ShiftStatus status, LocalDateTime windowEnd, LocalDateTime windowStart);

    boolean existsByWardIdAndTypeAndStartTime(String wardId, ShiftType type, LocalDateTime startTime);

    List<Shift> findAllByWardIdOrderByStartTimeDesc(String wardId);

    List<Shift> findAllByStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            ShiftStatus status, LocalDateTime windowEnd, LocalDateTime windowStart);

    long countByWardIdInAndStatus(List<String> wardIds, ShiftStatus status);
}
