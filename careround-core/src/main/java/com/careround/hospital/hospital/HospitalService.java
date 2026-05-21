package com.careround.hospital.hospital;

import com.careround.hospital.hospital.dto.HospitalRegistrationResponse;
import com.careround.hospital.hospital.dto.HospitalResponse;
import com.careround.hospital.hospital.dto.RegisterHospitalRequest;
import com.careround.hospital.hospital.dto.UpdateHospitalRequest;

import java.util.List;

public interface HospitalService {
    HospitalRegistrationResponse register(RegisterHospitalRequest request);
    HospitalResponse getById(String hospitalId);
    HospitalResponse update(String hospitalId, UpdateHospitalRequest request);
    List<HospitalResponse> listAll();
}
