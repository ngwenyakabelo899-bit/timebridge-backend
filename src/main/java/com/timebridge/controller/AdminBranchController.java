package com.timebridge.controller;

import com.timebridge.entity.Branch;
import com.timebridge.repository.BranchRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/branches")
public class AdminBranchController {

    private final BranchRepository branchRepository;

    public AdminBranchController(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @GetMapping
    public List<Branch> getAll() {
        return branchRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Branch branch) {
        branch.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(branchRepository.save(branch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @Valid @RequestBody Branch updated) {
        return branchRepository.findById(id)
                .<ResponseEntity<Object>>map(existing -> {
                    existing.setName(updated.getName());
                    existing.setLocation(updated.getLocation());
                    existing.setDailyCapacity(updated.getDailyCapacity());
                    return ResponseEntity.ok(branchRepository.save(existing));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Branch not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!branchRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Branch not found"));
        }
        branchRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Branch deleted"));
    }
}
