package com.careround.ai;

import com.careround.ai.client.AiServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI", description = "AI voice note processing endpoints")
public class AiController {

    private final AiServiceClient aiServiceClient;

    @PostMapping(
            value = "/process-voice-note",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    @Operation(
            summary = "Process a voice note",
            description = """
                    Sends an audio recording to the AI service and streams the result back as Server-Sent Events (SSE).

                    **Note:** Swagger UI's 'Try it out' cannot render SSE streams. Use curl instead:
                    ```
                    curl -N -X POST http://localhost:8080/api/v1/ai/process-voice-note \\
                      -H "Authorization: Bearer <token>" \\
                      -F "audio=@recording.m4a" \\
                      -F "patientId=<uuid>" \\
                      -F "mode=ward_round"
                    ```

                    **Events emitted:**
                    - `transcription_complete` — Whisper finished; no data payload (UI progress signal)
                    - `processing_complete` — Full result: `rawTranscription`, `clinicalNote` (SOAP), `prescriptions[]`
                    - `done` — Stream closed normally
                    - `error` — A stage failed; `data.detail` contains the reason
                    """
    )
    public SseEmitter processVoiceNote(
            @Parameter(description = "Audio recording file (wav, mp3, m4a, webm, ogg, flac)")
            @RequestPart("audio") MultipartFile audio,
            @Parameter(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam("patient_id") String patientId,
            @Parameter(description = "ISO-8601 timestamp from the client")
            @RequestParam(value = "current_time", required = false) String currentTime,
            @Parameter(description = "Processing mode: 'ward_round' (transcription + SOAP note + prescriptions) or 'transcription_only'",
                    example = "ward_round")
            @RequestParam(value = "mode", defaultValue = "ward_round") String mode) throws IOException {

        if (audio.isEmpty() || audio.getSize() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded audio file is empty");
        }

        log.info("action=VOICE_NOTE_RECEIVED filename={} contentType={} bytes={} signature={}",
                audio.getOriginalFilename(),
                audio.getContentType(),
                audio.getSize(),
                toHexPrefix(audio.getBytes(), 16));

        SseEmitter emitter = new SseEmitter(300_000L);

        Disposable subscription = aiServiceClient.streamVoiceNote(audio, patientId, currentTime, mode)
                .subscribe(
                        event -> {
                            try {
                                SseEmitter.SseEventBuilder builder = SseEmitter.event();
                                if (event.event() != null) builder.name(event.event());
                                if (event.data() != null) builder.data(event.data());
                                emitter.send(builder);
                                if ("done".equals(event.event()) || "error".equals(event.event())) {
                                    emitter.complete();
                                }
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("action=AI_STREAM_ERROR patientId={} message={}", patientId, error.getMessage());
                            emitter.completeWithError(error);
                        },
                        emitter::complete
                );

        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(subscription::dispose);
        emitter.onError(t -> subscription.dispose());

        return emitter;
    }

    private String toHexPrefix(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        int max = Math.min(bytes.length, length);
        for (int i = 0; i < max; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
}
