package com.timebridge.repository;

import com.timebridge.entity.Appointment;
import com.timebridge.entity.AppointmentStatus;
import com.timebridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByCitizen(User citizen);

    List<Appointment> findByCitizenOrderByAppointmentDateDesc(User citizen);

    long countByStatus(AppointmentStatus status);

    List<Appointment> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = 'NO_SHOW'")
    long countNoShows();
}
