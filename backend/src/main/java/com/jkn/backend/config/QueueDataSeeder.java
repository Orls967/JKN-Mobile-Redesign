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
            queueCounterRepository.save(defaultQueue);
            System.out.println("Default Queue (Loket Pendaftaran) seeded successfully.");
        }
    }
}
