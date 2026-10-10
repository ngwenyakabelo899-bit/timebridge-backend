package com.timebridge.dto;

public class BranchLoadInfo {

    private Long branchId;
    private String branchName;
    private String location;
    private int appointmentsOnDate;
    private long currentQueueCount;
    private int dailyCapacity;
    private double loadPercentage;
    private boolean recommended;

    public BranchLoadInfo(Long branchId, String branchName, String location, int appointmentsOnDate,
                          long currentQueueCount, int dailyCapacity, double loadPercentage, boolean recommended) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.location = location;
        this.appointmentsOnDate = appointmentsOnDate;
        this.currentQueueCount = currentQueueCount;
        this.dailyCapacity = dailyCapacity;
        this.loadPercentage = loadPercentage;
        this.recommended = recommended;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getLocation() {
        return location;
    }

    public int getAppointmentsOnDate() {
        return appointmentsOnDate;
    }

    public long getCurrentQueueCount() {
        return currentQueueCount;
    }

    public int getDailyCapacity() {
        return dailyCapacity;
    }

    public double getLoadPercentage() {
        return loadPercentage;
    }

    public boolean isRecommended() {
        return recommended;
    }
}
