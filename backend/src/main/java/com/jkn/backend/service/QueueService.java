package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueResponse;

import java.util.List;

public interface QueueService {

    QueueResponse createQueue(CreateQueueRequest request, String idempotencyKey);

    QueueResponse getQueueById(Long id);

    List<QueueResponse> getAllQueues();

    QueueResponse nextQueue(Long id);
    com.jkn.backend.dto.QueueStatusResponse getQueueStatusByIdempotencyKey(String idempotencyKey);
}
