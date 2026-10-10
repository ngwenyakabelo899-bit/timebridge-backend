package com.timebridge.service;

import com.timebridge.dto.BestTimeRecommendation;
import com.timebridge.entity.Appointment;
import com.timebridge.entity.Branch;
import com.timebridge.repository.AppointmentRepository;
import com.timebridge.repository.BranchRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class BestTimeService {

    private static final int MIN_APPOINTMENTS_FOR_CONFIDENCE = 5;

    private final AppointmentRepository appointmentRepository;
    private final BranchRepository branchRepository;

    public BestTimeService(AppointmentRepository appointmentRepository, BranchRepository branchRepository) {
        this.appointmentRepository = appointmentRepository;
        this.branchRepository = branchRepository;
    }

    public BestTimeRecommendation getBestTimeForBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getBranch().getId().equals(branch.getId()))
                .toList();

        if (appointments.size() < MIN_APPOINTMENTS_FOR_CONFIDENCE) {
            return new BestTimeRecommendation(
                    null, null, null, null,
                    "Not enough booking history yet for this branch to make a reliable recommendation.",
                    false
            );
        }

        Map<DayOfWeek, Long> byDay = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentDate().getDayOfWeek(), Collectors.counting()));

        Map<Integer, Long> byHour = appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentTime().getHour(), Collectors.counting()));

        DayOfWeek quietestDay = findExtreme(byDay, true);
        DayOfWeek busiestDay = findExtreme(byDay, false);
        Integer quietestHour = findExtremeHour(byHour, true);
        Integer busiestHour = findExtremeHour(byHour, false);

        String quietestDayLabel = quietestDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String busiestDayLabel = busiestDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String quietestHourLabel = String.format("%02d:00", quietestHour);
        String busiestHourLabel = String.format("%02d:00", busiestHour);

        String message = String.format(
                "Based on %d past bookings, %ss around %s tend to be quietest. Avoid %ss around %s if possible.",
                appointments.size(), quietestDayLabel, quietestHourLabel, busiestDayLabel, busiestHourLabel
        );

        return new BestTimeRecommendation(
                quietestDayLabel, quietestHourLabel, busiestDayLabel, busiestHourLabel, message, true
        );
    }

    private DayOfWeek findExtreme(Map<DayOfWeek, Long> counts, boolean lowest) {
        DayOfWeek result = null;
        long best = lowest ? Long.MAX_VALUE : Long.MIN_VALUE;
        for (DayOfWeek day : DayOfWeek.values()) {
            long count = counts.getOrDefault(day, 0L);
            if ((lowest && count < best) || (!lowest && count > best)) {
                best = count;
                result = day;
            }
        }
        return result;
    }

    private Integer findExtremeHour(Map<Integer, Long> counts, boolean lowest) {
        Integer result = null;
        long best = lowest ? Long.MAX_VALUE : Long.MIN_VALUE;
        for (Map.Entry<Integer, Long> entry : counts.entrySet()) {
            if ((lowest && entry.getValue() < best) || (!lowest && entry.getValue() > best)) {
                best = entry.getValue();
                result = entry.getKey();
            }
        }
        return result != null ? result : 9;
    }
}
