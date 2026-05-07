package com.careround.hospital.handover.repository;

import com.careround.hospital.handover.entity.Handover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HandoverRepository extends JpaRepository<Handover, String> {
}

