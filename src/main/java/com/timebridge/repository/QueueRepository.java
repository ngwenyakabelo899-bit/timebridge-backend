package com.timebridge.repository;

import com.timebridge.entity.Branch;
import com.timebridge.entity.Queue;
import com.timebridge.entity.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueRepository extends JpaRepository<Queue, Long> {

    List<Queue> findByStatusOrderByQueuePositionAsc(QueueStatus status);

    long countByStatus(QueueStatus status);

    Optional<Queue> findByAppointmentId(Long appointmentId);

    @Query("SELECT q FROM Queue q WHERE q.status = 'WAITING' " +
            "AND q.appointment.branch = :branch ORDER BY q.joinedAt ASC")
    List<Queue> findWaitingByBranchOrderByJoinedAt(Branch branch);

    @Query("SELECT q FROM Queue q WHERE q.status = 'CALLED' " +
            "AND q.appointment.branch = :branch ORDER BY q.joinedAt ASC")
    List<Queue> findCalledByBranch(Branch branch);

    @Query("SELECT COUNT(q) FROM Queue q WHERE q.status = 'SERVED' " +
            "AND q.appointment.branch = :branch AND q.joinedAt >= :startOfDay")
    long countServedTodayByBranch(Branch branch, LocalDateTime startOfDay);

    @Query("SELECT AVG(q.appointment.service.durationMinutes) FROM Queue q " +
            "WHERE q.status = 'SERVED' AND q.appointment.branch = :branch AND q.joinedAt >= :startOfDay")
    Double averageServiceMinutesTodayByBranch(Branch branch, LocalDateTime startOfDay);

    @Query("SELECT q FROM Queue q ORDER BY q.joinedAt DESC")
    List<Queue> findRecentActivity(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT AVG(q.estimatedWaitMinutes) FROM Queue q WHERE q.status = 'SERVED'")
    Double averageWaitTimeAllServed();
}
