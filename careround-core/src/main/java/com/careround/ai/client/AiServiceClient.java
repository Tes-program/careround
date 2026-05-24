package com.careround.ai.client;

import com.careround.ai.dto.ExtractedPrescription;
import com.careround.shared.exception.AiServiceException;
import com.careround.shared.exception.AiServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.io.IOException;
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

    public Flux<ServerSentEvent<String>> streamVoiceNote(MultipartFile audio, String patientId, String currentTime, String mode) {
        if (!isReady()) {
            throw new AiServiceUnavailableException("AI service is not ready");
        }

        try {
            byte[] audioBytes = audio.getBytes();
            String filename = audio.getOriginalFilename() != null
                    ? audio.getOriginalFilename()
                    : "recording.webm";
            MediaType audioContentType = audio.getContentType() != null
                    ? MediaType.parseMediaType(audio.getContentType())
                    : MediaType.parseMediaType("audio/webm");

            ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            log.info("action=AI_VOICE_NOTE_FORWARD filename={} contentType={} bytes={} signature={}",
                    filename, audioContentType, audioBytes.length, toHexPrefix(audioBytes, 16));

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("audio", audioResource).contentType(audioContentType).filename(filename);
            builder.part("patient_id", patientId);
            builder.part("current_time", currentTime != null ? currentTime : LocalDateTime.now(ZoneOffset.UTC).toString());
            builder.part("mode", mode);

            return aiWebClient.post()
                    .uri("/process-voice-note")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), (resp) ->
                            resp.bodyToMono(String.class).map(body ->
                                    new AiServiceException("AI service returned error " + resp.statusCode() + ": " + body)))
                    .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                    .doOnError(WebClientResponseException.ServiceUnavailable.class, e ->
                            log.warn("action=AI_STREAM status=503 patientId={}", patientId));
        } catch (AiServiceUnavailableException | AiServiceException e) {
            throw e;
        } catch (IOException e) {
            throw new AiServiceException("Failed to read audio bytes: " + e.getMessage());
        } catch (Exception e) {
            throw new AiServiceException("AI service call failed: " + e.getMessage());
        }
    }

    private String toHexPrefix(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        int max = Math.min(bytes.length, length);
        for (int i = 0; i < max; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
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
