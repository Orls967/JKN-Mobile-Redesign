package com.jkn.backend.service;

import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueResponse;

public interface QueueService {

    QueueResponse createQueue(CreateQueueRequest request);

    QueueResponse getQueueById(Long id);
}
