package com.careround.hospital.shiftschedule;

import com.careround.hospital.entity.ShiftSchedule;
import com.careround.hospital.repository.ShiftScheduleRepository;
import com.careround.hospital.shiftschedule.dto.CreateShiftScheduleRequest;
import com.careround.hospital.shiftschedule.dto.ShiftScheduleResponse;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftScheduleServiceImpl implements ShiftScheduleService {

    private final ShiftScheduleRepository shiftScheduleRepository;

    @Override
    @Transactional
    public ShiftScheduleResponse create(String hospitalId, CreateShiftScheduleRequest request) {
        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setHospitalId(hospitalId);
        schedule.setWardId(request.wardId());
        schedule.setShiftType(request.shiftType());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setDaysOfWeek(request.daysOfWeek());
        schedule.setActive(true);
        return toResponse(shiftScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftScheduleResponse getById(String hospitalId, String scheduleId) {
        return shiftScheduleRepository.findByIdAndHospitalId(scheduleId, hospitalId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Shift schedule not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftScheduleResponse> listActive(String hospitalId) {
        return shiftScheduleRepository.findAllByHospitalIdAndIsActiveTrue(hospitalId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deactivate(String hospitalId, String scheduleId) {
        ShiftSchedule schedule = shiftScheduleRepository.findByIdAndHospitalId(scheduleId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift schedule not found"));
        schedule.setActive(false);
    }

    private ShiftScheduleResponse toResponse(ShiftSchedule s) {
        return new ShiftScheduleResponse(s.getId(), s.getHospitalId(), s.getWardId(),
                s.getShiftType(), s.getStartTime(), s.getEndTime(),
                s.getDaysOfWeek(), s.isActive(), s.getCreatedAt());
    }
}
