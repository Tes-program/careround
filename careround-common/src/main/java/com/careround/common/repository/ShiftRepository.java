package com.careround.common.repository;

import com.careround.common.entity.Shift;
import com.careround.common.enums.ShiftStatus;
import com.careround.common.enums.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, String> {

    /** Uniqueness check used by ShiftAutoCreatorJob before creating */
    boolean existsByWardIdAndTypeAndStartTime(String wardId, ShiftType type, LocalDateTime startTime);

    List<ShiftRepository.ShiftWardProjection> findByWardId(String wardId);

    /** Find the currently active (ACTIVE or PENDING_ASSIGNMENT) shift for a ward */
    @Query("SELECT s FROM Shift s WHERE s.wardId = :wardId " +
           "AND s.status IN ('ACTIVE', 'PENDING_ASSIGNMENT') " +
           "AND s.startTime <= :now AND s.endTime > :now " +
           "ORDER BY s.startTime DESC")
    Optional<Shift> findCurrentByWardId(@Param("wardId") String wardId,
                                         @Param("now") LocalDateTime now);

    List<Shift> findByWardIdAndStatus(String wardId, ShiftStatus status);
    List<Shift> findByWardIdOrderByStartTimeDesc(String wardId);

    interface ShiftWardProjection {
        String getId();
        ShiftType getType();
        ShiftStatus getStatus();
        LocalDateTime getStartTime();
        LocalDateTime getEndTime();
    }
}
