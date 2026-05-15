package com.careround.patient.round;

import com.careround.patient.round.dto.CreateRoundRequest;
import com.careround.patient.round.dto.PatientRoundReviewResponse;
import com.careround.patient.round.dto.ReviewPatientRequest;
import com.careround.patient.round.dto.RoundResponse;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Tag(name = "Rounds", description = "Clinical ward round creation, review, and completion")
public class RoundController {

    private final RoundService roundService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR')")
    @Operation(summary = "Create round", description = "Creates a clinical round for a ward and medical team.")
    public ResponseEntity<ApiResponse<RoundResponse>> createRound(@Valid @RequestBody CreateRoundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Round created", roundService.createRound(request)));
    }

    @PostMapping("/{roundId}/start")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR')")
    @Operation(summary = "Start round", description = "Starts a scheduled clinical round.")
    public ResponseEntity<ApiResponse<RoundResponse>> startRound(@PathVariable String roundId) {
        return ResponseEntity.ok(ApiResponse.ok("Round started", roundService.startRound(roundId)));
    }

    @PatchMapping("/{roundId}/patients/{patientId}")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'JUNIOR_DOCTOR')")
    @Operation(summary = "Review round patient", description = "Records a patient review during a clinical round.")
    public ResponseEntity<ApiResponse<PatientRoundReviewResponse>> reviewPatient(
            @PathVariable String roundId,
            @PathVariable String patientId,
            @Valid @RequestBody ReviewPatientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Patient reviewed",
                roundService.reviewPatient(roundId, patientId, request)));
    }

    @PostMapping("/{roundId}/complete")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR')")
    @Operation(summary = "Complete round", description = "Completes an active clinical round.")
    public ResponseEntity<ApiResponse<RoundResponse>> completeRound(@PathVariable String roundId) {
        return ResponseEntity.ok(ApiResponse.ok("Round completed", roundService.completeRound(roundId)));
    }

    @PostMapping("/{roundId}/cancel")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR')")
    @Operation(summary = "Cancel round", description = "Cancels a scheduled round that was created in error.")
    public ResponseEntity<ApiResponse<RoundResponse>> cancelRound(
            @Parameter(description = "Round id") @PathVariable String roundId) {
        return ResponseEntity.ok(ApiResponse.ok("Round cancelled", roundService.cancelRound(roundId)));
    }

    @GetMapping
    @Operation(summary = "List rounds", description = "Returns clinical rounds for a ward and medical team.")
    public ResponseEntity<ApiResponse<List<RoundResponse>>> getRounds(
            @RequestParam String wardId,
            @RequestParam String teamId) {
        return ResponseEntity.ok(ApiResponse.ok(roundService.getRounds(wardId, teamId)));
    }

    @GetMapping("/{roundId}/reviews")
    @Operation(summary = "List round reviews", description = "Returns patient reviews recorded for a clinical round.")
    public ResponseEntity<ApiResponse<List<PatientRoundReviewResponse>>> getReviews(@PathVariable String roundId) {
        return ResponseEntity.ok(ApiResponse.ok(roundService.getReviews(roundId)));
    }
}
