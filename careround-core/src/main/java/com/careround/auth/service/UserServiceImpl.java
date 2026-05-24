package com.careround.auth.service;

import com.careround.auth.dto.CreateUserRequest;
import com.careround.auth.dto.UpdateProfileRequest;
import com.careround.auth.dto.UpdateUserRequest;
import com.careround.auth.dto.UserResponse;
import com.careround.auth.entity.User;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(String hospitalId, CreateUserRequest request) {
        String email = request.getEmail().toLowerCase(Locale.ROOT);

        if (userRepository.existsByHospitalIdAndEmail(hospitalId, email)) {
            throw new BusinessRuleException(
                    "A user with email '" + email + "' already exists in this hospital");
        }

        User user = new User();
        user.setHospitalId(hospitalId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setWardId(request.getWardId());
        user.setActive(true);

        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(String hospitalId, String userId) {
        return userRepository.findByIdAndHospitalId(userId, hospitalId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public UserResponse updateUser(String hospitalId, String userId, UpdateUserRequest request) {
        User user = userRepository.findByIdAndHospitalId(userId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().toLowerCase(Locale.ROOT);
            if (userRepository.existsByHospitalIdAndEmailAndIdNot(hospitalId, newEmail, userId)) {
                throw new BusinessRuleException("Email '" + newEmail + "' is already in use");
            }
            user.setEmail(newEmail);
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse reactivate(String hospitalId, String userId) {
        User user = userRepository.findByIdAndHospitalId(userId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.isActive()) {
            throw new BusinessRuleException("User is already active");
        }
        user.setActive(true);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String hospitalId, String userId, UpdateProfileRequest request) {
        User user = userRepository.findByIdAndHospitalId(userId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().toLowerCase(Locale.ROOT);
            if (userRepository.existsByHospitalIdAndEmailAndIdNot(hospitalId, newEmail, userId)) {
                throw new BusinessRuleException("Email '" + newEmail + "' is already in use");
            }
            user.setEmail(newEmail);
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivate(String hospitalId, String userId) {
        User user = userRepository.findByIdAndHospitalId(userId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
    }

    @Override
    @Transactional
    public void updateDeviceToken(String userId, String hospitalId, String deviceToken) {
        User user = userRepository.findByIdAndHospitalId(userId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFcmToken(deviceToken);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse assignWard(String hospitalId, String userId, String wardId) {
        User user = userRepository.findByIdAndHospitalId(userId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));
        user.setWardId(wardId);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listByHospital(String hospitalId) {
        return userRepository.findAllByHospitalIdAndIsActiveTrue(hospitalId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getHospitalId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getFcmToken(),
                user.isActive(),
                user.getCreatedAt(),
                user.getWardId()
        );
    }
}
