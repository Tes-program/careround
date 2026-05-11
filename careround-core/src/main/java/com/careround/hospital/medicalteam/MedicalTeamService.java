package com.careround.hospital.medicalteam;

import com.careround.hospital.medicalteam.dto.CreateMedicalTeamRequest;
import com.careround.hospital.medicalteam.dto.AssignWardRequest;
import com.careround.hospital.medicalteam.dto.InviteResponse;
import com.careround.hospital.medicalteam.dto.MedicalTeamResponse;
import com.careround.hospital.medicalteam.dto.SendInviteRequest;

import java.util.List;

public interface MedicalTeamService {
    MedicalTeamResponse create(String hospitalId, String requestingUserId, CreateMedicalTeamRequest request);
    MedicalTeamResponse getById(String hospitalId, String teamId);
    List<MedicalTeamResponse> listByHospital(String hospitalId);
    InviteResponse sendInvite(String hospitalId, String teamId, String requestingUserId, SendInviteRequest request);
    void acceptInvite(String hospitalId, String inviteId, String userId);
    void declineInvite(String hospitalId, String inviteId, String userId);
    void removeMember(String hospitalId, String teamId, String memberUserId, String requestingUserId);
    MedicalTeamResponse assignWard(String hospitalId, String teamId, String requestingUserId, AssignWardRequest request);
    void removeWard(String hospitalId, String teamId, String wardId, String requestingUserId);
    List<InviteResponse> listPendingInvites(String userId);
}
