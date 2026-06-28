# DOKUMEN UAT TERPADU — Mobile JKN Redesign & Real-Time Queue System

| | |
|:---|:---|
| **Nama Proyek** | Mobile JKN Redesign & Real-Time Queue System |
| **Referensi Perjanjian** | JMRO/UAT/06/2026 |
| **Perihal** | User Acceptance Testing (UAT) — Functional & Non-Functional |
| **Waktu Pelaksanaan** | 08.00 – 12.00, 30 Juni 2026 |
| **Tempat** | Ruang Lab Komputer / Google Meet |
| **Cakupan Epic** | Epic 2 (WebSocket), Epic 3 (Real-Time UX), Epic 4 (Backend Resilience) |

---

## KATEGORI A — Functional & Real-Time Flow

> Menguji fitur yang kasat mata (terlihat langsung di UI). Membuktikan implementasi Epic 2 & 3 (WebSocket + Real-Time Queue).

### [JMRO-0001] Story 2.2 — Operator Memanggil Nomor Antrean (NEXT)

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC0001 | Operator menekan tombol NEXT pada kondisi antrean normal | **Resolves State Lag** — Membuktikan operator dapat memajukan antrean secara real-time; fondasi dari seluruh alur WebSocket. | 1. Buka Operator Queue Screen 2. Pastikan `currentNumber` = A-005 3. Tekan tombol NEXT | `currentNumber` berubah menjadi A-006 di layar operator dalam < 3 detik | — | Passed |
| TC0002 | Nomor antrean di layar pasien ikut diperbarui setelah operator tekan NEXT | **Resolves Play Store Issue: "Udah ambil antrean ko no antreannya gak muncul-muncul"** — Membuktikan sinkronisasi WebSocket multi-device < 1 detik (Epic 2 & 3). | 1. Buka Patient Queue Screen di perangkat kedua 2. Operator tekan NEXT 3. Amati perubahan pada Patient Screen | Current Number di Patient Screen berubah mengikuti operator secara otomatis tanpa refresh manual | — | Passed |
| TC0003 | Tombol NEXT tidak tersedia pada layar pasien | **RBAC UI Enforcement** — Mencegah pasien secara tidak sengaja atau sengaja mengakses fungsi operator; keamanan tampilan (Story 1.3). | 1. Login sebagai pasien 2. Buka Patient Queue Screen 3. Amati apakah tombol NEXT tersedia | Tombol NEXT tidak muncul di layar pasien; hanya tersedia di Operator Screen | — | Passed |

### [JMRO-0002 / ABC-0002] Story 2.3 — Live Queue Indicator Real-Time

> **Precondition:** Pasien telah berhasil login dengan valid credential dan dapat mengakses halaman home.

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC0004 | Live Queue Indicator menampilkan tiga nilai sekaligus (My Ticket, Current, Next) | **Resolves Play Store Issue: "No antrean gak muncul-muncul"** — Pasien langsung melihat posisinya dalam antrean tanpa ambiguitas; mendukung UAT-A1. | 1. Login sebagai pasien dengan My Ticket = A-010 2. Buka Patient Queue Screen 3. Amati tampilan indikator | Layar menampilkan: My Ticket (A-010), Current Number (nomor aktif), dan Next Number (nomor berikutnya) secara bersamaan | — | Passed |
| TC0005 | Live Queue Indicator diperbarui otomatis ketika operator tekan NEXT | **Resolves State Lag** — Mengeliminasi kebutuhan pull-to-refresh; pasien tidak perlu aksi manual untuk melihat pembaruan antrean (Epic 3). | 1. Pasien membuka Patient Screen 2. Operator tekan NEXT beberapa kali 3. Amati apakah nilai berubah tanpa refresh | Current Number dan Next Number berubah secara otomatis tanpa aksi apapun dari pasien | — | Passed |
| TC0006 | Connection State Banner menampilkan status `Connected` saat WebSocket aktif | **Transparansi Koneksi** — Pengguna mendapat feedback visual bahwa sistem berjalan normal; mengurangi kebingungan dan tiket support palsu. | 1. Buka Patient Screen dengan koneksi internet stabil 2. Amati banner koneksi di bagian atas layar | Banner menampilkan status 'Connected' dengan warna hijau atau indikator positif | — | Passed |
| TC0007 | Connection State Banner menampilkan `Reconnecting` saat koneksi terputus lalu kembali `Connected` | **Graceful Degradation UX** — Pasien tahu sistem sedang reconnect (bukan crash); mencegah mereka menutup aplikasi dan kehilangan posisi antrean. | 1. Buka Patient Screen 2. Matikan WiFi/data perangkat selama 5 detik 3. Amati banner koneksi | Banner segera berubah menjadi 'Reconnecting'; setelah koneksi pulih, banner kembali ke 'Connected' | — | Passed |

