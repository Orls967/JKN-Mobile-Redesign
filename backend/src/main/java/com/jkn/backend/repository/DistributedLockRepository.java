package com.jkn.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jkn.backend.entity.QueueCounter;

@Repository
public interface DistributedLockRepository extends JpaRepository<QueueCounter, Long> {

    /**
     * PostgreSQL Advisory Transaction Lock.
     * Lock ini akan otomatis dirilis ketika transaksi (commit/rollback) selesai.
     * Menggunakan hashtext() untuk mengubah string menjadi int 32-bit (atau bisa pakai fungsi hash lain).
     *
     * @param lockKey string representasi key, misal "queue_lock:userId:faskesId:tanggal"
     * @return true jika berhasil mendapatkan lock, false jika sedang di-lock transaksi lain
     */
    @Query(value = "SELECT pg_try_advisory_xact_lock(hashtext(:lockKey))", nativeQuery = true)
    boolean tryAcquireLock(@Param("lockKey") String lockKey);
}
