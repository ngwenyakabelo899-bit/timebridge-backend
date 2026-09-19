package com.timebridge.repository;

import com.timebridge.entity.Appointment;
import com.timebridge.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByAppointment(Appointment appointment);
}
