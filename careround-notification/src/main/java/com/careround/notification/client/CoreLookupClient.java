package com.careround.notification.client;

import java.util.List;
import java.util.Optional;

public interface CoreLookupClient {
    Optional<String> findWardSupervisorId(String wardId);

    List<NextOfKinContact> findNextOfKin(String patientId);
}
