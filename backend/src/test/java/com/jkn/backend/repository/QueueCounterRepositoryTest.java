package com.jkn.backend.repository;

import com.jkn.backend.entity.QueueCounter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class QueueCounterRepositoryTest {

    @Autowired
    private QueueCounterRepository queueCounterRepository;

    @Test
    void testSaveAndFindByIdForUpdate() {
        // Arrange
        QueueCounter queue = new QueueCounter();
        queue.setCounterName("Poli Uji");
        queue.setCurrentNumber(0);
        queue.setNextNumber(1);
        QueueCounter saved = queueCounterRepository.save(queue);

        // Act
        Optional<QueueCounter> found = queueCounterRepository.findByIdForUpdate(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Poli Uji", found.get().getCounterName());
        assertEquals(0, found.get().getCurrentNumber());
    }
}
