-- ============================================================
-- STORY-04 TASK-04-A: Database Index Optimization
-- Migration: V20260627__add_queue_indexes.sql
-- Epic: JMRO-111 | Story: JMRO-113
-- ============================================================
-- PENTING: Jalankan saat off-peak traffic.
-- CONCURRENTLY memungkinkan index dibuat tanpa locking tabel.
-- ============================================================

-- STEP 1: Tambah kolom baru pada queue_counter (jika belum ada)
-- Kolom ini dibutuhkan untuk mendukung query antrean per faskes/user
ALTER TABLE queue_counter ADD COLUMN IF NOT EXISTS faskes_id BIGINT;
ALTER TABLE queue_counter ADD COLUMN IF NOT EXISTS tanggal DATE;
ALTER TABLE queue_counter ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE queue_counter ADD COLUMN IF NOT EXISTS user_id VARCHAR(100);

-- STEP 2: Index Prioritas pada queue_counter

-- Index 1: Query antrean per faskes + tanggal + status (paling sering di-query)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_queue_counter_faskes_tanggal_status
ON queue_counter(faskes_id, tanggal, status);

-- Index 2: Query antrean per user + tanggal (cek apakah user sudah daftar)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_queue_counter_user_tanggal
ON queue_counter(user_id, tanggal);

-- Index 3: Partial index untuk status ACTIVE saja (covering index)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_queue_counter_active
ON queue_counter(faskes_id, tanggal) WHERE status = 'ACTIVE';

-- Index 4: Counter name lookup
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_queue_counter_name
ON queue_counter(counter_name);

-- STEP 3: Index pada queue_call_log

-- Index 5: Query ETA — Top 10 by calledAt DESC per queueCounterId
-- Ini adalah query paling kritis untuk kalkulasi Moving Average
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_call_log_counter_called_desc
ON queue_call_log(queue_counter_id, called_at DESC);
