package com.timebridge.service;

import com.timebridge.dto.BranchLoadInfo;
import com.timebridge.entity.Appointment;
import com.timebridge.entity.Branch;
import com.timebridge.entity.Queue;
import com.timebridge.entity.QueueStatus;
import com.timebridge.repository.AppointmentRepository;
import com.timebridge.repository.BranchRepository;
import com.timebridge.repository.QueueRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class BranchLoadService {

    private final BranchRepository branchRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueRepository queueRepository;

    public BranchLoadService(BranchRepository branchRepository,
                             AppointmentRepository appointmentRepository,
                             QueueRepository queueRepository) {
        this.branchRepository = branchRepository;
        this.appointmentRepository = appointmentRepository;
        this.queueRepository = queueRepository;
    }

    public List<BranchLoadInfo> getLoadComparison(LocalDate date) {
        List<Branch> branches = branchRepository.findAll();
        List<Appointment> allAppointments = appointmentRepository.findAll();
        List<Queue> allWaitingQueues = queueRepository.findByStatusOrderByQueuePositionAsc(QueueStatus.WAITING);

        List<BranchLoadInfo> results = branches.stream().map(branch -> {
            int appointmentsOnDate = (int) allAppointments.stream()
                    .filter(a -> a.getBranch().getId().equals(branch.getId()))
                    .filter(a -> a.getAppointmentDate().equals(date))
                    .filter(a -> a.getStatus() != com.timebridge.entity.AppointmentStatus.CANCELLED)
                    .count();

            long currentQueueCount = allWaitingQueues.stream()
                    .filter(q -> q.getAppointment().getBranch().getId().equals(branch.getId()))
                    .count();

            int capacity = branch.getDailyCapacity() == null || branch.getDailyCapacity() == 0
                    ? 1 : branch.getDailyCapacity();

            double loadPercentage = Math.round(
                    ((appointmentsOnDate + currentQueueCount) * 100.0 / capacity) * 10.0
            ) / 10.0;

            return new BranchLoadInfo(
                    branch.getId(), branch.getName(), branch.getLocation(),
                    appointmentsOnDate, currentQueueCount, capacity, loadPercentage, false
            );
        }).collect(java.util.stream.Collectors.toList());

        results.stream()
                .min(Comparator.comparingDouble(BranchLoadInfo::getLoadPercentage))
                .ifPresent(least -> {
                    int idx = results.indexOf(least);
                    BranchLoadInfo recommended = new BranchLoadInfo(
                            least.getBranchId(), least.getBranchName(), least.getLocation(),
                            least.getAppointmentsOnDate(), least.getCurrentQueueCount(),
                            least.getDailyCapacity(), least.getLoadPercentage(), true
                    );
                    results.set(idx, recommended);
                });

        return results;
    }
}
