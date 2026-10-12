package com.timebridge.service;

import com.timebridge.dto.AvailabilityResponse;
import com.timebridge.dto.AvailabilityResponse.SlotInfo;
import com.timebridge.entity.Appointment;
import com.timebridge.entity.AppointmentStatus;
import com.timebridge.entity.Branch;
import com.timebridge.exception.BookingRuleException;
import com.timebridge.repository.AppointmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    private static final LocalTime FIRST_SLOT = LocalTime.of(8, 0);
    private static final LocalTime LAST_SLOT = LocalTime.of(15, 30);
    private static final int SLOT_MINUTES = 30;
    private static final int MAX_DAYS_AHEAD = 90;
    private static final int DEFAULT_DAILY_CAPACITY = 20;
    private static final List<AppointmentStatus> FREED = List.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW);

    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<LocalTime> allSlots() {
        List<LocalTime> slots = new ArrayList<>();
        for (LocalTime t = FIRST_SLOT; !t.isAfter(LAST_SLOT); t = t.plusMinutes(SLOT_MINUTES)) {
            slots.add(t);
        }
        return slots;
    }

    public AvailabilityResponse getAvailability(Branch branch, LocalDate date) {
        String closedReason = closedReason(date);
        if (closedReason != null) {
            return new AvailabilityResponse(date.toString(), false, closedReason, List.of());
        }

        boolean dayFull = dayBooked(branch, date, null) >= dailyCapacity(branch);
        List<SlotInfo> slots = allSlots().stream().map(t -> {
            int remaining = remainingForSlot(branch, date, t, null);
            return new SlotInfo(t.toString(), remaining, !dayFull && remaining > 0);
        }).toList();

        return new AvailabilityResponse(date.toString(), true,
                dayFull ? "This branch is fully booked on this date." : null, slots);
    }

    // Throws a BookingRuleException when the requested date and time cannot be booked.
    // 'ignore' is the appointment being rescheduled, so it does not block itself.
    public void assertBookable(Branch branch, LocalDate date, LocalTime time, Appointment ignore) {
        String closedReason = closedReason(date);
        if (closedReason != null) {
            throw new BookingRuleException(HttpStatus.BAD_REQUEST, closedReason);
        }

        LocalTime slot = time.withSecond(0).withNano(0);
        if (!allSlots().contains(slot)) {
            throw new BookingRuleException(HttpStatus.BAD_REQUEST,
                    "Please choose a valid time between 08:00 and 15:30 in 30-minute steps.");
        }

        if (dayBooked(branch, date, ignore) >= dailyCapacity(branch)) {
            throw new BookingRuleException(HttpStatus.CONFLICT,
                    "This branch is fully booked on this date. Please choose another date.");
        }
        if (remainingForSlot(branch, date, slot, ignore) <= 0) {
            throw new BookingRuleException(HttpStatus.CONFLICT,
                    "That time slot is fully booked. Please choose another time.");
        }
    }

    private String closedReason(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date == null || !date.isAfter(today)) {
            return "Appointments must be booked for a future date.";
        }
        if (date.isAfter(today.plusDays(MAX_DAYS_AHEAD))) {
            return "Appointments can only be booked up to " + MAX_DAYS_AHEAD + " days ahead.";
        }
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return "Branches are closed on weekends. Please choose a weekday.";
        }
        return null;
    }

    private int dailyCapacity(Branch branch) {
        Integer value = branch.getDailyCapacity();
        return value == null || value <= 0 ? DEFAULT_DAILY_CAPACITY : value;
    }

    private int slotCapacity(Branch branch) {
        return Math.max(1, (int) Math.ceil(dailyCapacity(branch) / (double) allSlots().size()));
    }

    private int remainingForSlot(Branch branch, LocalDate date, LocalTime time, Appointment ignore) {
        long booked = appointmentRepository
                .countByBranchAndAppointmentDateAndAppointmentTimeAndStatusNotIn(branch, date, time, FREED);
        if (occupies(ignore, branch, date, time)) {
            booked--;
        }
        return (int) Math.max(0, slotCapacity(branch) - booked);
    }

    private long dayBooked(Branch branch, LocalDate date, Appointment ignore) {
        long booked = appointmentRepository.countByBranchAndAppointmentDateAndStatusNotIn(branch, date, FREED);
        if (occupies(ignore, branch, date, null)) {
            booked--;
        }
        return Math.max(0, booked);
    }

    private boolean occupies(Appointment a, Branch branch, LocalDate date, LocalTime time) {
        return a != null
                && a.getBranch().getId().equals(branch.getId())
                && a.getAppointmentDate().equals(date)
                && (time == null || a.getAppointmentTime().withSecond(0).withNano(0).equals(time))
                && !FREED.contains(a.getStatus());
    }
}
