package com.careround.patient.nextofkin;

import com.careround.patient.nextofkin.dto.AddNextOfKinRequest;
import com.careround.patient.nextofkin.dto.NextOfKinResponse;
import com.careround.patient.nextofkin.dto.UpdateNextOfKinRequest;
import com.careround.patient.nextofkin.dto.UpdateNotificationConsentRequest;

import java.util.List;

public interface NextOfKinService {

    NextOfKinResponse addNextOfKin(String patientId, AddNextOfKinRequest request);

    NextOfKinResponse updateNextOfKin(String patientId, String nokId, UpdateNextOfKinRequest request);

    void removeNextOfKin(String patientId, String nokId);

    List<NextOfKinResponse> getNextOfKin(String patientId);

    NextOfKinResponse updateNotificationConsent(String patientId, String nokId,
            UpdateNotificationConsentRequest request);
}
