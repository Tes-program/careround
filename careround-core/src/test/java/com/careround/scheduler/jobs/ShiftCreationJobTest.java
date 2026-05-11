package com.careround.scheduler.jobs;

import com.careround.hospital.entity.Shift;
import com.careround.hospital.entity.ShiftSchedule;
import com.careround.hospital.enums.ShiftType;
import com.careround.hospital.repository.ShiftRepository;
import com.careround.hospital.repository.ShiftScheduleRepository;
import com.careround.scheduler.service.ShiftCreationProcessor;
import com.careround.shared.event.ShiftCreatedEvent;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftCreationJobTest {

    @Mock
    private ShiftScheduleRepository shiftScheduleRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private OutboxService outboxService;

    private ShiftCreationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ShiftCreationProcessor(shiftScheduleRepository, shiftRepository, outboxService);
    }

    @Test
    void shiftAlreadyExists_skipsCreation() {
        ShiftSchedule schedule = activeSchedule(LocalDate.now(ZoneOffset.UTC).getDayOfWeek().name());
        when(shiftScheduleRepository.findAllByIsActiveTrue()).thenReturn(List.of(schedule));
        when(shiftRepository.existsByWardIdAndTypeAndStartTime(eq("ward-1"), eq(ShiftType.DAY), any(LocalDateTime.class)))
                .thenReturn(true);

        int created = processor.createShiftsForToday("corr-1");

        assertThat(created).isZero();
        verify(outboxService, never()).publish(any(), any(), any());
    }

    @Test
    void todayNotInDaysOfWeek_skipsCreation() {
        DayOfWeek otherDay = LocalDate.now(ZoneOffset.UTC).getDayOfWeek().plus(1);
        ShiftSchedule schedule = activeSchedule(otherDay.name());
        when(shiftScheduleRepository.findAllByIsActiveTrue()).thenReturn(List.of(schedule));

        int created = processor.createShiftsForToday("corr-1");

        assertThat(created).isZero();
        verify(shiftRepository, never()).existsByWardIdAndTypeAndStartTime(any(), any(), any());
    }

    @Test
    void happyPath_createsShiftAndPublishesEvent() {
        ShiftSchedule schedule = activeSchedule(LocalDate.now(ZoneOffset.UTC).getDayOfWeek().name());
        when(shiftScheduleRepository.findAllByIsActiveTrue()).thenReturn(List.of(schedule));
        when(shiftRepository.existsByWardIdAndTypeAndStartTime(eq("ward-1"), eq(ShiftType.DAY), any(LocalDateTime.class)))
                .thenReturn(false);
        when(shiftRepository.save(any())).thenAnswer(invocation -> {
            Shift shift = invocation.getArgument(0);
            shift.setId("shift-1");
            return shift;
        });

        int created = processor.createShiftsForToday("corr-1");

        assertThat(created).isEqualTo(1);
        verify(outboxService).publish(eq("SHIFT_CREATED"), any(ShiftCreatedEvent.class), eq("hosp-1"));
    }

    private ShiftSchedule activeSchedule(String daysOfWeek) {
        ShiftSchedule schedule = new ShiftSchedule();
        schedule.setId("schedule-1");
        schedule.setHospitalId("hosp-1");
        schedule.setWardId("ward-1");
        schedule.setShiftType(ShiftType.DAY);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(16, 0));
        schedule.setDaysOfWeek(daysOfWeek);
        schedule.setActive(true);
        return schedule;
    }
}
