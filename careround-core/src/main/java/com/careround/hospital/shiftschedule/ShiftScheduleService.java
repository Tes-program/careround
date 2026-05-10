package com.careround.hospital.shiftschedule;

import com.careround.hospital.shiftschedule.dto.CreateShiftScheduleRequest;
import com.careround.hospital.shiftschedule.dto.ShiftScheduleResponse;

import java.util.List;

public interface ShiftScheduleService {
    ShiftScheduleResponse create(String hospitalId, CreateShiftScheduleRequest request);
    ShiftScheduleResponse getById(String hospitalId, String scheduleId);
    List<ShiftScheduleResponse> listActive(String hospitalId);
    void deactivate(String hospitalId, String scheduleId);
}
