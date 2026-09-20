package com.timebridge.controller;

import com.timebridge.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
