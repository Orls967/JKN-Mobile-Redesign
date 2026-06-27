# Connection Pool Configuration & Calibration (TASK-04-B)

Dokumen ini menjelaskan spesifikasi kalibrasi *Connection Pool* (HikariCP) untuk backend sistem antrean JKN Mobile Redesign.

## 1. Sizing Formula
Formula dasar yang disepakati untuk ukuran optimal pool database PostgreSQL:
```
pool_size = (core_count * 2) + effective_spindle_count
```
Berdasarkan spesifikasi server rata-rata (4 cores, SSD):
`Pool Size = (4 * 2) + 1 = 9`

Untuk memberikan ruang gerak (headroom) pada saat lonjakan pendaftaran, pool size dibulatkan menjadi **20**.

## 2. Parameter HikariCP yang Diterapkan
Tabel berikut menjelaskan *mapping* parameter dari spesifikasi awal (Go) ke Spring Boot HikariCP:

| Konsep / Tujuan | Parameter Spesifikasi (Go) | Konfigurasi Spring Boot (HikariCP) | Nilai | Penjelasan |
|---|---|---|---|---|
| **Max Aktif** | `db.SetMaxOpenConns(20)` | `maximum-pool-size` | `20` | Batas absolut koneksi concurrent ke database. |
| **Koneksi Idle** | `db.SetMaxIdleConns(10)` | `minimum-idle` | `10` | Jumlah koneksi yang dipertahankan terbuka meski traffic sedang sepi, untuk mencegah *cold start*. |
| **Max Lifetime** | `db.SetConnMaxLifetime(30m)` | `max-lifetime` | `1800000` (ms) | Setelah 30 menit, koneksi akan ditutup paksa secara *graceful* dan dibuat ulang untuk menghindari akumulasi *stale connection* di DB level. |
| **Idle Time** | `db.SetConnMaxIdleTime(10m)` | `idle-timeout` | `600000` (ms) | Koneksi idle di atas batas `minimum-idle` akan dibebaskan setelah 10 menit. |
| **Health Check** | (Implisit: ping test) | *HikariCP Internal* | (Otomatis) | HikariCP versi modern sudah menggunakan `Connection.isValid()` secara otomatis sebelum koneksi dipinjamkan, sangat ringan dibanding query *ping*. |
| **Wait Timeout** | (Baru) | `connection-timeout` | `5000` (ms) | Jika 20 koneksi penuh, request akan menunggu maksimal 5 detik sebelum dilempar exception, mencegah sistem menggantung tanpa batas (Eliminasi Timeout Sporadis). |
| **Leak Detection** | (Baru) | `leak-detection-threshold` | `30000` (ms) | Menangkap anomali *slow query* (>30s) yang menyandera koneksi, log akan muncul sebagai WARNING. |

## 3. Kalkulasi Multi-Instance (Horizontal Scaling)
Jika backend dideploy menggunakan *Load Balancer* dengan multiple instances, maka:
`Total Connections = instance_count × maximum-pool-size`

**WARNING:** Pastikan konfigurasi `max_connections` di sisi server PostgreSQL (biasanya di `postgresql.conf`) bernilai lebih besar dari total perhitungan di atas (rekomendasi: `Total Connections + 20` untuk *admin/maintenance*).

## 4. Metrik Monitoring
Parameter `register-mbeans: true` telah dihidupkan untuk mengekspos metrik HikariCP ke Prometheus (via Micrometer).
- **Active Connections:** `hikaricp_connections_active{pool="JknHikariPool"}`
- **Idle Connections:** `hikaricp_connections_idle{pool="JknHikariPool"}`
- **Wait/Pending Count:** `hikaricp_connections_pending{pool="JknHikariPool"}`

*Acceptance Criteria (AC): `wait_count` di pool metrics mendekati 0 pada normal traffic, menandakan kalibrasi ini sehat.*
