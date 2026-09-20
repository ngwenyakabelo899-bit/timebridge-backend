package com.timebridge.dto;

import java.time.LocalDateTime;

public class ActivityLogEntry {

    private String token;
    private String clientName;
    private String serviceName;
    private String branchName;
    private String status;
    private LocalDateTime timestamp;

    public ActivityLogEntry(String token, String clientName, String serviceName,
                            String branchName, String status, LocalDateTime timestamp) {
        this.token = token;
        this.clientName = clientName;
        this.serviceName = serviceName;
        this.branchName = branchName;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getToken() {
        return token;
    }

    public String getClientName() {
        return clientName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
