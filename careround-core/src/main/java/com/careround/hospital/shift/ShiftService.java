package com.careround.hospital.shift;

import com.careround.hospital.shift.dto.AssignStaffRequest;
import com.careround.hospital.shift.dto.ShiftResponse;
import com.careround.hospital.enums.ShiftStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ShiftService {
    ShiftResponse assignStaff(String hospitalId, String shiftId, AssignStaffRequest request);
    ShiftResponse getCurrentShift(String hospitalId, String wardId);
    List<ShiftResponse> listShifts(String hospitalId, String wardId, ShiftStatus status, LocalDateTime from, LocalDateTime to);
}
