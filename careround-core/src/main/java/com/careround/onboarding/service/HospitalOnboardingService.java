package com.careround.onboarding.service;

import com.careround.onboarding.dto.CreateHospitalOnboardingRequest;
import com.careround.onboarding.dto.HospitalOnboardingResponse;
import com.careround.onboarding.dto.ProvisionHospitalResponse;
import com.careround.onboarding.dto.ProvisionHospitalTenantRequest;
import com.careround.onboarding.dto.ReviewHospitalOnboardingRequest;
import com.careround.onboarding.entity.HospitalOnboardingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HospitalOnboardingService {

    HospitalOnboardingResponse submitRequest(CreateHospitalOnboardingRequest request);

    Page<HospitalOnboardingResponse> listRequests(HospitalOnboardingStatus status, Pageable pageable);

    HospitalOnboardingResponse getRequest(String requestId);

    HospitalOnboardingResponse reviewRequest(String requestId, String reviewerUserId,
                                              ReviewHospitalOnboardingRequest request);

    ProvisionHospitalResponse provisionTenant(String requestId, ProvisionHospitalTenantRequest request);
}
