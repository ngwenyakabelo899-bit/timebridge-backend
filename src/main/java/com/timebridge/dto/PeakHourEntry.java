package com.timebridge.dto;

public class PeakHourEntry {

    private String hour;
    private long clientsPerHour;

    public PeakHourEntry(String hour, long clientsPerHour) {
        this.hour = hour;
        this.clientsPerHour = clientsPerHour;
    }

    public String getHour() {
        return hour;
    }

    public long getClientsPerHour() {
        return clientsPerHour;
    }
}
