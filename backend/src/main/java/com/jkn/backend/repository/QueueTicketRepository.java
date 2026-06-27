package com.jkn.backend.repository;

import com.jkn.backend.entity.QueueTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueTicketRepository extends JpaRepository<QueueTicket, Long> {
}
