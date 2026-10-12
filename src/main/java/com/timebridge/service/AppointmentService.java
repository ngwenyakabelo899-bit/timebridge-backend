package com.timebridge.service;

import com.timebridge.dto.AppointmentRequest;
import com.timebridge.entity.Appointment;
import com.timebridge.entity.AppointmentStatus;
import com.timebridge.entity.Branch;
import com.timebridge.entity.User;
import com.timebridge.exception.BookingRuleException;
import com.timebridge.repository.AppointmentRepository;
import com.timebridge.repository.BranchRepository;
import com.timebridge.repository.ServiceRepository;
import com.timebridge.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AppointmentService {

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final List<AppointmentStatus> INACTIVE = List.of(
            AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW, AppointmentStatus.COMPLETED);

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BranchRepository branchRepository;
    private final AvailabilityService availabilityService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              ServiceRepository serviceRepository,
                              BranchRepository branchRepository,
                              AvailabilityService availabilityService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.branchRepository = branchRepository;
        this.availabilityService = availabilityService;
    }

    @Transactional
    public Appointment createAppointment(String citizenEmail, AppointmentRequest request) {
        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Locking the branch row makes bookings for one branch run one at a time (no double booking).
        Branch branch = branchRepository.findByIdForUpdate(request.getBranchId())
                .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        com.timebridge.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new NoSuchElementException("Service not found"));

        String provinceCode = request.getProvinceCode();
        if (provinceCode != null && !provinceCode.isBlank()
                && (branch.getProvince() == null || !branch.getProvince().name().equals(provinceCode))) {
            throw new BookingRuleException(HttpStatus.BAD_REQUEST,
                    "The selected branch is not in the selected province.");
        }

        boolean offered = branch.getServices().stream().anyMatch(s -> s.getId().equals(service.getId()));
        if (!offered) {
            throw new BookingRuleException(HttpStatus.BAD_REQUEST,
                    "The selected branch does not offer this service.");
        }

        availabilityService.assertBookable(branch, request.getAppointmentDate(), request.getAppointmentTime(), null);

        if (appointmentRepository.existsByCitizenAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
                citizen, request.getAppointmentDate(), request.getAppointmentTime(), INACTIVE)) {
            throw new BookingRuleException(HttpStatus.CONFLICT,
                    "You already have an appointment at this date and time.");
        }
        if (appointmentRepository.existsByCitizenAndBranchAndServiceAndAppointmentDateAndStatusNotIn(
                citizen, branch, service, request.getAppointmentDate(), INACTIVE)) {
            throw new BookingRuleException(HttpStatus.CONFLICT,
                    "You already have an appointment for this service at this branch on this date.");
        }

        Appointment appointment = new Appointment(
                citizen, service, branch,
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                generateUniqueToken()
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

    @Transactional
    public Appointment rescheduleAppointment(String citizenEmail, Long appointmentId, AppointmentRequest request) {
        Appointment appointment = getOwnedAppointment(citizenEmail, appointmentId);

        if (INACTIVE.contains(appointment.getStatus())) {
            throw new BookingRuleException(HttpStatus.BAD_REQUEST,
                    "This appointment can no longer be rescheduled.");
        }

        Branch branch = branchRepository.findByIdForUpdate(appointment.getBranch().getId())
                .orElseThrow(() -> new NoSuchElementException("Branch not found"));

        availabilityService.assertBookable(branch, request.getAppointmentDate(), request.getAppointmentTime(), appointment);

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
        do {
            StringBuilder sb = new StringBuilder("APT-");
            for (int i = 0; i < 6; i++) {
                sb.append(TOKEN_CHARS.charAt(RANDOM.nextInt(TOKEN_CHARS.length())));
            }
            candidate = sb.toString();
        } while (appointmentRepository.existsByTokenNumber(candidate));
        return candidate;
    }
}
