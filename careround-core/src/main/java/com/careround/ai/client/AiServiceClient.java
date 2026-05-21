package com.careround.ai.client;

import com.careround.ai.dto.ProcessVoiceNoteResponse;
import com.careround.shared.exception.AiServiceException;
import com.careround.shared.exception.AiServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {

    private final RestClient aiRestClient;

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

    public ProcessVoiceNoteResponse processVoiceNote(MultipartFile audio, String patientId,
                                                      LocalDateTime admissionDate) {
        if (!isReady()) {
            throw new AiServiceUnavailableException("AI service is not ready");
        }

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("audio", audio.getResource());
            body.add("patientId", patientId);
            body.add("admissionDate", admissionDate.toString());

            return aiRestClient.post()
                    .uri("/process-voice-note")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(),
                            (req, resp) -> {
                                throw new AiServiceException(
                                        "AI service returned error: " + resp.getStatusCode());
                            })
                    .body(ProcessVoiceNoteResponse.class);
        } catch (AiServiceUnavailableException | AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("AI service call failed: " + e.getMessage());
        }
    }
}
