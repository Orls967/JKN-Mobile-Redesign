-- ============================================================
-- STORY-05 TASK-05-B: Queue Consistency & Reconciliation Job
-- Migration: V20260627_3__add_anomaly_log.sql
-- Epic: JMRO-114
-- ============================================================

CREATE TABLE IF NOT EXISTS queue_anomaly_log (
    id BIGSERIAL PRIMARY KEY,
    anomaly_type VARCHAR(50) NOT NULL,
    description TEXT,
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP
);
