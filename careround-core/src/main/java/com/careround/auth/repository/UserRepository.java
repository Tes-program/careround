package com.careround.auth.repository;

import com.careround.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByHospitalIdAndEmail(String hospitalId, String email);

    Optional<User> findByIdAndHospitalId(String id, String hospitalId);

    List<User> findAllByHospitalIdAndIsActiveTrue(String hospitalId);

    boolean existsByHospitalIdAndEmail(String hospitalId, String email);

    Optional<User> findByHospitalIdAndEmailAndIsActiveTrue(String hospitalId, String email);

    long countByHospitalIdAndIsActiveTrue(String hospitalId);

}
