package com.careround.hospital.shiftschedule;

import com.careround.hospital.entity.ShiftSchedule;
import com.careround.hospital.enums.ShiftType;
import com.careround.hospital.repository.ShiftScheduleRepository;
import com.careround.hospital.shiftschedule.dto.CreateShiftScheduleRequest;
import com.careround.hospital.shiftschedule.dto.ShiftScheduleResponse;
import com.careround.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftScheduleServiceTest {

    @Mock private ShiftScheduleRepository repository;
    @InjectMocks private ShiftScheduleServiceImpl service;

    private ShiftSchedule schedule;

    @BeforeEach
    void setUp() {
        schedule = new ShiftSchedule();
        schedule.setId("sched-1");
        schedule.setHospitalId("hosp-1");
        schedule.setWardId("ward-1");
        schedule.setShiftType(ShiftType.DAY);
        schedule.setStartTime(LocalTime.of(7, 0));
        schedule.setEndTime(LocalTime.of(19, 0));
        schedule.setDaysOfWeek("MON,TUE,WED,THU,FRI");
        schedule.setActive(true);
    }

    @Test
    void create_happyPath_shouldSaveActiveSchedule() {
        when(repository.save(any())).thenAnswer(i -> {
            ShiftSchedule s = i.getArgument(0);
            s.setId("sched-new");
            return s;
        });

        ShiftScheduleResponse result = service.create("hosp-1",
                new CreateShiftScheduleRequest("ward-1", ShiftType.DAY,
                        LocalTime.of(7, 0), LocalTime.of(19, 0), "MON,TUE,WED,THU,FRI"));

        assertThat(result.id()).isEqualTo("sched-new");
        assertThat(result.active()).isTrue();
    }

    @Test
    void getById_withUnknownId_shouldThrowResourceNotFoundException() {
        when(repository.findByIdAndHospitalId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("hosp-1", "bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_crossTenantAccess_shouldThrowResourceNotFoundException() {
        when(repository.findByIdAndHospitalId("sched-1", "other-hosp")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("other-hosp", "sched-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listActive_shouldReturnOnlyActiveSchedules() {
        when(repository.findAllByHospitalIdAndIsActiveTrue("hosp-1")).thenReturn(List.of(schedule));

        List<ShiftScheduleResponse> results = service.listActive("hosp-1");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().active()).isTrue();
    }

    @Test
    void deactivate_shouldSetActiveFalse() {
        when(repository.findByIdAndHospitalId("sched-1", "hosp-1")).thenReturn(Optional.of(schedule));

        service.deactivate("hosp-1", "sched-1");

        assertThat(schedule.isActive()).isFalse();
    }

    @Test
    void deactivate_withUnknownId_shouldThrowResourceNotFoundException() {
        when(repository.findByIdAndHospitalId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate("hosp-1", "bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
