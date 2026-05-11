package com.careround.scheduler.service;

import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.ShiftSchedule;
import com.careround.hospital.enums.ShiftStatus;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.ShiftScheduleRepository;
import com.careround.shared.event.ShiftCreatedEvent;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftCreationProcessor {

    private final ShiftScheduleRepository shiftScheduleRepository;
    private final ShiftRepository shiftRepository;
    private final OutboxService outboxService;

    @Transactional
    public int createShiftsForToday(String correlationId) {
        int createdCount = 0;
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DayOfWeek currentDay = today.getDayOfWeek();

        for (ShiftSchedule schedule : shiftScheduleRepository.findAllByIsActiveTrue()) {
            if (!appliesToday(schedule, currentDay) || schedule.getWardId() == null) {
                continue;
            }

            LocalDateTime startTime = today.atTime(schedule.getStartTime());
            LocalDateTime endTime = today.atTime(schedule.getEndTime());
            if (!endTime.isAfter(startTime)) {
                endTime = endTime.plusDays(1);
            }

            if (createShiftIfMissing(schedule, startTime, endTime, correlationId)) {
                createdCount++;
            }
        }

        return createdCount;
    }

    private boolean createShiftIfMissing(ShiftSchedule schedule,
                                         LocalDateTime startTime,
                                         LocalDateTime endTime,
                                         String correlationId) {
        if (shiftRepository.existsByWardIdAndTypeAndStartTime(
                schedule.getWardId(), schedule.getShiftType(), startTime)) {
            return false;
        }

        Shift shift = new Shift();
        shift.setWardId(schedule.getWardId());
        shift.setShiftScheduleId(schedule.getId());
        shift.setType(schedule.getShiftType());
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setStatus(ShiftStatus.PENDING_ASSIGNMENT);

        Shift saved = shiftRepository.save(shift);

        outboxService.publish(
                "SHIFT_CREATED",
                new ShiftCreatedEvent(
                        schedule.getHospitalId(),
                        saved.getId(),
                        saved.getWardId(),
                        saved.getType(),
                        saved.getStartTime(),
                        saved.getEndTime(),
                        correlationId
                ),
                schedule.getHospitalId()
        );

        log.info("action=SHIFT_CREATED wardId={} type={} startTime={}",
                saved.getWardId(), saved.getType(), saved.getStartTime());
        return true;
    }

    private boolean appliesToday(ShiftSchedule schedule, DayOfWeek currentDay) {
        Set<DayOfWeek> scheduleDays = Arrays.stream(schedule.getDaysOfWeek().split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> DayOfWeek.valueOf(value.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toSet());
        return scheduleDays.contains(currentDay);
    }
}
