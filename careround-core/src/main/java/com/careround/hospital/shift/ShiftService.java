package com.careround.hospital.shift;

import com.careround.hospital.shift.dto.AssignStaffRequest;
import com.careround.hospital.shift.dto.ShiftResponse;

public interface ShiftService {
    ShiftResponse assignStaff(String hospitalId, String shiftId, AssignStaffRequest request);
    ShiftResponse getCurrentShift(String hospitalId, String wardId);
}
