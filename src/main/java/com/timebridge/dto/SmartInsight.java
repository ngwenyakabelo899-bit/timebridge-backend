package com.timebridge.dto;

public class SmartInsight {

    private String category;
    private String message;
    private String tone;

    public SmartInsight(String category, String message, String tone) {
        this.category = category;
        this.message = message;
        this.tone = tone;
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public String getTone() {
        return tone;
    }
}
