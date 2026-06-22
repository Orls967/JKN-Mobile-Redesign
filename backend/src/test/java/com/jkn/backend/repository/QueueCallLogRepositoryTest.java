package com.jkn.backend.repository;

import com.jkn.backend.entity.QueueCallLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class QueueCallLogRepositoryTest {

    @Autowired
    private QueueCallLogRepository queueCallLogRepository;

    @Test
    void testSaveLog() {
        // Arrange
        QueueCallLog log = new QueueCallLog(99L, 5);

        // Act
        QueueCallLog saved = queueCallLogRepository.save(log);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(99L, saved.getQueueCounterId());
        assertEquals(5, saved.getTicketNumber());
        assertNotNull(saved.getCalledAt());
    }
}
