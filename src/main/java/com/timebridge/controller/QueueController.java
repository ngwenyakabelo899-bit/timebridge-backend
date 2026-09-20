package com.timebridge.controller;

import com.timebridge.dto.QueueRequest;
import com.timebridge.dto.QueueResponse;
import com.timebridge.security.SecurityUtil;
import com.timebridge.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinQueue(@Valid @RequestBody QueueRequest request) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            QueueResponse response = queueService.joinQueue(email, request.getAppointmentId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my/{appointmentId}")
    public ResponseEntity<?> getMyQueueStatus(@PathVariable Long appointmentId) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            QueueResponse response = queueService.getMyQueueStatus(email, appointmentId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> leaveQueue(@PathVariable Long id) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            QueueResponse response = queueService.leaveQueue(email, id);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }
}
