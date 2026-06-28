# Performance Baseline (Before Epic 4 Sprint)

## Overview
Dokumen ini merangkum status kinerja sistem pendaftaran antrean JKN sebelum implementasi fitur-fitur ketahanan pada Epic 4 (Sprint 1-7). Data ini diambil dari insiden historis dan log server selama jam sibuk di production bulan sebelumnya.

## Sistem Kondisi Awal (Pre-Sprint)
- **Rate Limiter**: Tidak ada.
- **Idempotency**: Tidak ada (berpotensi duplikasi data jika jaringan tidak stabil).
- **Circuit Breaker**: Tidak ada (cascade failure jika BPJS eksternal mati).
- **Koneksi Database**: Tidak optimal (terdapat *N+1 query problem* dan koneksi yang hang).

## Metrik Performa Historis (Estimasi Beban Puncak)

| Skenario Historis | Hasil Observasi (Before) | Permasalahan Utama |
| --- | --- | --- |
| **Normal Load (100 Concurrent Users)** | Berhasil (P95: ~800ms) | Kinerja relatif aman jika tidak ada *spike*. |
| **Peak Traffic (500 Concurrent Users)** | Degraded (P95: **4000ms+**) | Thread-pool kelelahan, *database connection pool exhausted*. |
| **Burst Traffic (Ribuan klik berbarengan)** | **Server Crash (HTTP 502/504)** | Tidak ada proteksi *rate-limiting*. Server kehabisan RAM. |
| **Koneksi Terputus Sesaat (Retry user)** | **15% Duplicate Data** | Sistem memproses pendaftaran ganda karena tidak ada mekanisme *Idempotency-Key*. |
| **Dependency BPJS Down** | **100% Cascade Failure** | Antrean lumpuh total karena thread tertahan (*blocked*) menunggu balasan BPJS hingga *timeout* default TCP/IP. |

## Target (Post-Sprint Baseline)
Dengan diimplementasikannya Epic 4, kami menargetkan:
1. P95 di bawah 1 detik untuk *Normal Load*.
2. P95 di bawah 2 detik untuk *Peak Traffic*.
3. Tidak ada server *crash* pada saat *Burst* (dihalau HTTP 429).
4. 0% data duplikat pada percobaan ulang klien.
5. Sistem tetap merespons (via 503 + *Retry-After*) saat BPJS down (Circuit Breaker terbuka).
