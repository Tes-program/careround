package com.careround.hospital.oncall;

import com.careround.hospital.enums.OnCallRole;
import com.careround.hospital.oncall.dto.CreateOnCallRotationRequest;
import com.careround.hospital.oncall.dto.OnCallRotationResponse;

import java.util.List;
import java.util.Optional;

public interface OnCallRotationService {
    OnCallRotationResponse create(String hospitalId, CreateOnCallRotationRequest request);
    OnCallRotationResponse getById(String hospitalId, String rotationId);
    List<OnCallRotationResponse> listByHospital(String hospitalId);
    Optional<OnCallRotationResponse> getCurrentOnCall(String hospitalId, String departmentId, OnCallRole role);
    Optional<OnCallRotationResponse> getCurrentOnCallByWard(String hospitalId, String wardId, OnCallRole role);
    void delete(String hospitalId, String rotationId);
}
