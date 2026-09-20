package com.timebridge.dto;

public class WaitTimeTrendEntry {

    private String day;
    private double avgWaitMinutes;

    public WaitTimeTrendEntry(String day, double avgWaitMinutes) {
        this.day = day;
        this.avgWaitMinutes = avgWaitMinutes;
    }

    public String getDay() {
        return day;
    }

    public double getAvgWaitMinutes() {
        return avgWaitMinutes;
    }
}
