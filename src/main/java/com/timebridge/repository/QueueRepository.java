package com.timebridge.repository;

import com.timebridge.entity.Branch;
import com.timebridge.entity.Queue;
import com.timebridge.entity.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueRepository extends JpaRepository<Queue, Long> {

    List<Queue> findByStatusOrderByQueuePositionAsc(QueueStatus status);

    long countByStatus(QueueStatus status);
}
