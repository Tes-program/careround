package com.careround.patient.prescription;

import com.careround.patient.prescription.dto.PrescriptionResponse;
import com.careround.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Prescription lookup and lifecycle management")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/api/v1/patients/{patientId}/prescriptions")
    @Operation(summary = "List prescriptions", description = "Returns all prescriptions for a patient.")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPrescriptions(
            @PathVariable String patientId) {
        return ResponseEntity.ok(ApiResponse.ok(prescriptionService.getActivePrescriptions(patientId)));
    }

    @PutMapping("/api/v1/prescriptions/{prescriptionId}/discontinue")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    @Operation(summary = "Discontinue prescription", description = "Marks an active prescription as discontinued.")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> discontinue(
            @PathVariable String prescriptionId) {
        return ResponseEntity.ok(ApiResponse.ok("Prescription discontinued",
                prescriptionService.discontinue(prescriptionId)));
    }
}
