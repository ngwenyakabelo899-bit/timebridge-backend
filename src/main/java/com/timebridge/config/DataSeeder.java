package com.timebridge.config;

import com.timebridge.entity.*;
import com.timebridge.repository.BranchRepository;
import com.timebridge.repository.ServiceRepository;
import com.timebridge.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DataSeeder(BranchRepository branchRepository,
                      ServiceRepository serviceRepository,
                      UserRepository userRepository,
                      org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.branchRepository = branchRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedBranches();
        seedServices();
        seedDemoUsers();
    }

    private void seedBranches() {
        if (branchRepository.count() > 0) {
            return;
        }
        branchRepository.save(new Branch("Kimberley Home Affairs & SASSA",
                "Corner of Knight & Priel Road, Kimberley", 60));
        branchRepository.save(new Branch("Upington Home Affairs & SASSA",
                "19 Mark Street, Upington", 40));
        branchRepository.save(new Branch("De Aar Home Affairs & SASSA",
                "Murray Avenue, De Aar", 30));
        branchRepository.save(new Branch("Kuruman Home Affairs & SASSA",
                "Main Street, Kuruman", 25));
        System.out.println(">>> Seeded 4 branches");
    }

    private void seedServices() {
        if (serviceRepository.count() > 0) {
            return;
        }
        serviceRepository.save(new Service("ID Application/Collection", 30));
        serviceRepository.save(new Service("Passport Application/Collection", 30));
        serviceRepository.save(new Service("Birth Certificate", 20));
        serviceRepository.save(new Service("Register for Grants (Old Age)", 25));
        System.out.println(">>> Seeded 4 services");
    }

    private void seedDemoUsers() {
        seedUser("John", "Doe", "citizen@timebridge.gov.za", "0821234567", "password123", Role.ROLE_CITIZEN);
        seedUser("Thabo", "Molefe", "staff@timebridge.gov.za", "0837654321", "password123", Role.ROLE_STAFF);
        seedUser("Nomsa", "Dlamini", "admin@timebridge.gov.za", "0845551234", "admin123", Role.ROLE_ADMIN);
    }

    private void seedUser(String firstName, String lastName, String email, String phone,
                          String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User(firstName, lastName, email, phone, passwordEncoder.encode(password), role);
        user.setEmailVerified(true);
        userRepository.save(user);
        System.out.println(">>> Seeded demo account: " + email);
    }
}
