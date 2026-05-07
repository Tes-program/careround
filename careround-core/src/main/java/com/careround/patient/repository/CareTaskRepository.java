package com.careround.patient.repository;

import com.careround.patient.entity.CareTask;
import com.careround.patient.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CareTaskRepository extends JpaRepository<CareTask, String> {

    List<CareTask> findAllByHospitalIdAndWardIdAndStatusOrderByWindowEnd(
            String hospitalId, String wardId, TaskStatus status);

    List<CareTask> findAllByPatientId(String patientId);

    List<CareTask> findAllByStatusInAndWindowEndBefore(List<TaskStatus> statuses, LocalDateTime now);
}
