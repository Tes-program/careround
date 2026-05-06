package com.careround.common.repository;

import com.careround.common.entity.Handover;
import com.careround.common.enums.HandoverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HandoverRepository extends JpaRepository<Handover, String> {
    List<Handover> findByWardIdOrderByCreatedAtDesc(String wardId);
    Optional<Handover> findByOutgoingShiftIdAndStatus(String outgoingShiftId, HandoverStatus status);
}
