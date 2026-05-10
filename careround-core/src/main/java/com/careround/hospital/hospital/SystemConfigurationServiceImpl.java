package com.careround.hospital.hospital;

import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.hospital.dto.SystemConfigResponse;
import com.careround.hospital.hospital.dto.UpdateSystemConfigRequest;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigurationServiceImpl implements SystemConfigurationService {

    static final String CACHE_PREFIX = "sysconfig:";
    static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final SystemConfigurationRepository systemConfigurationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public SystemConfigResponse getByHospitalId(String hospitalId) {
        String cacheKey = CACHE_PREFIX + hospitalId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, SystemConfigResponse.class);
            } catch (JacksonException e) {
                log.warn("Failed to deserialize cached config for hospital {}: {}", hospitalId, e.getMessage());
            }
        }

        SystemConfiguration config = systemConfigurationRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("System configuration not found"));
        SystemConfigResponse response = toResponse(config);
        putCache(cacheKey, response);
        return response;
    }

    @Override
    @Transactional
    public SystemConfigResponse update(String hospitalId, UpdateSystemConfigRequest request) {
        SystemConfiguration config = systemConfigurationRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("System configuration not found"));

        config.setNewsAmberThreshold(request.newsAmberThreshold());
        config.setNewsRedThreshold(request.newsRedThreshold());
        config.setTaskOverdueGraceMinutes(request.taskOverdueGraceMinutes());
        config.setRoundNotificationsEnabled(request.roundNotificationsEnabled());
        config.setNokNotificationEnabled(request.nokNotificationEnabled());

        redisTemplate.delete(CACHE_PREFIX + hospitalId);
        return toResponse(config);
    }

    private void putCache(String key, SystemConfigResponse response) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(response), CACHE_TTL);
        } catch (JacksonException e) {
            log.warn("Failed to cache system config [key={}]: {}", key, e.getMessage());
        }
    }

    private SystemConfigResponse toResponse(SystemConfiguration c) {
        return new SystemConfigResponse(c.getId(), c.getHospitalId(),
                c.getNewsAmberThreshold(), c.getNewsRedThreshold(),
                c.getTaskOverdueGraceMinutes(), c.isRoundNotificationsEnabled(),
                c.isNokNotificationEnabled());
    }
}
