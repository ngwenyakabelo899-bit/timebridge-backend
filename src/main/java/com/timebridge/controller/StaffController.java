package com.timebridge.controller;

import com.timebridge.dto.StaffDashboardStats;
import com.timebridge.dto.StaffQueueResponse;
import com.timebridge.service.QueueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final QueueService queueService;

    public StaffController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/queue")
    public ResponseEntity<?> getWaitingQueue(@RequestParam Long branchId) {
        try {
            List<StaffQueueResponse> queue = queueService.getWaitingQueue(branchId);
            return ResponseEntity.ok(queue);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/queue/current")
    public ResponseEntity<?> getCurrentClient(@RequestParam Long branchId) {
        try {
            StaffQueueResponse current = queueService.getCurrentClient(branchId);
            if (current == null) {
                return ResponseEntity.ok(Map.of("message", "No client currently being served"));
            }
            return ResponseEntity.ok(current);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats(@RequestParam Long branchId) {
        try {
            StaffDashboardStats stats = queueService.getDashboardStats(branchId);
            return ResponseEntity.ok(stats);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/queue/next")
    public ResponseEntity<?> callNextClient(@RequestParam Long branchId) {
        try {
            StaffQueueResponse next = queueService.callNextClient(branchId);
            return ResponseEntity.ok(next);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/queue/{id}/serve")
    public ResponseEntity<?> serveClient(@PathVariable Long id) {
        try {
            StaffQueueResponse served = queueService.serveClient(id);
            return ResponseEntity.ok(served);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/queue/{id}/no-show")
    public ResponseEntity<?> markNoShow(@PathVariable Long id) {
        try {
            StaffQueueResponse result = queueService.markNoShow(id);
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}