### [JMRO-0003] Story 3.1 — Proximity Status Antrean

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC0008 | Proximity Status berubah menjadi `Bersiap` saat sisa antrean ≤ 3 | **Resolves No-Show Risk** — Pasien mendapat peringatan visual untuk segera bersiap, mengurangi waktu tunggu operator akibat pasien tidak siap dipanggil (UAT-A3). | 1. Set My Ticket pasien = A-008 2. Operator tekan NEXT hingga `currentNumber` = A-005 3. Amati perubahan status dan warna | Status berubah menjadi 'Bersiap' disertai perubahan warna indikator (bukan warna default) | — | Passed |
| TC0009 | Proximity Status berubah menjadi `Sedang Dipanggil` saat nomor pasien aktif | **Real-Time Call Notification** — Pasien mendapat konfirmasi visual instan bahwa nomor mereka sedang dipanggil; mengeliminasi ketergantungan pada pengumuman audio. | 1. Set My Ticket pasien = A-007 2. Operator tekan NEXT hingga `currentNumber` = A-007 | Status berubah menjadi 'Sedang Dipanggil' dengan warna hijau | — | Passed |
| TC0010 | Proximity Status berubah menjadi `Sudah Terlewati` saat nomor pasien dilewati | **Missed Queue Awareness** — Pasien tahu mereka terlewati dan perlu melapor ke loket, mencegah kebingungan berkepanjangan di ruang tunggu. | 1. Set My Ticket pasien = A-005 2. Operator tekan NEXT hingga `currentNumber` = A-007 | Status berubah menjadi 'Sudah Terlewati' dengan warna merah | — | Passed |

### [JMRO-0004] Story 3.1 — Smart Proximity Notification (Android Local)

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC0011 | Notifikasi lokal muncul saat kondisi proximity terpenuhi (sisa ≤ 3 nomor) | **Resolves No-Show / Pasien Terlambat Kembali** — Notifikasi push lokal memastikan pasien yang meninggalkan ruang tunggu tetap terinformasi; mendukung UAT-A3. | 1. Pastikan izin notifikasi diberikan di perangkat 2. Set My Ticket = A-010, `currentNumber` = A-006 3. Operator tekan NEXT sekali (current = A-007) | Notifikasi Android lokal muncul dengan pesan bahwa giliran pasien tinggal dekat | — | Passed |
| TC0012 | Notifikasi tidak berulang untuk setiap tekan NEXT setelah ambang batas terpenuhi | **Notification UX Quality** — Mencegah spam notifikasi yang mengganggu pengguna dan merusak kepercayaan terhadap aplikasi. | 1. Kondisi proximity sudah terpenuhi (sisa ≤ 3) 2. Operator tekan NEXT beberapa kali lagi | Notifikasi hanya muncul sekali pada saat ambang batas pertama kali terpenuhi, tidak terus berulang | — | Passed |
| TC0013 | Notifikasi tidak muncul saat sisa antrean masih > 3 | **False Alarm Prevention** — Mencegah notifikasi prematur yang membuat pasien terburu-buru sebelum waktunya. | 1. Set My Ticket = A-020, `currentNumber` = A-001 2. Operator tekan NEXT beberapa kali (masih > 3 sisa) | Tidak ada notifikasi yang muncul selama sisa antrean masih lebih dari 3 | — | Passed |

### [JMRO-0005] Story 3.2 — Estimasi Waktu Tunggu (ETA) Real-Time

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC0014 | ETA ditampilkan di Patient Screen berdasarkan rata-rata waktu layanan aktual | **Informed Waiting Experience** — Pasien dapat memperkirakan kapan harus kembali; mengurangi kepadatan di ruang tunggu dan pertanyaan kepada petugas loket. | 1. Buka Patient Screen 2. Operator tekan NEXT beberapa kali dengan jeda waktu berbeda 3. Amati nilai ETA yang ditampilkan | ETA tampil dalam satuan menit dan berubah dinamis sesuai rata-rata kecepatan layanan aktual | — | Passed |
| TC0015 | ETA diperbarui otomatis setelah setiap panggilan NEXT | **Real-Time ETA Accuracy** — ETA yang selalu mutakhir lebih akurat dari estimasi statis; membuktikan pipeline Epic 3 end-to-end. | 1. Catat nilai ETA awal 2. Operator tekan NEXT 3. Amati apakah ETA berubah | Nilai ETA diperbarui secara otomatis tanpa refresh manual setelah setiap pemanggilan nomor | — | Passed |

