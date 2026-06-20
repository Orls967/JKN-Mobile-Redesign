package com.jkn.backend.repository;

import com.jkn.backend.entity.QueueCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueCounterRepository extends JpaRepository<QueueCounter, Long> {
}
