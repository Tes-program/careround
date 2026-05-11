package com.careround.platform.service;

import com.careround.platform.entity.PlatformOperator;
import com.careround.platform.entity.PlatformOperatorRole;
import com.careround.platform.repository.PlatformOperatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformOperatorBootstrap implements ApplicationRunner {

    private final PlatformOperatorRepository platformOperatorRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${careround.platform.bootstrap-admin.email:}")
    private String email;

    @Value("${careround.platform.bootstrap-admin.password:}")
    private String password;

    @Value("${careround.platform.bootstrap-admin.first-name:Platform}")
    private String firstName;

    @Value("${careround.platform.bootstrap-admin.last-name:Admin}")
    private String lastName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return;
        }
        if (platformOperatorRepository.count() > 0) {
            return;
        }

        PlatformOperator operator = new PlatformOperator();
        operator.setFirstName(firstName);
        operator.setLastName(lastName);
        operator.setEmail(email.trim().toLowerCase(Locale.ROOT));
        operator.setPasswordHash(passwordEncoder.encode(password));
        operator.setRole(PlatformOperatorRole.PLATFORM_ADMIN);
        operator.setActive(true);
        platformOperatorRepository.save(operator);
        log.info("action=PLATFORM_OPERATOR_BOOTSTRAPPED email={}", operator.getEmail());
    }
}
