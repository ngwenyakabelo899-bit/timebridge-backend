package com.timebridge.dto;

import jakarta.validation.constraints.NotNull;

public class QueueRequest {

    @NotNull(message = "Appointment is required")
    private Long appointmentId;

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
}
