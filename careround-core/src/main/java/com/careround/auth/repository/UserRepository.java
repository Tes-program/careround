package com.careround.auth.repository;

import com.careround.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByHospitalIdAndEmail(String hospitalId, String email);

    Optional<User> findByIdAndHospitalId(String id, String hospitalId);

    List<User> findAllByHospitalIdAndIsActiveTrue(String hospitalId);

    boolean existsByHospitalIdAndEmail(String hospitalId, String email);

    Optional<User> findByHospitalIdAndEmailAndIsActiveTrue(String hospitalId, String email);

    long countByHospitalIdAndIsActiveTrue(String hospitalId);

    @Query("""
            select u
            from User u
            where u.hospitalId = :hospitalId
              and u.isActive = true
              and (
                    lower(u.firstName) like lower(concat('%', :q, '%'))
                 or lower(u.lastName) like lower(concat('%', :q, '%'))
                 or lower(u.email) like lower(concat('%', :q, '%'))
              )
            """)
    List<User> searchActiveByHospitalId(@Param("hospitalId") String hospitalId, @Param("q") String q);

}
