package com.careround.onboarding.repository;

import com.careround.onboarding.entity.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, String> {

    Optional<ActivationToken> findByTokenHashAndUsedFalse(String tokenHash);
}
