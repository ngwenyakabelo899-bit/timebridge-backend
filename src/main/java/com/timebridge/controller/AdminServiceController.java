package com.timebridge.controller;

import com.timebridge.entity.Service;
import com.timebridge.repository.ServiceRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/services")
public class AdminServiceController {

    private final ServiceRepository serviceRepository;

    public AdminServiceController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    public List<Service> getAll() {
        return serviceRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Service service) {
        service.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRepository.save(service));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @Valid @RequestBody Service updated) {
        return serviceRepository.findById(id)
                .<ResponseEntity<Object>>map(existing -> {
                    existing.setName(updated.getName());
                    existing.setDurationMinutes(updated.getDurationMinutes());
                    return ResponseEntity.ok(serviceRepository.save(existing));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Service not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!serviceRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Service not found"));
        }
        serviceRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Service deleted"));
    }
}
