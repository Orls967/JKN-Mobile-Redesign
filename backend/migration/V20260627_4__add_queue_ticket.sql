-- ============================================================
-- STORY-05 TASK-05-A: Queue Consistency & Transaction Integrity
-- Migration: V20260627_4__add_queue_ticket.sql
-- Epic: JMRO-114
-- ============================================================

CREATE TABLE IF NOT EXISTS queue_ticket (
    id BIGSERIAL PRIMARY KEY,
    queue_counter_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    faskes_id BIGINT NOT NULL,
    tanggal DATE NOT NULL,
    ticket_number INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_queue_ticket_counter FOREIGN KEY (queue_counter_id) REFERENCES queue_counter(id)
);

-- Index for reconciliation job efficiency
CREATE INDEX IF NOT EXISTS idx_queue_ticket_counter ON queue_ticket(queue_counter_id);
