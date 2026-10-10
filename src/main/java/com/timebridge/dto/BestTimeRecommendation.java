package com.timebridge.dto;

public class BestTimeRecommendation {

    private String quietestDay;
    private String quietestHour;
    private String busiestDay;
    private String busiestHour;
    private String message;
    private boolean hasEnoughData;

    public BestTimeRecommendation(String quietestDay, String quietestHour, String busiestDay,
                                  String busiestHour, String message, boolean hasEnoughData) {
        this.quietestDay = quietestDay;
        this.quietestHour = quietestHour;
        this.busiestDay = busiestDay;
        this.busiestHour = busiestHour;
        this.message = message;
        this.hasEnoughData = hasEnoughData;
    }

    public String getQuietestDay() {
        return quietestDay;
    }

    public String getQuietestHour() {
        return quietestHour;
    }

    public String getBusiestDay() {
        return busiestDay;
    }

    public String getBusiestHour() {
        return busiestHour;
    }

    public String getMessage() {
        return message;
    }

    public boolean isHasEnoughData() {
        return hasEnoughData;
    }
}
