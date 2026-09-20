package com.timebridge.dto;

public class AdminDashboardStats {

    private long totalClientsServed;
    private double avgWaitTimeMinutes;
    private long numberOfAppointments;
    private double noShowRatePercent;

    public AdminDashboardStats(long totalClientsServed, double avgWaitTimeMinutes,
                               long numberOfAppointments, double noShowRatePercent) {
        this.totalClientsServed = totalClientsServed;
        this.avgWaitTimeMinutes = avgWaitTimeMinutes;
        this.numberOfAppointments = numberOfAppointments;
        this.noShowRatePercent = noShowRatePercent;
    }

    public long getTotalClientsServed() {
        return totalClientsServed;
    }

    public double getAvgWaitTimeMinutes() {
        return avgWaitTimeMinutes;
    }

    public long getNumberOfAppointments() {
        return numberOfAppointments;
    }

    public double getNoShowRatePercent() {
        return noShowRatePercent;
    }
}
