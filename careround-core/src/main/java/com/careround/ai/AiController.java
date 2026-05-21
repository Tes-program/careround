package com.careround.ai;

import com.careround.ai.client.AiServiceClient;
import com.careround.ai.dto.ProcessVoiceNoteResponse;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI voice note processing endpoints")
public class AiController {

    private final AiServiceClient aiServiceClient;

    @PostMapping(value = "/process-voice-note", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE')")
    @Operation(
            summary = "Process a voice note",
            description = "Sends an audio file to the AI service for transcription and clinical note extraction."
    )
    public ResponseEntity<ApiResponse<ProcessVoiceNoteResponse>> processVoiceNote(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("patientId") String patientId) {
        ProcessVoiceNoteResponse response = aiServiceClient.processVoiceNote(
                audio, patientId, LocalDateTime.now(ZoneOffset.UTC));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
