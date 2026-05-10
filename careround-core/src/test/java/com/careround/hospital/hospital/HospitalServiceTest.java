package com.careround.hospital.hospital;

import com.careround.hospital.entity.Hospital;
import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.hospital.dto.CreateHospitalRequest;
import com.careround.hospital.hospital.dto.HospitalResponse;
import com.careround.hospital.repository.HospitalRepository;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock private HospitalRepository hospitalRepository;
    @Mock private SystemConfigurationRepository systemConfigurationRepository;

    @InjectMocks private HospitalServiceImpl hospitalService;

    @Test
    void register_shouldSaveHospitalAndCreateSystemConfigurationInSameCall() {
        Hospital saved = new Hospital();
        saved.setId("hosp-1");
        saved.setName("City Hospital");
        saved.setContactEmail("admin@city.com");

        when(hospitalRepository.save(any())).thenReturn(saved);
        when(systemConfigurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HospitalResponse result = hospitalService.register(
                new CreateHospitalRequest("City Hospital", "123 Main St", "admin@city.com", null));

        assertThat(result.id()).isEqualTo("hosp-1");
        assertThat(result.name()).isEqualTo("City Hospital");

        ArgumentCaptor<SystemConfiguration> configCaptor = ArgumentCaptor.forClass(SystemConfiguration.class);
        verify(systemConfigurationRepository).save(configCaptor.capture());
        assertThat(configCaptor.getValue().getHospitalId()).isEqualTo("hosp-1");
        assertThat(configCaptor.getValue().getNewsAmberThreshold()).isEqualTo(5);
        assertThat(configCaptor.getValue().getNewsRedThreshold()).isEqualTo(7);
    }

    @Test
    void register_shouldInitializeSystemConfigurationWithDefaults() {
        Hospital saved = new Hospital();
        saved.setId("hosp-2");

        when(hospitalRepository.save(any())).thenReturn(saved);
        when(systemConfigurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        hospitalService.register(
                new CreateHospitalRequest("New Hospital", null, "new@hosp.com", null));

        ArgumentCaptor<SystemConfiguration> cap = ArgumentCaptor.forClass(SystemConfiguration.class);
        verify(systemConfigurationRepository).save(cap.capture());
        SystemConfiguration config = cap.getValue();
        assertThat(config.getTaskOverdueGraceMinutes()).isEqualTo(30);
        assertThat(config.isRoundNotificationsEnabled()).isTrue();
        assertThat(config.isNokNotificationEnabled()).isTrue();
    }

    @Test
    void getById_withValidId_shouldReturnHospital() {
        Hospital hospital = new Hospital();
        hospital.setId("hosp-1");
        hospital.setName("City Hospital");
        hospital.setContactEmail("admin@city.com");

        when(hospitalRepository.findById("hosp-1")).thenReturn(Optional.of(hospital));

        HospitalResponse result = hospitalService.getById("hosp-1");

        assertThat(result.id()).isEqualTo("hosp-1");
        assertThat(result.name()).isEqualTo("City Hospital");
    }

    @Test
    void getById_withUnknownId_shouldThrowResourceNotFoundException() {
        when(hospitalRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hospitalService.getById("bad-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Hospital not found");
    }
}
