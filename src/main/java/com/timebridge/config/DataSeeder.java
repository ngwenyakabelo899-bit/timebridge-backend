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
        if (!userRepository.existsByEmail("citizen@timebridge.gov.za")) {
            userRepository.save(new User("John", "Doe", "citizen@timebridge.gov.za",
                    "0821234567", passwordEncoder.encode("password123"), Role.ROLE_CITIZEN));
            System.out.println(">>> Seeded demo citizen account");
        }

        if (!userRepository.existsByEmail("staff@timebridge.gov.za")) {
            userRepository.save(new User("Thabo", "Molefe", "staff@timebridge.gov.za",
                    "0837654321", passwordEncoder.encode("password123"), Role.ROLE_STAFF));
            System.out.println(">>> Seeded demo staff account");
        }

        if (!userRepository.existsByEmail("admin@timebridge.gov.za")) {
            userRepository.save(new User("Nomsa", "Dlamini", "admin@timebridge.gov.za",
                    "0845551234", passwordEncoder.encode("admin123"), Role.ROLE_ADMIN));
            System.out.println(">>> Seeded demo admin account");
        }

        System.out.println(">>> DEMO LOGINS:");
        System.out.println(">>>   Citizen: citizen@timebridge.gov.za / password123");
        System.out.println(">>>   Staff:   staff@timebridge.gov.za / password123");
        System.out.println(">>>   Admin:   admin@timebridge.gov.za / admin123");
    }
}
