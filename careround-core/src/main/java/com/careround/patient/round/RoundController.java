package com.careround.patient.round;

import com.careround.patient.round.dto.CreateRoundRequest;
import com.careround.patient.round.dto.PatientRoundReviewResponse;
import com.careround.patient.round.dto.ReviewPatientRequest;
import com.careround.patient.round.dto.RoundResponse;
import com.careround.shared.dto.ApiResponse;
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
public class RoundController {

    private final RoundService roundService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<RoundResponse>> createRound(@Valid @RequestBody CreateRoundRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Round created", roundService.createRound(request)));
    }

    @PostMapping("/{roundId}/start")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<RoundResponse>> startRound(@PathVariable String roundId) {
        return ResponseEntity.ok(ApiResponse.ok("Round started", roundService.startRound(roundId)));
    }

    @PatchMapping("/{roundId}/patients/{patientId}")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR', 'JUNIOR_DOCTOR')")
    public ResponseEntity<ApiResponse<PatientRoundReviewResponse>> reviewPatient(
            @PathVariable String roundId,
            @PathVariable String patientId,
            @Valid @RequestBody ReviewPatientRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Patient reviewed",
                roundService.reviewPatient(roundId, patientId, request)));
    }

    @PostMapping("/{roundId}/complete")
    @PreAuthorize("hasAnyRole('CONSULTANT', 'REGISTRAR')")
    public ResponseEntity<ApiResponse<RoundResponse>> completeRound(@PathVariable String roundId) {
        return ResponseEntity.ok(ApiResponse.ok("Round completed", roundService.completeRound(roundId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoundResponse>>> getRounds(
            @RequestParam String wardId,
            @RequestParam String teamId) {
        return ResponseEntity.ok(ApiResponse.ok(roundService.getRounds(wardId, teamId)));
    }

    @GetMapping("/{roundId}/reviews")
    public ResponseEntity<ApiResponse<List<PatientRoundReviewResponse>>> getReviews(@PathVariable String roundId) {
        return ResponseEntity.ok(ApiResponse.ok(roundService.getReviews(roundId)));
    }
}
