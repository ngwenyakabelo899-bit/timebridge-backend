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
import com.timebridge.dto.SmartInsight;


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

    public String generateAppointmentsCsv() {
        List<Appointment> appointments = appointmentRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("Token,Citizen,Service,Branch,Date,Time,Status,Created At\n");

        for (Appointment a : appointments) {
            csv.append(escapeCsv(a.getTokenNumber())).append(",");
            csv.append(escapeCsv(a.getCitizen().getFirstName() + " " + a.getCitizen().getLastName())).append(",");
            csv.append(escapeCsv(a.getService().getName())).append(",");
            csv.append(escapeCsv(a.getBranch().getName())).append(",");
            csv.append(a.getAppointmentDate()).append(",");
            csv.append(a.getAppointmentTime()).append(",");
            csv.append(a.getStatus()).append(",");
            csv.append(a.getCreatedAt()).append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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

    public List<SmartInsight> generateInsights() {
        List<SmartInsight> insights = new java.util.ArrayList<>();

        List<WeeklyPerformanceEntry> weekly = getWeeklyPerformance();
        List<PeakHourEntry> peak = getPeakHours();
        List<ServiceDistributionEntry> distribution = getServiceDistribution();
        List<WaitTimeTrendEntry> waitTrend = getWaitTimeTrend();
        AdminDashboardStats stats = getDashboardStats();

        if (stats.getNumberOfAppointments() == 0) {
            insights.add(new SmartInsight(
                    "General",
                    "There isn't enough booking history yet to generate meaningful insights. Check back once more appointments have been made.",
                    "neutral"
            ));
            return insights;
        }

        WeeklyPerformanceEntry busiestDay = weekly.stream()
                .max(java.util.Comparator.comparingLong(w -> w.getClientsServed() + w.getNoShows()))
                .orElse(null);
        if (busiestDay != null && (busiestDay.getClientsServed() + busiestDay.getNoShows()) > 0) {
            insights.add(new SmartInsight(
                    "Traffic Pattern",
                    String.format("%s has historically been the busiest day, with %d clients served. Consider ensuring full staffing on this day.",
                            busiestDay.getDay(), busiestDay.getClientsServed()),
                    "neutral"
            ));
        }

        PeakHourEntry busiestHour = peak.stream()
                .max(java.util.Comparator.comparingLong(PeakHourEntry::getClientsPerHour))
                .orElse(null);
        if (busiestHour != null) {
            insights.add(new SmartInsight(
                    "Peak Hours",
                    String.format("The %s time slot sees the highest number of bookings. Citizens visiting outside this window may experience shorter waits.",
                            busiestHour.getHour()),
                    "neutral"
            ));
        }

        ServiceDistributionEntry topService = distribution.stream()
                .max(java.util.Comparator.comparingDouble(ServiceDistributionEntry::getPercentage))
                .orElse(null);
        if (topService != null) {
            insights.add(new SmartInsight(
                    "Service Demand",
                    String.format("%s is the most requested service, accounting for %.1f%% of all appointments.",
                            topService.getServiceName(), topService.getPercentage()),
                    "neutral"
            ));
        }

        if (stats.getNoShowRatePercent() >= 20) {
            insights.add(new SmartInsight(
                    "No-Show Rate",
                    String.format("The no-show rate is %.1f%%, which is relatively high. Consider sending appointment reminders to reduce missed bookings.",
                            stats.getNoShowRatePercent()),
                    "warning"
            ));
        } else if (stats.getNoShowRatePercent() <= 5 && stats.getNoShowRatePercent() > 0) {
            insights.add(new SmartInsight(
                    "No-Show Rate",
                    String.format("The no-show rate is a healthy %.1f%%. Current reminder and confirmation processes appear to be working well.",
                            stats.getNoShowRatePercent()),
                    "positive"
            ));
        } else if (stats.getNoShowRatePercent() == 0) {
            insights.add(new SmartInsight(
                    "No-Show Rate",
                    "No no-shows have been recorded so far. Excellent attendance rate.",
                    "positive"
            ));
        }

        List<WaitTimeTrendEntry> nonZeroWaits = waitTrend.stream()
                .filter(w -> w.getAvgWaitMinutes() > 0)
                .toList();
        if (nonZeroWaits.size() >= 2) {
            double first = nonZeroWaits.get(0).getAvgWaitMinutes();
            double last = nonZeroWaits.get(nonZeroWaits.size() - 1).getAvgWaitMinutes();
            if (last > first * 1.2) {
                insights.add(new SmartInsight(
                        "Wait Times",
                        "Average wait times appear to be trending upward across the week. This may indicate a need for additional staff during busy periods.",
                        "warning"
                ));
            } else if (last < first * 0.8) {
                insights.add(new SmartInsight(
                        "Wait Times",
                        "Average wait times are trending downward across the week, suggesting improved queue efficiency.",
                        "positive"
                ));
            }
        }

        if (stats.getAvgWaitTimeMinutes() > 30) {
            insights.add(new SmartInsight(
                    "Efficiency",
                    String.format("The overall average wait time is %.0f minutes. Reviewing service durations or adding a second staff member per branch could help.",
                            stats.getAvgWaitTimeMinutes()),
                    "warning"
            ));
        }

        if (insights.isEmpty()) {
            insights.add(new SmartInsight(
                    "General",
                    "System performance looks stable, with no significant patterns to flag at this time.",
                    "neutral"
            ));
        }

        return insights;
    }
}
