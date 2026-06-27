-- ============================================================
-- STORY-01 TASK-01-A: Idempotency Log & Duplicate Prevention
-- Migration: V20260627_2__add_idempotency_log.sql
-- Epic: JMRO-91
-- ============================================================

CREATE TABLE IF NOT EXISTS idempotency_log (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

-- Index pada expires_at untuk mempercepat query cleanup job (TASK-01-C)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_idempotency_expires
ON idempotency_log(expires_at);
