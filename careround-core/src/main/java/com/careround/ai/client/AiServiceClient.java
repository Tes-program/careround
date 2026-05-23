package com.careround.ai.client;

import com.careround.ai.dto.ExtractedPrescription;
import com.careround.shared.exception.AiServiceException;
import com.careround.shared.exception.AiServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {

    private final RestClient aiRestClient;
    private final WebClient aiWebClient;

    public boolean isReady() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> response = aiRestClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(Map.class);
            return response != null && "ready".equals(response.get("status"));
        } catch (Exception e) {
            log.warn("action=AI_HEALTH_CHECK status=unavailable message={}", e.getMessage());
            return false;
        }
    }

    public Flux<ServerSentEvent<String>> streamVoiceNote(MultipartFile audio, String patientId, String mode) {
        if (!isReady()) {
            throw new AiServiceUnavailableException("AI service is not ready");
        }

        try {
            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
            formData.add("audio", audio.getResource());
            formData.add("patient_id", patientId);
            formData.add("current_time", LocalDateTime.now(ZoneOffset.UTC).toString());
            formData.add("mode", mode);

            return aiWebClient.post()
                    .uri("/process-voice-note")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(formData))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), (resp) ->
                            resp.bodyToMono(String.class).map(body ->
                                    new AiServiceException("AI service returned error " + resp.statusCode() + ": " + body)))
                    .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                    .doOnError(WebClientResponseException.ServiceUnavailable.class, e ->
                            log.warn("action=AI_STREAM status=503 patientId={}", patientId));
        } catch (AiServiceUnavailableException | AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("AI service call failed: " + e.getMessage());
        }
    }

    public List<ExtractedPrescription> extractPrescriptionsFromText(String noteText, String patientId) {
        if (!isReady()) {
            throw new AiServiceUnavailableException("AI service is not ready");
        }
        try {
            Map<String, String> body = Map.of(
                    "note_text", noteText,
                    "patient_id", patientId,
                    "current_time", LocalDateTime.now(ZoneOffset.UTC).toString()
            );
            List<ExtractedPrescription> result = aiRestClient.post()
                    .uri("/extract-prescriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return result != null ? result : List.of();
        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("AI prescription extraction failed: " + e.getMessage());
        }
    }
}
