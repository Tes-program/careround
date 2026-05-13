package com.careround.hospital.hospital;

import com.careround.hospital.entity.SystemConfiguration;
import com.careround.hospital.hospital.dto.SystemConfigResponse;
import com.careround.hospital.hospital.dto.UpdateSystemConfigRequest;
import com.careround.hospital.repository.SystemConfigurationRepository;
import com.careround.shared.exception.ResourceNotFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigurationServiceTest {

    @Mock private SystemConfigurationRepository systemConfigurationRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private SystemConfigurationServiceImpl service;

    private SystemConfiguration config;

    @BeforeEach
    void setUp() {
        config = new SystemConfiguration();
        config.setId("cfg-1");
        config.setHospitalId("hosp-1");
        config.setNewsAmberThreshold(5);
        config.setNewsRedThreshold(7);
        config.setTaskOverdueGraceMinutes(30);
        config.setRoundNotificationsEnabled(true);
        config.setNokNotificationEnabled(true);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getByHospitalId_whenCacheMiss_shouldReadFromDbAndPopulateCache() throws Exception {
        when(valueOperations.get("sysconfig:hosp-1")).thenReturn(null);
        when(systemConfigurationRepository.findByHospitalId("hosp-1")).thenReturn(Optional.of(config));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":\"cfg-1\"}");

        SystemConfigResponse result = service.getByHospitalId("hosp-1");

        assertThat(result.id()).isEqualTo("cfg-1");
        assertThat(result.hospitalId()).isEqualTo("hosp-1");
        verify(systemConfigurationRepository).findByHospitalId("hosp-1");
        verify(valueOperations).set(eq("sysconfig:hosp-1"), any(), any());
    }

    @Test
    void getByHospitalId_secondReadWithinTTL_hitsCacheNotDatabase() throws Exception {
        SystemConfigResponse cachedResponse = new SystemConfigResponse(
                "cfg-1", "hosp-1", 5, 7, 30, true, true);
        String cachedJson = "{\"id\":\"cfg-1\"}";

        when(valueOperations.get("sysconfig:hosp-1")).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, SystemConfigResponse.class)).thenReturn(cachedResponse);

        SystemConfigResponse result = service.getByHospitalId("hosp-1");

        assertThat(result.id()).isEqualTo("cfg-1");
        verify(systemConfigurationRepository, never()).findByHospitalId(any());
    }

    @Test
    void getByHospitalId_whenNotFound_shouldThrowResourceNotFoundException() {
        when(valueOperations.get(any())).thenReturn(null);
        when(systemConfigurationRepository.findByHospitalId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByHospitalId("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("System configuration not found");
    }

    @Test
    void update_shouldPersistChangesAndEvictCache() {
        when(systemConfigurationRepository.findByHospitalId("hosp-1")).thenReturn(Optional.of(config));

        service.update("hosp-1", new UpdateSystemConfigRequest(6, 8, 45, false, false));

        assertThat(config.getNewsAmberThreshold()).isEqualTo(6);
        assertThat(config.getNewsRedThreshold()).isEqualTo(8);
        assertThat(config.getTaskOverdueGraceMinutes()).isEqualTo(45);
        assertThat(config.isRoundNotificationsEnabled()).isFalse();
        verify(redisTemplate).delete("sysconfig:hosp-1");
    }

    @Test
    void update_whenRedisEvictFails_shouldStillReturnUpdatedConfig() {
        when(systemConfigurationRepository.findByHospitalId("hosp-1")).thenReturn(Optional.of(config));
        when(redisTemplate.delete("sysconfig:hosp-1"))
                .thenThrow(new RedisConnectionFailureException("redis unavailable"));

        SystemConfigResponse result = service.update(
                "hosp-1", new UpdateSystemConfigRequest(6, 8, 45, false, false));

        assertThat(result.newsAmberThreshold()).isEqualTo(6);
        assertThat(result.newsRedThreshold()).isEqualTo(8);
        assertThat(result.taskOverdueGraceMinutes()).isEqualTo(45);
        assertThat(result.roundNotificationsEnabled()).isFalse();
    }

    @Test
    void update_whenNotFound_shouldThrowResourceNotFoundException() {
        when(systemConfigurationRepository.findByHospitalId("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("bad", new UpdateSystemConfigRequest(5, 7, 30, true, true)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
