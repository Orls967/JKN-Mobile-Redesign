# Runbook: Circuit Breaker Alert Response

## Deskripsi
Dokumen ini menjelaskan langkah-langkah yang harus diambil oleh tim operasional (Ops/DevOps) jika terjadi *alert* terkait **Circuit Breaker** pada sistem antrean JKN Mobile Redesign.

---

## 🚨 Alert: `CircuitBreakerOpen`

**Pemicu:** Circuit breaker untuk dependensi eksternal (misal: `bpjs_api`, `db_replica`) telah terbuka (*OPEN*) selama > 1 menit.  
**Dampak:** Aplikasi berjalan dalam *Degraded Mode*. Klien (Android) menerima HTTP 503 Service Unavailable saat mencoba mengakses fitur yang membutuhkan dependensi tersebut.

### Langkah Penanganan:
1. **Verifikasi Status Real-time:**
   Gunakan API ops untuk memeriksa tingkat kegagalan saat ini:
   ```bash
   curl -X GET https://<SERVER_URL>/ops/circuit-breaker/status \
        -H "Authorization: Bearer ops-secret-token-123"
   ```
2. **Cek Log Eksternal:**
   Periksa log (Kibana/Grafana) untuk melihat alasan spesifik kegagalan. Apakah itu *Timeout*, HTTP 500 dari BPJS, atau masalah konektivitas jaringan (DNS)?
3. **Konfirmasi Pemulihan Dependencies:**
   Hubungi tim terkait (misal: Tim Infrastruktur BPJS, Tim Database). Pastikan dependensi sudah stabil sebelum melakukan intervensi manual.
4. **Manual Recovery (Opsional):**
   Circuit breaker dirancang untuk memulihkan diri secara otomatis (menuju `HALF_OPEN` setelah 30 detik). Namun, jika sistem gagal memulihkan diri dengan benar, lakukan *force probe* (reset):
   ```bash
   curl -X POST https://<SERVER_URL>/ops/circuit-breaker/<dependency_name>/reset \
        -H "Authorization: Bearer ops-secret-token-123"
   ```

---

## 🚨 Alert: `CircuitBreakerFrequentlyOpening`

**Pemicu:** Circuit breaker membuka lebih dari 3 kali dalam rentang waktu 10 menit (Flapping).  
**Dampak:** Sistem terus-menerus *flap* antara sukses sesaat lalu gagal lagi.

### Langkah Penanganan:
1. **Stop Traffic Sementara (Jika Perlu):**
   Jika flap ini disebabkan oleh lonjakan *traffic* yang membunuh eksternal API perlahan, turunkan alokasi *rate limit* untuk endpoint terkait (via config map / vault).
2. **Eskalasi:**
   Kondisi ini menandakan eksternal API *unstable* (tidak konsisten mati, tapi sangat lemot). Segera eskalasikan ke L3 Support / Developer untuk meninjau *timeout matrix* (kemungkinan timeout BPJS terlalu sempit dibandingkan realita latensinya).
