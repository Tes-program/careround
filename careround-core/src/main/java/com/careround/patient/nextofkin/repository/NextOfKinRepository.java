package com.careround.patient.nextofkin.repository;

import com.careround.patient.nextofkin.entity.NextOfKin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NextOfKinRepository extends JpaRepository<NextOfKin, String> {
}

