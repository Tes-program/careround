package com.careround.hospital.supervisor;

import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.WardRepository;
import com.careround.hospital.supervisor.dto.PatientSummary;
import com.careround.hospital.supervisor.dto.SupervisorDashboardResponse;
import com.careround.patient.entity.Patient;
import com.careround.patient.repository.PatientRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.security.HospitalContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupervisorDashboardServiceImpl implements SupervisorDashboardService {

    private final WardRepository wardRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional(readOnly = true)
    public SupervisorDashboardResponse getDashboard(String wardId) {
        String hospitalId = HospitalContextHolder.getHospitalId();

        Ward ward = wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        List<Patient> patients = patientRepository
                .findAllByHospitalIdAndWardIdOrderByAcuityColorDescAdmissionDateAsc(hospitalId, wardId);

        List<PatientSummary> summaries = patients.stream()
                .map(p -> new PatientSummary(
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getAcuityColor().name(),
                        p.getAdmissionDate(),
                        p.getWardId()))
                .toList();

        return new SupervisorDashboardResponse(
                ward.getId(),
                ward.getName(),
                ward.getSpecialty(),
                ward.getTotalBeds(),
                summaries.size(),
                summaries);
    }
}
