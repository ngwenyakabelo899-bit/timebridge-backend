package com.timebridge.service;

import com.timebridge.dto.QueueResponse;
import com.timebridge.entity.*;
import com.timebridge.repository.AppointmentRepository;
import com.timebridge.repository.QueueRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class QueueService {

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final QueueRepository queueRepository;
    private final AppointmentRepository appointmentRepository;

    public QueueService(QueueRepository queueRepository, AppointmentRepository appointmentRepository) {
        this.queueRepository = queueRepository;
        this.appointmentRepository = appointmentRepository;
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
}
