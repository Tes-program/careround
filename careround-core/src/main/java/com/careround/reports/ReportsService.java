package com.careround.reports;

import com.careround.reports.dto.ChartSeriesResponse;
import com.careround.reports.dto.RoundHistoryItemResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportsService {

    ChartSeriesResponse taskCompletion(String hospitalId, String wardId, LocalDate from, LocalDate to);

    ChartSeriesResponse overdueTasks(String hospitalId, String wardId, LocalDate from, LocalDate to);

    ChartSeriesResponse patientFlow(String hospitalId, String wardId, LocalDate from, LocalDate to);

    List<RoundHistoryItemResponse> roundHistory(String hospitalId, String wardId, LocalDate from, LocalDate to);
}
