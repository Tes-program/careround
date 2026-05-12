package com.careround.auth.repository;

import com.careround.auth.entity.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken, String> {

    Optional<AccountActivationToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
}
