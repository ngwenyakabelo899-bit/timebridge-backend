package com.timebridge.controller;

import com.timebridge.dto.AppointmentRequest;
import com.timebridge.entity.Appointment;
import com.timebridge.security.SecurityUtil;
import com.timebridge.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            Appointment appointment = appointmentService.createAppointment(email, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public List<Appointment> getMyAppointments() {
        String email = SecurityUtil.getCurrentUserEmail();
        return appointmentService.getAppointmentsForCitizen(email);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        String email = SecurityUtil.getCurrentUserEmail();
        return appointmentService.getAppointmentsForCitizen(email).stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Appointment not found")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> rescheduleAppointment(@PathVariable Long id,
                                                   @Valid @RequestBody AppointmentRequest request) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            Appointment appointment = appointmentService.rescheduleAppointment(email, id, request);
            return ResponseEntity.ok(appointment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            String email = SecurityUtil.getCurrentUserEmail();
            Appointment appointment = appointmentService.cancelAppointment(email, id);
            return ResponseEntity.ok(appointment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }
}
