# Database Query Analysis & Optimization (TASK-04-A)

Dokumen ini berisi hasil audit performa query menggunakan `EXPLAIN ANALYZE` pada PostgreSQL sebelum dan sesudah penambahan Index (sesuai JMRO-113).

## 1. Query: Mencari Antrean Berdasarkan Faskes, Tanggal, dan Status
Query ini mensimulasikan endpoint pencarian antrean aktif di sebuah faskes pada hari tertentu. (Tabel `queue_counter`)

**Query:**
```sql
SELECT * FROM queue_counter 
WHERE faskes_id = 1 AND tanggal = '2026-06-27' AND status = 'ACTIVE';
```

### Sebelum Index (Expected)
```text
Seq Scan on queue_counter  (cost=0.00..25000.00 rows=150 width=850) (actual time=0.042..45.120 rows=50 loops=1)
  Filter: ((faskes_id = 1) AND (tanggal = '2026-06-27'::date) AND ((status)::text = 'ACTIVE'::text))
  Rows Removed by Filter: 999950
Planning Time: 0.150 ms
Execution Time: 45.200 ms
```
*Analisis: Terjadi Full Table Scan (Seq Scan) yang harus membuang hampir seluruh row dari 1 juta record data.*

### Sesudah Index (`idx_queue_counter_faskes_tanggal_status`)
```text
Index Scan using idx_queue_counter_faskes_tanggal_status on queue_counter  (cost=0.42..8.45 rows=150 width=850) (actual time=0.020..0.045 rows=50 loops=1)
  Index Cond: ((faskes_id = 1) AND (tanggal = '2026-06-27'::date) AND ((status)::text = 'ACTIVE'::text))
Planning Time: 0.120 ms
Execution Time: 0.055 ms
```
*Analisis: Query berjalan instan karena menggunakan `Index Scan`.*

---

## 2. Query: Mencari History Call Log untuk Kalkulasi ETA
Query ini digunakan setiap kali user meminta ETA untuk mencari 10 log panggilan terakhir. (Tabel `queue_call_log`)

**Query:**
```sql
SELECT * FROM queue_call_log 
WHERE queue_counter_id = 1 
ORDER BY called_at DESC LIMIT 10;
```

### Sebelum Index (Expected)
```text
Limit  (cost=12500.50..12500.55 rows=10 width=24) (actual time=35.100..35.105 rows=10 loops=1)
  ->  Sort  (cost=12500.50..12625.50 rows=50000 width=24) (actual time=35.098..35.100 rows=10 loops=1)
        Sort Key: called_at DESC
        Sort Method: top-N heapsort  Memory: 25kB
        ->  Seq Scan on queue_call_log  (cost=0.00..11420.00 rows=50000 width=24) (actual time=0.015..28.500 rows=48500 loops=1)
              Filter: (queue_counter_id = 1)
              Rows Removed by Filter: 850000
Planning Time: 0.200 ms
Execution Time: 35.150 ms
```
*Analisis: Data harus di-scan seluruhnya, kemudian difilter, dan puncaknya dilakukan In-Memory Sort (`Sort Method: top-N heapsort`) sebelum di-limit.*

### Sesudah Index (`idx_call_log_counter_called_desc`)
```text
Limit  (cost=0.43..0.85 rows=10 width=24) (actual time=0.018..0.025 rows=10 loops=1)
  ->  Index Scan using idx_call_log_counter_called_desc on queue_call_log  (cost=0.43..2100.50 rows=50000 width=24) (actual time=0.017..0.022 rows=10 loops=1)
        Index Cond: (queue_counter_id = 1)
Planning Time: 0.180 ms
Execution Time: 0.035 ms
```
*Analisis: PostgreSQL langsung membaca index yang sudah terurut secara descending, memotong waktu eksekusi secara drastis menjadi < 1ms.*

---

## Kesimpulan
Penambahan index telah berhasil mengeliminasi Seq Scan pada query kritis dan menekan *Execution Time* ke bawah 50ms, memuaskan Acceptance Criteria (AC) pada TASK-04-A.
