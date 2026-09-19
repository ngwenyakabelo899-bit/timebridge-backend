package com.timebridge.repository;

import com.timebridge.entity.Appointment;
import com.timebridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByCitizen(User citizen);

    List<Appointment> findByCitizenOrderByAppointmentDateDesc(User citizen);
}
