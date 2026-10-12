package com.timebridge.dto;

import java.util.List;

public record AvailabilityResponse(String date, boolean open, String message, List<SlotInfo> slots) {

    public record SlotInfo(String time, int remaining, boolean available) {
    }
}
