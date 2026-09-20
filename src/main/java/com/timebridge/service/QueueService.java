package com.timebridge.service;

import com.timebridge.dto.QueueResponse;
import com.timebridge.entity.*;
import com.timebridge.repository.AppointmentRepository;
import com.timebridge.repository.QueueRepository;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;
import com.timebridge.dto.StaffDashboardStats;
import com.timebridge.dto.StaffQueueResponse;
import com.timebridge.repository.BranchRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

@Service
public class QueueService {

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final QueueRepository queueRepository;
    private final AppointmentRepository appointmentRepository;
    private final BranchRepository branchRepository;

    public QueueService(QueueRepository queueRepository, AppointmentRepository appointmentRepository,
                        BranchRepository branchRepository) {
        this.queueRepository = queueRepository;
        this.appointmentRepository = appointmentRepository;
        this.branchRepository = branchRepository;
    }

    public QueueResponse joinQueue(String citizenEmail, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found"));

        if (!appointment.getCitizen().getEmail().equals(citizenEmail)) {
            throw new SecurityException("You do not have permission to use this appointment");
        }

        if (queueRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new IllegalStateException("This appointment is already in a queue");
        }

        Queue queue = new Queue(appointment, generateQueueToken(), 0, 0);
        queue = queueRepository.save(queue);

        recalculatePositionsForBranch(appointment.getBranch());

        Queue refreshed = queueRepository.findById(queue.getId())
                .orElseThrow(() -> new NoSuchElementException("Queue entry not found after save"));

        return toResponse(refreshed);
    }

    public QueueResponse getMyQueueStatus(String citizenEmail, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found"));

        if (!appointment.getCitizen().getEmail().equals(citizenEmail)) {
            throw new SecurityException("You do not have permission to view this queue entry");
        }

        Queue queue = queueRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("This appointment has not joined a queue"));

        return toResponse(queue);
    }

    public QueueResponse leaveQueue(String citizenEmail, Long queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new NoSuchElementException("Queue entry not found"));

        if (!queue.getAppointment().getCitizen().getEmail().equals(citizenEmail)) {
            throw new SecurityException("You do not have permission to leave this queue entry");
        }

        Branch branch = queue.getAppointment().getBranch();
        queue.setStatus(QueueStatus.CANCELLED);
        queueRepository.save(queue);

        recalculatePositionsForBranch(branch);

        return toResponse(queue);
    }

    private void recalculatePositionsForBranch(Branch branch) {
        List<Queue> waiting = queueRepository.findWaitingByBranchOrderByJoinedAt(branch);

        int position = 1;
        int cumulativeMinutes = 0;

        for (Queue q : waiting) {
            q.setQueuePosition(position);
            q.setEstimatedWaitMinutes(cumulativeMinutes);
            queueRepository.save(q);

            cumulativeMinutes += q.getAppointment().getService().getDurationMinutes();
            position++;
        }
    }

    private QueueResponse toResponse(Queue queue) {
        return new QueueResponse(
                queue.getId(),
                queue.getQueueToken(),
                queue.getQueuePosition(),
                queue.getEstimatedWaitMinutes(),
                queue.getStatus().name(),
                queue.getAppointment().getBranch().getName(),
                queue.getAppointment().getService().getName(),
                queue.getAppointment().getTokenNumber()
        );
    }

    private String generateQueueToken() {
        String candidate;
        boolean exists;
        do {
            StringBuilder sb = new StringBuilder("Q-");
            for (int i = 0; i < 6; i++) {
                sb.append(TOKEN_CHARS.charAt(RANDOM.nextInt(TOKEN_CHARS.length())));
            }
            candidate = sb.toString();
            final String tokenToCheck = candidate;
            exists = queueRepository.findAll().stream()
                    .anyMatch(q -> q.getQueueToken().equals(tokenToCheck));
        } while (exists);
        return candidate;
    }

    public List<StaffQueueResponse> getWaitingQueue(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        return queueRepository.findWaitingByBranchOrderByJoinedAt(branch).stream()
                .map(this::toStaffResponse)
                .toList();
    }

    public StaffQueueResponse getCurrentClient(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        List<Queue> called = queueRepository.findCalledByBranch(branch);
        if (called.isEmpty()) {
            return null;
        }
        return toStaffResponse(called.get(0));
    }

    public StaffQueueResponse callNextClient(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        List<Queue> waiting = queueRepository.findWaitingByBranchOrderByJoinedAt(branch);
        if (waiting.isEmpty()) {
            throw new NoSuchElementException("No clients waiting at this branch");
        }

        Queue next = waiting.get(0);
        next.setStatus(QueueStatus.CALLED);
        queueRepository.save(next);

        recalculatePositionsForBranch(branch);

        return toStaffResponse(next);
    }

    public StaffQueueResponse serveClient(Long queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new NoSuchElementException("Queue entry not found"));

        queue.setStatus(QueueStatus.SERVED);
        queueRepository.save(queue);

        return toStaffResponse(queue);
    }

    public StaffQueueResponse markNoShow(Long queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new NoSuchElementException("Queue entry not found"));

        Branch branch = queue.getAppointment().getBranch();
        queue.setStatus(QueueStatus.NO_SHOW);
        queueRepository.save(queue);

        recalculatePositionsForBranch(branch);

        return toStaffResponse(queue);
    }

    public StaffDashboardStats getDashboardStats(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long inQueue = queueRepository.findWaitingByBranchOrderByJoinedAt(branch).size();
        long servedToday = queueRepository.countServedTodayByBranch(branch, startOfDay);
        Double avgServiceTime = queueRepository.averageServiceMinutesTodayByBranch(branch, startOfDay);

        List<Queue> waiting = queueRepository.findWaitingByBranchOrderByJoinedAt(branch);
        int currentWait = waiting.isEmpty() ? 0 : waiting.get(0).getEstimatedWaitMinutes();

        return new StaffDashboardStats(
                inQueue,
                servedToday,
                avgServiceTime == null ? 0.0 : avgServiceTime,
                currentWait
        );
    }

    private StaffQueueResponse toStaffResponse(Queue queue) {
        return new StaffQueueResponse(
                queue.getId(),
                queue.getQueueToken(),
                queue.getAppointment().getCitizen().getFirstName(),
                queue.getAppointment().getCitizen().getLastName(),
                queue.getAppointment().getService().getName(),
                queue.getQueuePosition(),
                queue.getEstimatedWaitMinutes(),
                queue.getStatus().name()
        );
    }

}
