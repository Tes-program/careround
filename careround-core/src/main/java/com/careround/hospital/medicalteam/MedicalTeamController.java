package com.careround.hospital.medicalteam;

import com.careround.hospital.medicalteam.dto.AssignWardRequest;
import com.careround.hospital.medicalteam.dto.CreateMedicalTeamRequest;
import com.careround.hospital.medicalteam.dto.InviteResponse;
import com.careround.hospital.medicalteam.dto.MedicalTeamResponse;
import com.careround.hospital.medicalteam.dto.SendInviteRequest;
import com.careround.shared.dto.ApiResponse;
import com.careround.shared.security.HospitalContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class MedicalTeamController {

    private final MedicalTeamService medicalTeamService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')")
    public ResponseEntity<ApiResponse<MedicalTeamResponse>> create(
            @Valid @RequestBody CreateMedicalTeamRequest request) {
        MedicalTeamResponse response = medicalTeamService.create(
                HospitalContextHolder.getHospitalId(), HospitalContextHolder.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Team created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicalTeamResponse>>> list() {
        List<MedicalTeamResponse> response = medicalTeamService
                .listByHospital(HospitalContextHolder.getHospitalId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalTeamResponse>> getById(@PathVariable String id) {
        MedicalTeamResponse response = medicalTeamService
                .getById(HospitalContextHolder.getHospitalId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{teamId}/invites")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<ApiResponse<InviteResponse>> sendInvite(
            @PathVariable String teamId,
            @Valid @RequestBody SendInviteRequest request) {
        InviteResponse response = medicalTeamService.sendInvite(
                HospitalContextHolder.getHospitalId(), teamId,
                HospitalContextHolder.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Invite sent", response));
    }

    @PostMapping("/{teamId}/wards")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')")
    public ResponseEntity<ApiResponse<MedicalTeamResponse>> assignWard(
            @PathVariable String teamId,
            @Valid @RequestBody AssignWardRequest request) {
        MedicalTeamResponse response = medicalTeamService.assignWard(
                HospitalContextHolder.getHospitalId(), teamId,
                HospitalContextHolder.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Ward assigned to team", response));
    }

    @DeleteMapping("/{teamId}/wards/{wardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')")
    public ResponseEntity<ApiResponse<Void>> removeWard(
            @PathVariable String teamId,
            @PathVariable String wardId) {
        medicalTeamService.removeWard(HospitalContextHolder.getHospitalId(), teamId,
                wardId, HospitalContextHolder.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Ward removed from team", null));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable String teamId, @PathVariable String userId) {
        medicalTeamService.removeMember(HospitalContextHolder.getHospitalId(), teamId,
                userId, HospitalContextHolder.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Member removed", null));
    }

    @GetMapping("/invites/pending")
    public ResponseEntity<ApiResponse<List<InviteResponse>>> listPendingInvites() {
        List<InviteResponse> response = medicalTeamService
                .listPendingInvites(HospitalContextHolder.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvite(@PathVariable String inviteId) {
        medicalTeamService.acceptInvite(HospitalContextHolder.getHospitalId(),
                inviteId, HospitalContextHolder.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Invite accepted", null));
    }

    @PostMapping("/invites/{inviteId}/decline")
    public ResponseEntity<ApiResponse<Void>> declineInvite(@PathVariable String inviteId) {
        medicalTeamService.declineInvite(HospitalContextHolder.getHospitalId(),
                inviteId, HospitalContextHolder.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Invite declined", null));
    }
}
