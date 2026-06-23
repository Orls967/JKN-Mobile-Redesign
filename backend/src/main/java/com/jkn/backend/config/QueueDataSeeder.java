package com.jkn.backend.config;

import com.jkn.backend.entity.QueueCounter;
import com.jkn.backend.repository.QueueCounterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class QueueDataSeeder implements CommandLineRunner {

    private final QueueCounterRepository queueCounterRepository;

    public QueueDataSeeder(QueueCounterRepository queueCounterRepository) {
        this.queueCounterRepository = queueCounterRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (queueCounterRepository.count() == 0) {
            QueueCounter defaultQueue = new QueueCounter();
            defaultQueue.setCounterName("Loket Pendaftaran");
            defaultQueue.setCurrentNumber(0);
            defaultQueue.setNextNumber(1);
            defaultQueue.setLastNumber(999);
            queueCounterRepository.save(defaultQueue);
            System.out.println("Default Queue (Loket Pendaftaran) seeded successfully.");
        } else {
            // Reset existing queues for demo purposes and increase limit
            var queues = queueCounterRepository.findAll();
            for (QueueCounter q : queues) {
                q.setCurrentNumber(0);
                q.setNextNumber(1);
                q.setLastNumber(999);
                queueCounterRepository.save(q);
            }
            System.out.println("Existing Queues reset and limit increased to 999.");
        }
    }
}
