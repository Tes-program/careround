package com.careround.common.repository;

import com.careround.common.entity.User;
import com.careround.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailAndHospitalId(String email, String hospitalId);
    Optional<User> findByIdAndHospitalId(String id, String hospitalId);
    List<User> findByHospitalId(String hospitalId);
    List<User> findByHospitalIdAndRole(String hospitalId, UserRole role);
    List<User> findByHospitalIdAndIsActiveTrue(String hospitalId);
    boolean existsByEmailAndHospitalId(String email, String hospitalId);
}
