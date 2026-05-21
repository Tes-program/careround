package com.careround.hospital.hospital;

import com.careround.auth.entity.User;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.Hospital;
import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.hospital.dto.HospitalRegistrationResponse;
import com.careround.hospital.hospital.dto.HospitalResponse;
import com.careround.hospital.hospital.dto.RegisterHospitalRequest;
import com.careround.hospital.repository.HospitalRepository;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.shared.exception.ConflictException;
import com.careround.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock private HospitalRepository hospitalRepository;
    @Mock private SystemConfigurationRepository systemConfigurationRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private HospitalServiceImpl hospitalService;

    private RegisterHospitalRequest registerRequest(String code) {
        return new RegisterHospitalRequest(
                "City Hospital", null, "admin@city.com", null,
                code, "Alice", "Admin", "alice@city.com", "securePass1");
    }

    @Test
    void register_shouldSaveHospitalAndCreateSystemConfiguration() {
        Hospital saved = savedHospital("hosp-1", "City Hospital", "CITY001");
        User savedUser = savedUser("user-1");

        when(hospitalRepository.existsByCode(any())).thenReturn(false);
        when(hospitalRepository.existsByContactEmail(any())).thenReturn(false);
        when(hospitalRepository.save(any())).thenReturn(saved);
        when(systemConfigurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(savedUser);

        HospitalRegistrationResponse result = hospitalService.register(registerRequest("CITY001"));

        assertThat(result.hospitalId()).isEqualTo("hosp-1");
        assertThat(result.code()).isEqualTo("CITY001");

        ArgumentCaptor<SystemConfiguration> configCaptor = ArgumentCaptor.forClass(SystemConfiguration.class);
        verify(systemConfigurationRepository).save(configCaptor.capture());
        assertThat(configCaptor.getValue().getHospitalId()).isEqualTo("hosp-1");
        assertThat(configCaptor.getValue().getAcuityAmberThreshold()).isEqualTo(5);
        assertThat(configCaptor.getValue().getAcuityRedThreshold()).isEqualTo(7);
    }

    @Test
    void register_shouldInitializeSystemConfigurationWithDefaults() {
        Hospital saved = savedHospital("hosp-2", "New Hospital", "NEW001");
        User savedUser = savedUser("user-2");

        when(hospitalRepository.existsByCode(any())).thenReturn(false);
        when(hospitalRepository.existsByContactEmail(any())).thenReturn(false);
        when(hospitalRepository.save(any())).thenReturn(saved);
        when(systemConfigurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(savedUser);

        hospitalService.register(registerRequest("NEW001"));

        ArgumentCaptor<SystemConfiguration> cap = ArgumentCaptor.forClass(SystemConfiguration.class);
        verify(systemConfigurationRepository).save(cap.capture());
        SystemConfiguration config = cap.getValue();
        assertThat(config.getTaskOverdueReminderMinutes()).isEqualTo(10);
        assertThat(config.getTaskEscalationMinutes()).isEqualTo(20);
        assertThat(config.isPushNotificationsEnabled()).isTrue();
    }

    @Test
    void register_withDuplicateCode_shouldThrowConflictException() {
        when(hospitalRepository.existsByCode("DUP001")).thenReturn(true);

        assertThatThrownBy(() -> hospitalService.register(registerRequest("DUP001")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("code");

        verify(hospitalRepository, never()).save(any());
    }

    @Test
    void register_throwsConflict_whenContactEmailAlreadyExists() {
        when(hospitalRepository.existsByCode(any())).thenReturn(false);
        when(hospitalRepository.existsByContactEmail("admin@city.com")).thenReturn(true);

        assertThatThrownBy(() -> hospitalService.register(registerRequest("CITY001")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");

        verify(hospitalRepository, never()).save(any());
    }

    @Test
    void register_createsAdminUser_withHashedPassword() {
        Hospital saved = savedHospital("hosp-1", "City Hospital", "CITY001");
        User savedUser = savedUser("admin-user-1");

        when(hospitalRepository.existsByCode(any())).thenReturn(false);
        when(hospitalRepository.existsByContactEmail(any())).thenReturn(false);
        when(hospitalRepository.save(any())).thenReturn(saved);
        when(systemConfigurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode("securePass1")).thenReturn("hashed-pw");
        when(userRepository.save(any())).thenReturn(savedUser);

        HospitalRegistrationResponse result = hospitalService.register(registerRequest("CITY001"));

        assertThat(result.adminUserId()).isEqualTo("admin-user-1");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User admin = userCaptor.getValue();
        assertThat(admin.getPasswordHash()).isEqualTo("hashed-pw");
        assertThat(admin.getEmail()).isEqualTo("alice@city.com");
        assertThat(admin.getFirstName()).isEqualTo("Alice");
    }

    @Test
    void register_returnsHospitalIdCodeAndAdminUserId() {
        Hospital saved = savedHospital("hosp-99", "Test Hospital", "TESTHOSP");
        User savedUser = savedUser("admin-99");

        when(hospitalRepository.existsByCode(any())).thenReturn(false);
        when(hospitalRepository.existsByContactEmail(any())).thenReturn(false);
        when(hospitalRepository.save(any())).thenReturn(saved);
        when(systemConfigurationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(savedUser);

        HospitalRegistrationResponse result = hospitalService.register(registerRequest("TESTHOSP"));

        assertThat(result.hospitalId()).isEqualTo("hosp-99");
        assertThat(result.code()).isEqualTo("TESTHOSP");
        assertThat(result.adminUserId()).isEqualTo("admin-99");
    }

    @Test
    void generateCode_stripsNonAlphanumericAndUppercases() {
        assertThat(hospitalService.generateCode(null, "St Mary's Hospital"))
                .isEqualTo("STMARYSH");
        assertThat(hospitalService.generateCode("  ", "Another Hospital"))
                .isEqualTo("ANOTHERH");
        assertThat(hospitalService.generateCode("city01", null))
                .isEqualTo("CITY01");
        assertThat(hospitalService.generateCode("ABC", null))
                .isEqualTo("ABC");
    }

    @Test
    void generateCode_shortName_doesNotPad() {
        assertThat(hospitalService.generateCode(null, "ABC")).isEqualTo("ABC");
    }

    @Test
    void getById_withValidId_shouldReturnHospital() {
        Hospital hospital = savedHospital("hosp-1", "City Hospital", "CITY001");
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

    private Hospital savedHospital(String id, String name, String code) {
        Hospital h = new Hospital();
        h.setId(id);
        h.setName(name);
        h.setCode(code);
        return h;
    }

    private User savedUser(String id) {
        User u = new User();
        u.setId(id);
        return u;
    }
}
