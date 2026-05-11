package com.careround.patient.escalation;

import com.careround.patient.enums.EscalationSeverity;
import com.careround.patient.escalation.dto.AcknowledgeEscalationRequest;
import com.careround.patient.escalation.dto.CreateEscalationRequest;
import com.careround.patient.escalation.dto.EscalationResponse;
import com.careround.patient.escalation.dto.ResolveEscalationRequest;

import java.util.List;

public interface EscalationService {

    EscalationResponse triggerSystemEscalation(String patientId, EscalationSeverity severity, String notes);

    EscalationResponse createManualEscalation(CreateEscalationRequest request);

    EscalationResponse acknowledgeEscalation(String escalationId, AcknowledgeEscalationRequest request);

    EscalationResponse resolveEscalation(String escalationId, ResolveEscalationRequest request);

    List<EscalationResponse> getOpenEscalations(String wardId);

    List<EscalationResponse> getEscalationsByPatient(String patientId);
}
