package com.careround.hospital.supervisor.dto;

import java.time.LocalDateTime;

public record HourlyTaskCount(LocalDateTime hour, int taskCount) {}
