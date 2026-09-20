package com.timebridge.dto;

public class StaffDashboardStats {

    private long inQueue;
    private long servedToday;
    private double avgServiceTimeMinutes;
    private int currentWaitMinutes;

    public StaffDashboardStats(long inQueue, long servedToday, double avgServiceTimeMinutes, int currentWaitMinutes) {
        this.inQueue = inQueue;
        this.servedToday = servedToday;
        this.avgServiceTimeMinutes = avgServiceTimeMinutes;
        this.currentWaitMinutes = currentWaitMinutes;
    }

    public long getInQueue() {
        return inQueue;
    }

    public long getServedToday() {
        return servedToday;
    }

    public double getAvgServiceTimeMinutes() {
        return avgServiceTimeMinutes;
    }

    public int getCurrentWaitMinutes() {
        return currentWaitMinutes;
    }
}
