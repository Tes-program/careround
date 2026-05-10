package com.careround.hospital.hospital;

import com.careround.hospital.hospital.dto.CreateHospitalRequest;
import com.careround.hospital.hospital.dto.HospitalResponse;

public interface HospitalService {
    HospitalResponse register(CreateHospitalRequest request);
    HospitalResponse getById(String hospitalId);
}
