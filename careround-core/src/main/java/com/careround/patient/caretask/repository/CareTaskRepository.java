package com.careround.patient.caretask.repository;

import com.careround.patient.caretask.entity.CareTask;
import com.careround.shared.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CareTaskRepository extends JpaRepository<CareTask, String> {
    List<CareTask> findByStatusAndWindowEndBefore(TaskStatus status, LocalDateTime now);
}
