package com.careround.hospital.ward;

import com.careround.hospital.ward.dto.CreateWardRequest;
import com.careround.hospital.ward.dto.UpdateWardRequest;
import com.careround.hospital.ward.dto.WardResponse;

import java.util.List;

public interface WardService {
    WardResponse create(String hospitalId, CreateWardRequest request);
    WardResponse getById(String hospitalId, String wardId);
    List<WardResponse> listByHospital(String hospitalId);
    WardResponse update(String hospitalId, String wardId, UpdateWardRequest request);
    void delete(String hospitalId, String wardId);
}
