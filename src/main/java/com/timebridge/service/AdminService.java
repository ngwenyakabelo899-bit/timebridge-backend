package com.timebridge.service;

import com.timebridge.dto.PeakHourEntry;
import com.timebridge.dto.ServiceDistributionEntry;
import com.timebridge.dto.WaitTimeTrendEntry;
import com.timebridge.dto.WeeklyPerformanceEntry;
import com.timebridge.entity.Service;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import com.timebridge.dto.ActivityLogEntry;
import com.timebridge.dto.AdminDashboardStats;
import com.timebridge.dto.StaffQueueResponse;
import com.timebridge.entity.Appointment;
import com.timebridge.entity.AppointmentStatus;
import com.timebridge.entity.Queue;
import com.timebridge.entity.QueueStatus;
import com.timebridge.repository.AppointmentRepository;
import com.timebridge.repository.QueueRepository;
import org.springframework.data.domain.PageRequest;


import java.util.List;

@org.springframework.stereotype.Service
public class AdminService {

    private final AppointmentRepository appointmentRepository;
    private final QueueRepository queueRepository;

    public AdminService(AppointmentRepository appointmentRepository, QueueRepository queueRepository) {
        this.appointmentRepository = appointmentRepository;
        this.queueRepository = queueRepository;
    }

    public AdminDashboardStats getDashboardStats() {
        long totalServed = queueRepository.countByStatus(QueueStatus.SERVED);
        long totalAppointments = appointmentRepository.count();
        long noShows = appointmentRepository.countNoShows();

        Double avgWait = queueRepository.averageWaitTimeAllServed();

        double noShowRate = totalAppointments == 0
                ? 0.0
                : (noShows * 100.0) / totalAppointments;

        return new AdminDashboardStats(
                totalServed,
                avgWait == null ? 0.0 : avgWait,
                totalAppointments,
                Math.round(noShowRate * 10.0) / 10.0
        );
    }

    public List<StaffQueueResponse> getLiveQueue() {
        return queueRepository.findByStatusOrderByQueuePositionAsc(QueueStatus.WAITING).stream()
                .map(q -> new StaffQueueResponse(
                        q.getId(),
                        q.getQueueToken(),
                        q.getAppointment().getCitizen().getFirstName(),
                        q.getAppointment().getCitizen().getLastName(),
                        q.getAppointment().getService().getName(),
                        q.getQueuePosition(),
                        q.getEstimatedWaitMinutes(),
                        q.getStatus().name()
                ))
                .toList();
    }

    public List<Appointment> getRecentAppointments() {
        return appointmentRepository.findTop10ByOrderByCreatedAtDesc();
    }

    public List<ActivityLogEntry> getRecentActivity() {
        return queueRepository.findRecentActivity(PageRequest.of(0, 10)).stream()
                .map(q -> new ActivityLogEntry(
                        q.getAppointment().getTokenNumber(),
                        q.getAppointment().getCitizen().getFirstName() + " " + q.getAppointment().getCitizen().getLastName(),
                        q.getAppointment().getService().getName(),
                        q.getAppointment().getBranch().getName(),
                        q.getStatus().name(),
                        q.getJoinedAt()
                ))
                .toList();
    }

    public List<WeeklyPerformanceEntry> getWeeklyPerformance() {
        Map<DayOfWeek, Long> served = queueRepository.findAll().stream()
                .filter(q -> q.getStatus() == QueueStatus.SERVED)
                .collect(Collectors.groupingBy(
                        q -> q.getJoinedAt().getDayOfWeek(), Collectors.counting()));

        Map<DayOfWeek, Long> noShows = queueRepository.findAll().stream()
                .filter(q -> q.getStatus() == QueueStatus.NO_SHOW)
                .collect(Collectors.groupingBy(
                        q -> q.getJoinedAt().getDayOfWeek(), Collectors.counting()));

        List<WeeklyPerformanceEntry> result = new java.util.ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            String label = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            result.add(new WeeklyPerformanceEntry(
                    label,
                    served.getOrDefault(day, 0L),
                    noShows.getOrDefault(day, 0L)
            ));
        }
        return result;
    }

    public List<PeakHourEntry> getPeakHours() {
        Map<Integer, Long> byHour = appointmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        a -> a.getAppointmentTime().getHour(), Collectors.counting()));

        Map<Integer, Long> sorted = new LinkedHashMap<>();
        byHour.keySet().stream().sorted().forEach(hour -> sorted.put(hour, byHour.get(hour)));

        return sorted.entrySet().stream()
                .map(e -> new PeakHourEntry(String.format("%02d:00", e.getKey()), e.getValue()))
                .toList();
    }

    public List<ServiceDistributionEntry> getServiceDistribution() {
        List<Appointment> all = appointmentRepository.findAll();
        long total = all.size();

        Map<String, Long> byService = all.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getService().getName(), Collectors.counting()));

        return byService.entrySet().stream()
                .map(e -> new ServiceDistributionEntry(
                        e.getKey(),
                        e.getValue(),
                        total == 0 ? 0.0 : Math.round((e.getValue() * 1000.0) / total) / 10.0
                ))
                .toList();
    }

    public List<WaitTimeTrendEntry> getWaitTimeTrend() {
        Map<DayOfWeek, List<Queue>> byDay = queueRepository.findAll().stream()
                .filter(q -> q.getStatus() == QueueStatus.SERVED)
                .collect(Collectors.groupingBy(q -> q.getJoinedAt().getDayOfWeek()));

        List<WaitTimeTrendEntry> result = new java.util.ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            String label = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            List<Queue> dayQueues = byDay.getOrDefault(day, List.of());
            double avg = dayQueues.isEmpty()
                    ? 0.0
                    : dayQueues.stream().mapToInt(Queue::getEstimatedWaitMinutes).average().orElse(0.0);
            result.add(new WaitTimeTrendEntry(label, Math.round(avg * 10.0) / 10.0));
        }
        return result;
    }
}
