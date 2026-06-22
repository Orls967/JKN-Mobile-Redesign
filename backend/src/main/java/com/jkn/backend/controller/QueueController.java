package com.jkn.backend.controller;

import com.jkn.backend.dto.ApiResponse;
import com.jkn.backend.dto.CreateQueueRequest;
import com.jkn.backend.dto.QueueChangedEvent;
import com.jkn.backend.dto.QueueResponse;
import com.jkn.backend.service.QueueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;
    private final SimpMessagingTemplate messagingTemplate;

    public QueueController(QueueService queueService, SimpMessagingTemplate messagingTemplate) {
        this.queueService = queueService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QueueResponse>> createQueue(@RequestBody CreateQueueRequest request) {
        QueueResponse response = queueService.createQueue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QueueResponse>>> getAllQueues() {
        List<QueueResponse> responses = queueService.getAllQueues();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QueueResponse>> getQueueById(@PathVariable Long id) {
        QueueResponse response = queueService.getQueueById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/next")
    public ResponseEntity<ApiResponse<QueueResponse>> nextQueue(@PathVariable Long id) {
        QueueResponse response = queueService.nextQueue(id);
        
        QueueChangedEvent eventPayload = new QueueChangedEvent(
                id, 
                response.getCurrentNumber(), 
                response.getCurrentNumber() + 1
        );
        
        messagingTemplate.convertAndSend("/topic/queue/" + id, eventPayload);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}