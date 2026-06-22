package com.jkn.backend.repository;

import com.jkn.backend.entity.QueueCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface QueueCounterRepository extends JpaRepository<QueueCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QueueCounter q WHERE q.id = :id")
    Optional<QueueCounter> findByIdForUpdate(@Param("id") Long id);
}
