package com.careround.hospital.supervisor.dto;

/**
 * One hourly data-point in the supervisor task chart.
 * {@code hour} is an ISO-8601 UTC datetime string for the start of the hour,
 * e.g. "2026-05-20T10:00:00Z".
 */
public record HourlyTaskCount(String hour, int taskCount) {}
