package com.timebridge.dto;

public class QueueResponse {

    private Long id;
    private String queueToken;
    private Integer queuePosition;
    private Integer estimatedWaitMinutes;
    private String status;
    private String branchName;
    private String serviceName;
    private String appointmentTokenNumber;

    public QueueResponse(Long id, String queueToken, Integer queuePosition, Integer estimatedWaitMinutes,
                         String status, String branchName, String serviceName, String appointmentTokenNumber) {
        this.id = id;
        this.queueToken = queueToken;
        this.queuePosition = queuePosition;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.status = status;
        this.branchName = branchName;
        this.serviceName = serviceName;
        this.appointmentTokenNumber = appointmentTokenNumber;
    }

    public Long getId() {
        return id;
    }

    public String getQueueToken() {
        return queueToken;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public Integer getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public String getStatus() {
        return status;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getAppointmentTokenNumber() {
        return appointmentTokenNumber;
    }
}
