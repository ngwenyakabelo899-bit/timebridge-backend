package com.timebridge.config;

import com.timebridge.entity.*;
import com.timebridge.repository.BranchRepository;
import com.timebridge.repository.ServiceRepository;
import com.timebridge.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final String ID_SERVICE = "ID Application/Collection";
    private static final String PASSPORT_SERVICE = "Passport Application/Collection";
    private static final String BIRTH_SERVICE = "Birth Certificate";
    private static final String GRANTS_SERVICE = "Register for Grants (Old Age)";
    private static final String VISA_SERVICE = "Visa and Permit Application Submission";

    private static final String SASSA_PORTAL = "https://services.sassa.gov.za/portal/r/sassa/sassa/contact-us-pu";
    private static final String SASSA_PAGE = "http://www.sassa.gov.za/index.php/component/content/article/85-contact-us/100-contact-us";
    private static final String DHA_VFS_PAGE = "https://www.dha.gov.za/index.php/component/content/article/10-immigration-services/469-vfs-global-offices";

    private static final String DEMO_NOTE = "Phase 3 demonstration record - address not verified against an official source.";
    private static final String SASSA_NOTE = "SASSA regional office as published on the SASSA website. Local offices handle most grant applications - confirm before visiting.";
    private static final String VFS_NOTE = "VFS Global centre listed on the Department of Home Affairs website, for visa and permit applications only.";

    private record BranchSeed(String name, Province province, String department, String location,
                              String address, String phone, int capacity, boolean verified,
                              String sourceUrl, String note, List<String> services) {
    }

    private final BranchRepository branchRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(BranchRepository branchRepository,
                      ServiceRepository serviceRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.branchRepository = branchRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedServices();
        seedBranches();
        seedDemoUsers();
    }

    private void seedServices() {
        ensureService(ID_SERVICE, 30);
        ensureService(PASSPORT_SERVICE, 30);
        ensureService(BIRTH_SERVICE, 20);
        ensureService(GRANTS_SERVICE, 25);
        ensureService(VISA_SERVICE, 30);
    }

    private void ensureService(String name, int minutes) {
        if (serviceRepository.findByName(name).isEmpty()) {
            serviceRepository.save(new Service(name, minutes));
        }
    }

    private void seedBranches() {
        for (BranchSeed seed : branchSeeds()) {
            ensureBranch(seed);
        }
    }

    // Creates a branch if it is missing. A branch that already has a province is left alone,
    // so changes made by an administrator are never overwritten.
    private void ensureBranch(BranchSeed seed) {
        Branch branch = branchRepository.findByNameIgnoreCase(seed.name()).orElse(null);
        if (branch == null) {
            branch = new Branch(seed.name(), seed.location(), seed.capacity());
        } else if (branch.getProvince() != null) {
            return;
        }

        branch.setProvince(seed.province());
        branch.setDepartment(seed.department());
        branch.setAddress(seed.address());
        branch.setPhone(seed.phone());
        branch.setVerified(seed.verified());
        branch.setSourceUrl(seed.sourceUrl());
        branch.setDataNote(seed.note());

        Set<Service> services = new HashSet<>();
        for (String serviceName : seed.services()) {
            serviceRepository.findByName(serviceName).ifPresent(services::add);
        }
        branch.setServices(services);
        branchRepository.save(branch);
    }

    private List<BranchSeed> branchSeeds() {
        List<BranchSeed> seeds = new ArrayList<>();

        // Phase 3 demonstration branches (not verified real-world branches)
        seeds.add(demo("Kimberley Home Affairs & SASSA", "Corner of Knight & Priel Road, Kimberley", 60));
        seeds.add(demo("Upington Home Affairs & SASSA", "19 Mark Street, Upington", 40));
        seeds.add(demo("De Aar Home Affairs & SASSA", "Murray Avenue, De Aar", 30));
        seeds.add(demo("Kuruman Home Affairs & SASSA", "Main Street, Kuruman", 25));

        // SASSA regional offices, one per province, from official SASSA pages
        seeds.add(sassa(Province.EASTERN_CAPE, "East London",
                "1st Floor Waverley Office Park, 3-33 Phillip Frame Road, Chiselhurst, East London, 5200",
                "043 707 6335", SASSA_PORTAL,
                "An older SASSA page lists the BKB Building, Cnr Fitzpatrick & Merino Road, Quigney."));
        seeds.add(sassa(Province.FREE_STATE, "Bloemfontein",
                "African Life Building, 75 St Andrews Street, Bloemfontein, 9300",
                "051 410 8304/5", SASSA_PAGE, "Published on an older SASSA page."));
        seeds.add(sassa(Province.GAUTENG, "Johannesburg",
                "28 Harrison Street, Johannesburg, 2000",
                "011 241 8320", SASSA_PORTAL, ""));
        seeds.add(sassa(Province.KWAZULU_NATAL, "Pietermaritzburg",
                "1 Bank Street, Pietermaritzburg, 3201",
                "033 846 3400", SASSA_PORTAL, "An older SASSA page lists 033 846 3300."));
        seeds.add(sassa(Province.LIMPOPO, "Polokwane",
                "43 Landros Mare Street, Polokwane, 0699",
                "015 291 7509", SASSA_PORTAL, ""));
        seeds.add(sassa(Province.MPUMALANGA, "Nelspruit",
                "18 Ferreira Street, Nelspruit, 1200",
                "013 754 9380", SASSA_PAGE, "Published on an older SASSA page."));
        seeds.add(sassa(Province.NORTHERN_CAPE, "Kimberley",
                "95-97 Du Toit Span Road, Kimberley, 8300",
                "053 802 4900", SASSA_PAGE, "Published on an older SASSA page."));
        seeds.add(sassa(Province.NORTH_WEST, "Mmabatho",
                "SASSA House, University Drive, Mmabatho, 2735",
                null, SASSA_PAGE, "Published on an older SASSA page, which lists no telephone number."));
        seeds.add(sassa(Province.WESTERN_CAPE, "Cape Town",
                "Golden Acre, Adderley Street, Cape Town",
                "021 469 0235", SASSA_PORTAL, ""));

        // VFS Global permit application centres listed on the Department of Home Affairs website
        seeds.add(vfs(Province.EASTERN_CAPE, "Port Elizabeth",
                "Office 7D, 1st Floor Moffet on Main, Cnr 17th Ave and Main Road, Walmer, Port Elizabeth, 6070"));
        seeds.add(vfs(Province.FREE_STATE, "Bloemfontein",
                "Suite 4, The Park, 14 Reid Street, Westdene, Bloemfontein"));
        seeds.add(vfs(Province.GAUTENG, "Pretoria",
                "Cherry Lane Office Park, 1st Floor, 114 Fehrsen Street, Nieuw Muckleneuk, Brooklyn, 0181"));
        seeds.add(vfs(Province.KWAZULU_NATAL, "Durban",
                "Suite 3, Silver Oaks, Ground Floor, 36 Silverton Road, Musgrave, Durban"));
        seeds.add(vfs(Province.LIMPOPO, "Polokwane",
                "Shop 13, Thornhill Shopping Centre, Cnr Veldspaat and Munnik Ave, Bendor, Polokwane"));
        seeds.add(vfs(Province.MPUMALANGA, "Nelspruit",
                "Office 5F, Nedbank Building, 30 Brown Street, Nelspruit"));
        seeds.add(vfs(Province.NORTHERN_CAPE, "Kimberley",
                "Unit 3, Building 2, Agri Office Park, N12, Kimberley"));
        seeds.add(vfs(Province.NORTH_WEST, "Rustenburg",
                "Inner Arcade Shopping Centre, Shop 2, Cnr Fatima Bhayat and Boom Street, Rustenburg"));
        seeds.add(vfs(Province.WESTERN_CAPE, "Cape Town",
                "21st Floor, 2 Riebeek Street, Absa Building, Cape Town, 8001"));

        return seeds;
    }

    private BranchSeed demo(String name, String address, int capacity) {
        return new BranchSeed(name, Province.NORTHERN_CAPE, "Home Affairs & SASSA (demonstration)",
                address, address, null, capacity, false, null, DEMO_NOTE,
                List.of(ID_SERVICE, PASSPORT_SERVICE, BIRTH_SERVICE, GRANTS_SERVICE));
    }

    private BranchSeed sassa(Province province, String town, String address, String phone,
                             String sourceUrl, String extraNote) {
        String note = extraNote == null || extraNote.isBlank() ? SASSA_NOTE : SASSA_NOTE + " " + extraNote;
        return new BranchSeed("SASSA " + province.getDisplayName() + " Regional Office", province,
                "SASSA", town, address, phone, 40, true, sourceUrl, note, List.of(GRANTS_SERVICE));
    }

    private BranchSeed vfs(Province province, String town, String address) {
        return new BranchSeed("VFS Global Centre - " + town, province,
                "Home Affairs - VFS Global permit centre", town, address, null, 40, true,
                DHA_VFS_PAGE, VFS_NOTE, List.of(VISA_SERVICE));
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
