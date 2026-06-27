package com.jkn.backend.repository;

import com.jkn.backend.entity.QueueAnomalyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueAnomalyLogRepository extends JpaRepository<QueueAnomalyLog, Long> {
}
