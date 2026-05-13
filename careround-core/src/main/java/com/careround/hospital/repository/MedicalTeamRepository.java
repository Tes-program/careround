package com.careround.hospital.repository;

import com.careround.hospital.entity.MedicalTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicalTeamRepository extends JpaRepository<MedicalTeam, String> {

    List<MedicalTeam> findAllByHospitalId(String hospitalId);

    Optional<MedicalTeam> findByIdAndHospitalId(String id, String hospitalId);

    List<MedicalTeam> findAllByConsultantIdAndHospitalId(String consultantId, String hospitalId);

    long countByHospitalId(String hospitalId);

    @Query("""
            select t
            from MedicalTeam t
            where t.hospitalId = :hospitalId
              and lower(t.name) like lower(concat('%', :q, '%'))
            """)
    List<MedicalTeam> searchByHospitalId(@Param("hospitalId") String hospitalId, @Param("q") String q);
}
