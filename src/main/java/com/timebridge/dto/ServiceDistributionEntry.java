package com.timebridge.dto;

public class ServiceDistributionEntry {

    private String serviceName;
    private long appointmentCount;
    private double percentage;

    public ServiceDistributionEntry(String serviceName, long appointmentCount, double percentage) {
        this.serviceName = serviceName;
        this.appointmentCount = appointmentCount;
        this.percentage = percentage;
    }

    public String getServiceName() {
        return serviceName;
    }

    public long getAppointmentCount() {
        return appointmentCount;
    }

    public double getPercentage() {
        return percentage;
    }
}
