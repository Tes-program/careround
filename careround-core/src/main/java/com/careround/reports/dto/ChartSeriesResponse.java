package com.careround.reports.dto;

import java.util.List;

public record ChartSeriesResponse(
        List<String> labels,
        List<Long> values
) {}
