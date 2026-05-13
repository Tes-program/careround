package com.careround.search;

import com.careround.auth.entity.User;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Patient;
import com.careround.patient.entity.Round;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.PatientRepository;
import com.careround.patient.repository.RoundRepository;
import com.careround.search.dto.GlobalSearchResponse;
import com.careround.search.dto.SearchGroupResponse;
import com.careround.search.dto.SearchResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private static final int GROUP_LIMIT = 8;

    private final PatientRepository patientRepository;
    private final WardRepository wardRepository;
    private final UserRepository userRepository;
    private final MedicalTeamRepository medicalTeamRepository;
    private final CareTaskRepository careTaskRepository;
    private final RoundRepository roundRepository;

    @Override
    @Transactional(readOnly = true)
    public GlobalSearchResponse search(String hospitalId, String query) {
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            return new GlobalSearchResponse(List.of());
        }

        List<SearchGroupResponse> groups = new ArrayList<>();
        groups.add(new SearchGroupResponse("PATIENT", patientResults(hospitalId, q)));
        groups.add(new SearchGroupResponse("WARD", wardResults(hospitalId, q)));
        groups.add(new SearchGroupResponse("USER", userResults(hospitalId, q)));
        groups.add(new SearchGroupResponse("MEDICAL_TEAM", teamResults(hospitalId, q)));
        groups.add(new SearchGroupResponse("CARE_TASK", taskResults(hospitalId, q)));
        groups.add(new SearchGroupResponse("ROUND", roundResults(hospitalId, q)));
        return new GlobalSearchResponse(groups.stream()
                .filter(group -> !group.results().isEmpty())
                .toList());
    }

    private List<SearchResultResponse> patientResults(String hospitalId, String q) {
        return patientRepository.searchByHospitalId(hospitalId, q).stream()
                .limit(GROUP_LIMIT)
                .map(patient -> new SearchResultResponse(
                        "PATIENT",
                        patient.getId(),
                        patient.getFirstName() + " " + patient.getLastName(),
                        patient.getHospitalNumber() + " - " + patient.getStatus(),
                        "/patients/" + patient.getId()))
                .toList();
    }

    private List<SearchResultResponse> wardResults(String hospitalId, String q) {
        return wardRepository.searchByHospitalId(hospitalId, q).stream()
                .limit(GROUP_LIMIT)
                .map(ward -> new SearchResultResponse(
                        "WARD",
                        ward.getId(),
                        ward.getName(),
                        ward.getSpecialty(),
                        "/wards/" + ward.getId()))
                .toList();
    }

    private List<SearchResultResponse> userResults(String hospitalId, String q) {
        return userRepository.searchActiveByHospitalId(hospitalId, q).stream()
                .limit(GROUP_LIMIT)
                .map(user -> new SearchResultResponse(
                        "USER",
                        user.getId(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getRole() + " - " + user.getEmail(),
                        "/users/" + user.getId()))
                .toList();
    }

    private List<SearchResultResponse> teamResults(String hospitalId, String q) {
        return medicalTeamRepository.searchByHospitalId(hospitalId, q).stream()
                .limit(GROUP_LIMIT)
                .map(team -> new SearchResultResponse(
                        "MEDICAL_TEAM",
                        team.getId(),
                        team.getName(),
                        "Consultant: " + team.getConsultantId(),
                        "/medical-teams/" + team.getId()))
                .toList();
    }

    private List<SearchResultResponse> taskResults(String hospitalId, String q) {
        return careTaskRepository.searchByHospitalId(hospitalId, q).stream()
                .limit(GROUP_LIMIT)
                .map(task -> new SearchResultResponse(
                        "CARE_TASK",
                        task.getId(),
                        task.getTitle(),
                        task.getStatus() + " - " + task.getPriority(),
                        "/care-tasks/" + task.getId()))
                .toList();
    }

    private List<SearchResultResponse> roundResults(String hospitalId, String q) {
        String normalized = q.toLowerCase();
        return roundRepository.findAllByHospitalIdOrderByCreatedAtDesc(hospitalId).stream()
                .filter(round -> round.getRoundType().name().toLowerCase().contains(normalized)
                        || round.getStatus().name().toLowerCase().contains(normalized))
                .limit(GROUP_LIMIT)
                .map(round -> new SearchResultResponse(
                        "ROUND",
                        round.getId(),
                        round.getRoundType() + " round",
                        round.getStatus() + " - " + round.getScheduledTime(),
                        "/rounds/" + round.getId()))
                .toList();
    }
}
