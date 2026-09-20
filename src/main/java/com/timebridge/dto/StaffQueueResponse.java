package com.timebridge.dto;

public class StaffQueueResponse {

    private Long queueId;
    private String queueToken;
    private String citizenFirstName;
    private String citizenLastName;
    private String serviceName;
    private Integer queuePosition;
    private Integer estimatedWaitMinutes;
    private String status;

    public StaffQueueResponse(Long queueId, String queueToken, String citizenFirstName, String citizenLastName,
                              String serviceName, Integer queuePosition, Integer estimatedWaitMinutes, String status) {
        this.queueId = queueId;
        this.queueToken = queueToken;
        this.citizenFirstName = citizenFirstName;
        this.citizenLastName = citizenLastName;
        this.serviceName = serviceName;
        this.queuePosition = queuePosition;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.status = status;
    }

    public Long getQueueId() {
        return queueId;
    }

    public String getQueueToken() {
        return queueToken;
    }

    public String getCitizenFirstName() {
        return citizenFirstName;
    }

    public String getCitizenLastName() {
        return citizenLastName;
    }

    public String getServiceName() {
        return serviceName;
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
}
