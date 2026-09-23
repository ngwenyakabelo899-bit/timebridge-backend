package com.timebridge.controller;

import com.timebridge.service.AdminService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public Object getDashboard() {
        return adminService.getDashboardStats();
    }

    @GetMapping("/live-queue")
    public Object getLiveQueue() {
        return adminService.getLiveQueue();
    }

    @GetMapping("/recent-appointments")
    public Object getRecentAppointments() {
        return adminService.getRecentAppointments();
    }

    @GetMapping("/activity")
    public Object getRecentActivity() {
        return adminService.getRecentActivity();
    }

    @GetMapping("/weekly-performance")
    public Object getWeeklyPerformance() {
        return adminService.getWeeklyPerformance();
    }

    @GetMapping("/peak-hours")
    public Object getPeakHours() {
        return adminService.getPeakHours();
    }

    @GetMapping("/service-distribution")
    public Object getServiceDistribution() {
        return adminService.getServiceDistribution();
    }

    @GetMapping("/wait-time-trend")
    public Object getWaitTimeTrend() {
        return adminService.getWaitTimeTrend();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAppointmentsCsv() {
        String csv = adminService.generateAppointmentsCsv();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        String filename = "timebridge-appointments-" + LocalDate.now() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}