package com.timebridge.controller;

import com.timebridge.entity.Branch;
import com.timebridge.repository.BranchRepository;
import com.timebridge.service.BestTimeService;
import com.timebridge.service.BranchLoadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchRepository branchRepository;
    private final BestTimeService bestTimeService;
    private final BranchLoadService branchLoadService;

    public BranchController(BranchRepository branchRepository,
                            BestTimeService bestTimeService,
                            BranchLoadService branchLoadService) {
        this.branchRepository = branchRepository;
        this.bestTimeService = bestTimeService;
        this.branchLoadService = branchLoadService;
    }

    @GetMapping
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBranchById(@PathVariable Long id) {
        return branchRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Branch not found")));
    }

    @GetMapping("/{id}/best-times")
    public ResponseEntity<?> getBestTimes(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bestTimeService.getBestTimeForBranch(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/load-comparison")
    public List<?> getLoadComparison(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return branchLoadService.getLoadComparison(targetDate);
    }
}