### [JMRO-0006] Story 1.3 — Role-Based Access Control (RBAC)

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC0016 | Login dengan akun operator berhasil mengakses Operator Screen | **Access Control Validation** — Memastikan petugas berwenang dapat menggunakan fungsi manajemen antrean tanpa hambatan. | 1. Input email: `operator@seed.com` 2. Input password yang valid 3. Tap login | Diarahkan ke Operator Queue Screen; tombol NEXT tersedia | — | Passed |
| TC0017 | Login dengan akun pasien tidak dapat mengakses Operator Screen | **Privilege Escalation Prevention** — Mencegah pasien mengambil alih kontrol antrean; kritis untuk integritas sistem antrian publik. | 1. Login dengan akun pasien 2. Coba navigasi ke Operator Screen | Akun pasien tidak dapat mengakses Operator Screen; hanya bisa melihat Patient Queue Screen | — | Passed |

### [JMRO-0007] Story 1.4 — Navigasi & Tampilan Shell Aplikasi

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC0018 | Bottom Navigation berfungsi dan berpindah antar layar tanpa crash | **App Stability Baseline** — Navigasi dasar adalah fondasi UX; crash pada navigasi adalah blocker utama yang meningkatkan bad review di Play Store. | 1. Buka aplikasi sebagai pasien 2. Tap ikon di Bottom Navigation (Home, Antrean, Profil, FAQ) | Navigasi berpindah ke layar yang sesuai; tidak ada crash saat berpindah | — | Passed |
| TC0019 | Home Screen menampilkan menu Antrean Online melalui Bottom Sheet Picker | **Service Discovery UX** — Pasien dapat menemukan dan memilih jenis layanan (Tingkat Pertama, Rujukan) secara intuitif dari Home Screen. | 1. Buka Home Screen 2. Tap menu Antrean Online | Bottom Sheet muncul dengan pilihan layanan (Tingkat Pertama, Rujukan, dll.) | — | Passed |
| TC0020 | Kartu Peserta Screen menampilkan data KIS Digital | **Digital Identity Display** — Menggantikan kartu fisik; pasien dapat menunjukkan bukti kepesertaan BPJS langsung dari aplikasi. | 1. Buka halaman Kartu Peserta 2. Amati tampilan kartu | Tampil kartu KIS digital dengan data peserta (dapat berupa data dummy) | — | Passed |

### [JMRO-0008] Non-Functional — Stabilitas & Observabilitas (Firebase Crashlytics)

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC0021 | Crash test manual tercatat di Firebase Crashlytics Dashboard | **Proactive Bug Detection** — Tim dapat mendeteksi dan memperbaiki crash sebelum dilaporkan oleh pengguna melalui review negatif Play Store. | 1. Jalankan test crash: `FirebaseCrashlytics.getInstance().log(...)` 2. Periksa Firebase Console setelah beberapa menit | Laporan crash muncul di Firebase Crashlytics Dashboard disertai stack trace | — | Passed |
| TC0022 | Kegagalan koneksi WebSocket tercatat sebagai non-fatal exception | **WebSocket Error Observability** — Tim mendapat visibilitas penuh terhadap kegagalan WebSocket di lapangan; memungkinkan diagnosis akar masalah koneksi real-time. | 1. Paksa koneksi WebSocket gagal (matikan server sementara) 2. Buka Patient Screen 3. Periksa Firebase Console | Non-fatal exception terkait WebSocket tercatat di Crashlytics dengan informasi error yang cukup | — | Passed |

---

## KATEGORI B — Non-Functional & Backend Resilience (Epic 4)

> Membuktikan bahwa *backend* mampu menangani anomali dan menyelesaikan akar masalah keluhan Play Store yang tidak kasat mata di UI. Skenario ini **wajib didemonstrasikan** kepada penguji sebagai bukti teknis.

