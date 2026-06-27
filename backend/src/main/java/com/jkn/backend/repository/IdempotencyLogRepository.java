package com.jkn.backend.repository;

import com.jkn.backend.entity.IdempotencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface IdempotencyLogRepository extends JpaRepository<IdempotencyLog, String> {

    @Modifying
    @Query(value = "DELETE FROM idempotency_log WHERE idempotency_key IN (SELECT idempotency_key FROM idempotency_log WHERE expires_at < :now LIMIT :batchSize)", nativeQuery = true)
    int deleteExpiredLogsBatch(@Param("now") LocalDateTime now, @Param("batchSize") int batchSize);
}
