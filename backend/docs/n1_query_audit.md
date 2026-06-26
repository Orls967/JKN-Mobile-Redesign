# N+1 Query Audit & Remediation (TASK-04-C)

Dokumen ini mencatat hasil identifikasi N+1 Query pattern pada endpoint aplikasi beserta strategi remediasi yang telah dilakukan sesuai spesifikasi Story 4 (JMRO-113).

## 1. Audit Endpoint

### Endpoint: `GET /api/queues` (Daftar Antrean)
- **Status:** Sehat ✅
- **Query Count:** 1
- **Keterangan:** Method `getAllQueues()` memanggil `queueCounterRepository.findAll()` yang menghasilkan 1 single query (karena tidak ada lazy-loaded EAGER relation yang memicu N+1).

### Endpoint: `GET /api/queues/{id}` (Detail Antrean)
- **Status:** Sehat ✅
- **Query Count:** 1
- **Keterangan:** Menggunakan `findById()` yang juga menghasilkan 1 single query.

### Endpoint: `PUT /api/queues/{id}/next` (Panggil Nomor Selanjutnya)
- **Status:** Sehat (By Design) ⚠️
- **Query Count:** 3 (Dengan batching)
- **Keterangan:** Method ini melakukan `findByIdForUpdate` (1x select for update), `save(counter)` (1x update), dan `save(callLog)` (1x insert). Jumlah ini proporsional dan transaksional, tidak masuk kategori N+1. Penggunaan `jdbc.batch_size: 20` di konfigurasi mengefisienkan proses update/insert-nya.

### Endpoint: `GET /api/queues/{id}/eta` (Kalkulasi ETA)
- **Status:** N+1 Pattern Terdeteksi ❌
- **Query Count Sebelum Optimasi:** 4
- **Keterangan:** Endpoint ini melakukan kalkulasi ETA dan mengambil *average service seconds*. 
  - `queueEtaService.calculateEtaMinutes()` melakukan 1x `findById()` dan 1x `findTop10ByQueueCounterId...` (plus 1x update DB jika average dihitung ulang).
  - `queueEtaService.getAverageServiceSeconds()` melakukan **1x `findById()` ulang** padahal datanya identik dengan method pertama.

## 2. Remediasi `GET /api/queues/{id}/eta`

### Strategi Perbaikan
Masalah N+1 di atas terjadi pada ranah pemanggilan service yang redundant. Perbaikannya adalah **Method Consolidation**.

### Detail Kode
1. Dibuat method baru `calculateEta()` di `QueueEtaService` yang mengembalikan `EtaResponse` (merangkum *minutes* dan *seconds*).
2. Method ini hanya memanggil `findById()` dan `findTop10()` masing-masing 1x.
3. Controller diupdate untuk menggunakan method terpadu ini.

### Hasil Akhir
- **Query Count Sesudah Optimasi:** 2 (Reduksi 50%)
- Method lama di-mark `@Deprecated` untuk mencegah penggunaan ulang di masa depan yang bisa memicu regresi performa.
