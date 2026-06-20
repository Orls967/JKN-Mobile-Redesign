package com.jkn.backend.repository;

import com.jkn.backend.entity.QueueCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueCallLogRepository extends JpaRepository<QueueCallLog, Long> {
}
