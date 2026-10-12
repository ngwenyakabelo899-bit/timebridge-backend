package com.timebridge.controller;

import com.timebridge.entity.Branch;
import com.timebridge.entity.Province;
import com.timebridge.repository.BranchRepository;
import com.timebridge.service.AvailabilityService;
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
    private final AvailabilityService availabilityService;

    public BranchController(BranchRepository branchRepository,
                            BestTimeService bestTimeService,
                            BranchLoadService branchLoadService,
                            AvailabilityService availabilityService) {
        this.branchRepository = branchRepository;
        this.bestTimeService = bestTimeService;
        this.branchLoadService = branchLoadService;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public List<Branch> getBranches(@RequestParam(required = false) Province province) {
        if (province != null) {
            return branchRepository.findByProvinceOrderByNameAsc(province);
        }
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

    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return branchRepository.findById(id)
                .<ResponseEntity<?>>map(branch -> ResponseEntity.ok(availabilityService.getAvailability(branch, date)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Branch not found")));
    }

    @GetMapping("/load-comparison")
    public List<?> getLoadComparison(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return branchLoadService.getLoadComparison(targetDate);
    }
}
