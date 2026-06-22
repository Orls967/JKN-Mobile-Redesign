# MANUAL VERIFICATION CHECKLIST: STORY 2.3 RECOVERY

Karena keterbatasan simulasi network/WiFi down pada environment *unit test* standar, pengujian skenario ini harus dilakukan secara manual (Integration / E2E Testing).

## Skenario 1: Backend Down Recovery (ITEM 4)
**Tujuan:** Membuktikan transisi CONNECTED → DISCONNECTED → CONNECTED.

**Langkah Pengujian:**
1. Jalankan aplikasi JKN Mobile dan buka layar `PatientQueueScreen`.
2. Perhatikan *Connection Status Badge* di kiri atas. Pastikan status **CONNECTED** (Warna Hijau).
3. Matikan service backend Spring Boot secara paksa (`CTRL+C` di terminal backend).
4. Perhatikan layar Android. *Connection Status Badge* harus berubah menjadi **DISCONNECTED** (Warna Merah).
5. Buka Logcat Android Studio, saring (*filter*) dengan *tag* `STOMP`.
6. Anda akan melihat log:
   `E/STOMP: WebSocket connection lost/failed`
   `D/STOMP: Retrying in 1000 ms (attempt 1)`
   `D/STOMP: Retrying in 2000 ms (attempt 2)`
7. Nyalakan kembali backend Spring Boot.
8. Setelah backend siap, log Android akan otomatis menunjukkan:
   `D/STOMP: Connected to Queue WebSocket`
9. *Connection Status Badge* di UI akan kembali menjadi **CONNECTED** (Warna Hijau).

**Status Verification:** Berhasil jika step 4 dan 9 terjadi tanpa perlu menekan tombol apa pun di layar HP.

---

## Skenario 2: WiFi Recovery (ITEM 5)
**Tujuan:** Membuktikan aplikasi tetap resilien saat jaringan HP putus sementara.

**Langkah Pengujian:**
1. Buka layar `PatientQueueScreen`, pastikan status **CONNECTED**.
2. Tarik menu pengaturan Android (Notification Bar), matikan **WiFi** (atau aktifkan Airplane Mode).
3. *Connection Status Badge* akan berubah ke **DISCONNECTED**.
4. Logcat akan mencatat error `java.net.ConnectException` atau `java.net.UnknownHostException`, lalu memulai proses `Retrying in ... ms`.
5. Tunggu sekitar 5-8 detik, lalu hidupkan kembali **WiFi**.
6. Setelah Android mendapatkan akses internet, iterasi *retry* berikutnya akan berhasil memanggil `stompClient.connect()`.
7. Status Badge kembali menjadi **CONNECTED**.

**Status Verification:** Berhasil jika UI kembali ke warna Hijau secara reaktif pasca koneksi internet pulih.