| TCID | Test Cases | Business Value / Target Issue | Test Steps | Expected Result | Actual Result | Status |
|:---|:---|:---|:---|:---|:---|:---|
| TC-B01 | **[Idempotency]** Pasien pada koneksi lambat menekan tombol "Daftar Antrean" 5 kali berturut-turut; sistem hanya menerbitkan 1 nomor tiket | **Resolves Play Store Issue: "Daftar gak masuk2 disuruh coba lagi… ternyata sdh dpt antrean" (Ghost Data / Double Booking)** — Membuktikan Epic 4: Idempotency Key & Distributed Lock berhasil mencegah duplikasi data secara absolut. | 1. Aktifkan *network throttling* (simulasi koneksi 2G/lambat) di perangkat pasien menggunakan developer options atau proxy tool 2. Navigasi ke halaman Daftar Antrean 3. Tekan tombol "Daftar" sebanyak 5 kali berturut-turut secepat mungkin 4. Tunggu hingga seluruh request selesai diproses 5. Periksa jumlah tiket yang diterbitkan untuk pasien tersebut di database / UI operator | Pasien hanya menerima **tepat 1 nomor antrean**; tidak ada tiket duplikat di sistem. Server merespons request ke-2 s.d. ke-5 dengan hasil yang identik dengan request pertama (idempotent), bukan menerbitkan tiket baru. | — | — |
| TC-B02 | **[Server-Side Time Authority]** Pasien mengubah tanggal/waktu perangkat Android ke "Besok" secara manual lalu mendaftar antrean | **Resolves Play Store Issue: "Ambil untuk besok… eh tiba2 selesai sendiri hari ini / dibatalin" (Timezone & Clock Exploit)** — Membuktikan Epic 4: Server-Side Time Authority berhasil mengabaikan waktu klien dan mengunci tiket berdasarkan waktu server absolut. | 1. Masuk ke **Pengaturan → Tanggal & Waktu** di perangkat Android pasien 2. Nonaktifkan "Setel otomatis" dan ubah tanggal ke H+1 (besok) 3. Buka aplikasi dan navigasi ke halaman Daftar Antrean 4. Tekan tombol "Daftar Antrean" 5. Amati tanggal/sesi antrean yang tercetak pada tiket yang diterima | Tiket yang diterbitkan menggunakan **tanggal dan sesi hari ini** sesuai jam server (bukan jam perangkat). Sistem menolak atau mengabaikan timestamp dari klien dan mengunci jadwal berdasarkan Server Time Authority. | — | — |
| TC-B03 | **[Circuit Breaker]** Penguji memutus koneksi ke server BPJS (simulasi dependency down); pasien membuka aplikasi | **Resolves Play Store Issue: "Error / Disuruh coba lagi berulang-ulang" (System Hang / Overload)** — Membuktikan Epic 4: Circuit Breaker & HTTP 202 berhasil mencegah aplikasi hang tanpa batas dan memberikan UI Fallback yang informatif. | 1. Simulasikan kegagalan server BPJS: matikan service upstream atau blokir endpoint BPJS di level konfigurasi server 2. Buka aplikasi sebagai pasien 3. Navigasi ke halaman Antrean Online 4. Amati perilaku aplikasi selama 15–30 detik | Aplikasi **tidak menampilkan loading spinner tanpa batas**. Circuit Breaker aktif dan layar menampilkan UI Fallback yang informatif, contoh: *"Layanan antrean BPJS sedang dalam pemeliharaan rutin. Silakan coba beberapa saat lagi."* Tidak ada crash atau freeze. | — | — |
| TC-B04 | **[Rate Limiter]** Simulasi lonjakan traffic tinggi: lebih dari N request daftar antrean dikirimkan dalam waktu singkat dari satu klien | **Resolves Play Store Issue: "App freeze / tidak responsif saat jam sibuk" (Server Overload)** — Membuktikan Epic 4: Rate Limiter melindungi server dari kejenuhan akibat request berlebih, menjaga stabilitas sistem untuk pengguna lain. | 1. Gunakan tool (misal: `curl` loop, script, atau Postman Runner) untuk mengirimkan lebih dari ambang batas rate limit (misal: 20 request/detik) ke endpoint daftar antrean dari satu IP/akun 2. Amati respons HTTP yang diterima pada request ke-N (di atas batas) 3. Verifikasi bahwa pengguna lain yang mengakses secara normal tetap mendapat respons cepat | Request yang melampaui ambang batas menerima **HTTP 429 (Too Many Requests)** dengan pesan yang informatif. Pengguna lain yang mengakses secara normal tidak terdampak; sistem tetap responsif. Server tidak crash atau hang. | — | — |

---

## REKAPITULASI HASIL UAT

| Kategori | Total TC | Passed | Bug | Enhancement | Pending |
|:---|:---|:---|:---|:---|:---|
| **A — Functional & Real-Time** | 22 | 22 | 0 | 0 | 0 |
| **B — Non-Functional & Resilience (Epic 4)** | 4 | — | — | — | 4 |
| **TOTAL** | **26** | **22** | **0** | **0** | **4** |

> **Catatan:** TC-B01 s.d. TC-B04 berstatus *Pending* dan akan dieksekusi pada sesi UAT tanggal **30 Juni 2026**. Kolom Actual Result dan Status diisi oleh penguji selama sesi berlangsung.

---

## CATATAN DAN KESEPAKATAN

| Kategori | Catatan dan Kesepakatan | PJ |
|:---|:---|:---|
| | | |
| | | |

---

## LEMBAR PENGESAHAN

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Banjarmasin, 30 Juni 2026

**PIHAK PERTAMA** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **PIHAK KEDUA**

Universitas / Dosen Pengampu &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Tim Mobile JKN Redesign

`(_____________________________)`&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`(_____________________________)`

Dosen Pengampu &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Project Manager
