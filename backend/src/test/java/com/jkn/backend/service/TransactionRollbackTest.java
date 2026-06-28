package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.entity.QueueTicket;
import com.jkn.backend.repository.QueueCounterRepository;
import com.jkn.backend.repository.QueueTicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class TransactionRollbackTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueCounterRepository queueCounterRepository;

    @MockitoSpyBean
    private QueueTicketRepository queueTicketRepository;

    @Test
    @DisplayName("Harus me-rollback seluruh transaksi (termasuk counter/kuota) ketika gagal generate tiket antrean (Simulasi OOM)")
    void shouldRollbackWhenQueueGenerationFails() {
        // Arrange: Catat jumlah antrean (kuota) sebelum transaksi dimulai
        long queueCounterBefore = queueCounterRepository.count();

        // Inject Chaos: Pura-pura memori penuh (OOM) atau listrik mati SAAT menyimpan tiket antrean,
        // PADAHAL counter antrean (kuota) sudah telanjur tersimpan di memori transaksi sebelumnya.
        doThrow(new RuntimeException("Simulated OOM / Koneksi Putus"))
                .when(queueTicketRepository).save(any(QueueTicket.class));

        CreateQueueRequest request = new CreateQueueRequest("Poli Jantung", "test_rollback_user", 1L);

        // Act: Eksekusi API pendaftaran antrean
        // Sistem harus menolak transaksi dan melempar Exception yang sama ke Controller
        Exception exception = assertThrows(RuntimeException.class, () -> {
            queueService.createQueue(request, "idem-rollback-001");
        });

        assertEquals("Simulated OOM / Koneksi Putus", exception.getMessage());

        // Assert: TRANSAKSI HARUS ROLLBACK! (All-or-Nothing)
        // Data counter antrean/kuota di database TIDAK BOLEH bertambah (menghindari Ghost Data)
        long queueCounterAfter = queueCounterRepository.count();
        assertEquals(queueCounterBefore, queueCounterAfter, "Transaksi gagal melakukan rollback! Ada kebocoran/ghost data di database.");
    }
}
