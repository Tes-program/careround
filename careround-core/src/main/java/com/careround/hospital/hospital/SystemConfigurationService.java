package com.careround.hospital.hospital;

import com.careround.hospital.hospital.dto.SystemConfigResponse;
import com.careround.hospital.hospital.dto.UpdateSystemConfigRequest;

public interface SystemConfigurationService {
    SystemConfigResponse getByHospitalId(String hospitalId);
    SystemConfigResponse update(String hospitalId, UpdateSystemConfigRequest request);
}
