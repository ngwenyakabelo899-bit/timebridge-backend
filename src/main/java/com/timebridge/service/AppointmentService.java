package com.timebridge.service;

import com.timebridge.dto.AppointmentRequest;
import com.timebridge.entity.*;
import com.timebridge.repository.AppointmentRepository;
import com.timebridge.repository.BranchRepository;
import com.timebridge.repository.ServiceRepository;
import com.timebridge.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AppointmentService {

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BranchRepository branchRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              ServiceRepository serviceRepository,
                              BranchRepository branchRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.branchRepository = branchRepository;
    }

    public Appointment createAppointment(String citizenEmail, AppointmentRequest request) {
        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        com.timebridge.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new NoSuchElementException("Service not found"));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        String tokenNumber = generateUniqueToken();

        Appointment appointment = new Appointment(
                citizen, service, branch,
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                tokenNumber
        );

        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsForCitizen(String citizenEmail) {
        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return appointmentRepository.findByCitizenOrderByAppointmentDateDesc(citizen);
    }

    public Appointment cancelAppointment(String citizenEmail, Long appointmentId) {
        Appointment appointment = getOwnedAppointment(citizenEmail, appointmentId);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }

    public Appointment rescheduleAppointment(String citizenEmail, Long appointmentId, AppointmentRequest request) {
        Appointment appointment = getOwnedAppointment(citizenEmail, appointmentId);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        return appointmentRepository.save(appointment);
    }

    private Appointment getOwnedAppointment(String citizenEmail, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found"));

        if (!appointment.getCitizen().getEmail().equals(citizenEmail)) {
            throw new SecurityException("You do not have permission to modify this appointment");
        }
        return appointment;
    }

    private String generateUniqueToken() {
        String candidate;
        boolean exists;
        do {
            StringBuilder sb = new StringBuilder("APT-");
            for (int i = 0; i < 6; i++) {
                sb.append(TOKEN_CHARS.charAt(RANDOM.nextInt(TOKEN_CHARS.length())));
            }
            candidate = sb.toString();
            final String tokenToCheck = candidate;
            exists = appointmentRepository.findAll().stream()
                    .anyMatch(a -> a.getTokenNumber().equals(tokenToCheck));
        } while (exists);
        return candidate;
    }
}
