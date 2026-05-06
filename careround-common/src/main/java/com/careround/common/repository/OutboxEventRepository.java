package com.careround.common.repository;

import com.careround.common.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    /** Fetch unpublished events in insertion order for the OutboxPoller */
    @Query("SELECT e FROM OutboxEvent e WHERE e.published = false ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnpublishedOrderByCreatedAt();
}
