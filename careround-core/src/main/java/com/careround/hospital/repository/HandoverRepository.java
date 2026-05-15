package com.careround.hospital.repository;

import com.careround.hospital.entity.Handover;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HandoverRepository extends JpaRepository<Handover, String> {

    List<Handover> findAllByWardIdOrderByCreatedAtDesc(String wardId);

    List<Handover> findTop5ByWardIdOrderByCreatedAtDesc(String wardId);

    Optional<Handover> findByOutgoingShiftId(String shiftId);
}
