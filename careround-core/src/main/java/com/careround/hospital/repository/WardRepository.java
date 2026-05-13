package com.careround.hospital.repository;

import com.careround.hospital.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, String> {

    List<Ward> findAllByHospitalId(String hospitalId);

    Optional<Ward> findByIdAndHospitalId(String id, String hospitalId);

    long countByHospitalId(String hospitalId);

    @Query("""
            select w
            from Ward w
            where w.hospitalId = :hospitalId
              and (
                    lower(w.name) like lower(concat('%', :q, '%'))
                 or lower(w.specialty) like lower(concat('%', :q, '%'))
              )
            """)
    List<Ward> searchByHospitalId(@Param("hospitalId") String hospitalId, @Param("q") String q);
}
