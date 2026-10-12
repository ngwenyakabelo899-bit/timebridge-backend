package com.timebridge.entity;

public enum Province {
    EASTERN_CAPE("Eastern Cape"),
    FREE_STATE("Free State"),
    GAUTENG("Gauteng"),
    KWAZULU_NATAL("KwaZulu-Natal"),
    LIMPOPO("Limpopo"),
    MPUMALANGA("Mpumalanga"),
    NORTHERN_CAPE("Northern Cape"),
    NORTH_WEST("North West"),
    WESTERN_CAPE("Western Cape");

    private final String displayName;

    Province(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
