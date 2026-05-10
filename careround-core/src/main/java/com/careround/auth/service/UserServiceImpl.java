package com.careround.auth.service;

import com.careround.auth.dto.CreateUserRequest;
import com.careround.auth.dto.UserResponse;
import com.careround.auth.entity.User;
import com.careround.auth.repository.UserRepository;
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
        user.setDepartmentId(request.getDepartmentId());
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
    public void deactivate(String hospitalId, String userId) {
        User user = userRepository.findByIdAndHospitalId(userId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listByHospital(String hospitalId) {
        return userRepository.findAllByHospitalIdAndActiveTrue(hospitalId)
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
                user.getDepartmentId(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
