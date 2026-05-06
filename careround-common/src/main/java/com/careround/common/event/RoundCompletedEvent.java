package com.careround.common.event;

import java.util.List;

/** Published on: ROUND_COMPLETED → careround.round.completed */
public record RoundCompletedEvent(
        String roundId,
        String wardId,
        List<String> reviewedPatientIds,
        String hospitalId
) {}
