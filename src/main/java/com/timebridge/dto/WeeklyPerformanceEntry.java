package com.timebridge.dto;

public class WeeklyPerformanceEntry {

    private String day;
    private long clientsServed;
    private long noShows;

    public WeeklyPerformanceEntry(String day, long clientsServed, long noShows) {
        this.day = day;
        this.clientsServed = clientsServed;
        this.noShows = noShows;
    }

    public String getDay() {
        return day;
    }

    public long getClientsServed() {
        return clientsServed;
    }

    public long getNoShows() {
        return noShows;
    }
}
