package com.careround.notification.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpCoreLookupClient implements CoreLookupClient {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${careround.core.base-url:http://localhost:8080}")
    private String coreBaseUrl;

    @Value("${careround.core.service-account-jwt:}")
    private String serviceAccountJwt;

    @Override
    public Optional<String> findWardSupervisorId(String wardId) {
        try {
            String body = get("/api/v1/wards/" + wardId);
            Map<?, ?> response = objectMapper.readValue(body, Map.class);
            Object data = response.get("data");
            if (data instanceof Map<?, ?> ward) {
                Object supervisorId = ward.get("supervisorId");
                return supervisorId instanceof String value && StringUtils.hasText(value)
                        ? Optional.of(value)
                        : Optional.empty();
            }
        } catch (Exception ex) {
            log.warn("action=CORE_WARD_LOOKUP_FAILED wardId={} message={}", wardId, ex.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<NextOfKinContact> findNextOfKin(String patientId) {
        try {
            String body = get("/api/v1/patients/" + patientId + "/next-of-kin");
            Map<?, ?> response = objectMapper.readValue(body, Map.class);
            Object data = response.get("data");
            if (data instanceof List<?> items) {
                List<NextOfKinContact> contacts = new ArrayList<>();
                for (Object item : items) {
                    if (item instanceof Map<?, ?> nok) {
                        contacts.add(new NextOfKinContact(
                                asString(nok.get("id")),
                                asString(nok.get("preferredContactMethod")),
                                Boolean.TRUE.equals(nok.get("notificationConsent"))));
                    }
                }
                return contacts;
            }
        } catch (Exception ex) {
            log.warn("action=CORE_NOK_LOOKUP_FAILED patientId={} message={}", patientId, ex.getMessage());
        }
        return List.of();
    }

    private String get(String path) {
        RestClient.RequestHeadersSpec<?> request = restClientBuilder
                .baseUrl(coreBaseUrl)
                .build()
                .get()
                .uri(path);
        if (StringUtils.hasText(serviceAccountJwt)) {
            request = request.header("Authorization", "Bearer " + serviceAccountJwt);
        }
        return request.retrieve().body(String.class);
    }

    private String asString(Object value) {
        return value instanceof String string ? string : null;
    }
}
