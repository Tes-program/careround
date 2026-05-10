package com.careround.auth.service;

import com.careround.auth.dto.CreateUserRequest;
import com.careround.auth.dto.UserResponse;
import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.auth.service.UserServiceImpl;
import com.careround.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId("user-123");
        existingUser.setHospitalId("hospital-456");
        existingUser.setFirstName("John");
        existingUser.setLastName("Smith");
        existingUser.setEmail("john.smith@hospital.com");
        existingUser.setPasswordHash("$2a$10$hash");
        existingUser.setRole(UserRole.CONSULTANT);
        existingUser.setActive(true);
    }

    @Test
    void create_withUniqueEmail_shouldSaveAndReturnUser() {
        CreateUserRequest request = new CreateUserRequest(
                "Jane", "Doe", "jane.doe@hospital.com",
                "password123", UserRole.NURSE, null);

        when(userRepository.existsByHospitalIdAndEmail("hospital-456", "jane.doe@hospital.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId("new-user-id");
            return u;
        });

        UserResponse result = userService.create("hospital-456", request);

        assertThat(result.getEmail()).isEqualTo("jane.doe@hospital.com");
        assertThat(result.getRole()).isEqualTo(UserRole.NURSE);
        assertThat(result.getHospitalId()).isEqualTo("hospital-456");
        verify(userRepository).save(argThat(u ->
                u.getPasswordHash().equals("$2a$10$encoded") &&
                        u.getEmail().equals("jane.doe@hospital.com") &&
                        !u.getEmail().contains("A")   // stored lowercase
        ));
    }

    @Test
    void create_withDuplicateEmail_shouldThrowBusinessRuleException() {
        when(userRepository.existsByHospitalIdAndEmail("hospital-456", "john.smith@hospital.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.create("hospital-456",
                new CreateUserRequest("J", "S", "john.smith@hospital.com", "pass", UserRole.NURSE, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getById_withValidIds_shouldReturnUser() {
        when(userRepository.findByIdAndHospitalId("user-123", "hospital-456"))
                .thenReturn(Optional.of(existingUser));

        UserResponse result = userService.getById("hospital-456", "user-123");

        assertThat(result.getId()).isEqualTo("user-123");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Smith");
    }

    @Test
    void getById_withUnknownUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findByIdAndHospitalId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById("hospital-456", "bad-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivate_shouldSetUserInactiveWithoutDeleting() {
        when(userRepository.findByIdAndHospitalId("user-123", "hospital-456"))
                .thenReturn(Optional.of(existingUser));

        userService.deactivate("hospital-456", "user-123");

        assertThat(existingUser.isActive()).isFalse();
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deactivate_withUnknownUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findByIdAndHospitalId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivate("hospital-456", "bad-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByHospital_shouldReturnAllActiveUsers() {
        when(userRepository.findAllByHospitalIdAndIsActiveTrue("hospital-456"))
                .thenReturn(List.of(existingUser));

        List<UserResponse> results = userService.listByHospital("hospital-456");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().isActive()).isTrue();
    }
}

