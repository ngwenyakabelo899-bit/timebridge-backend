package com.timebridge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "branches")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Branch name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer dailyCapacity;

    @Enumerated(EnumType.STRING)
    private Province province;

    private String department;

    @Column(length = 300)
    private String address;

    private String phone;

    @Column(length = 500)
    private String sourceUrl;

    @Column(length = 500)
    private String dataNote;

    @Column(nullable = false)
    private boolean verified = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "branch_services",
            joinColumns = @JoinColumn(name = "branch_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Service> services = new HashSet<>();

    public Branch() {
    }

    public Branch(String name, String location, Integer dailyCapacity) {
        this.name = name;
        this.location = location;
        this.dailyCapacity = dailyCapacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getDailyCapacity() { return dailyCapacity; }
    public void setDailyCapacity(Integer dailyCapacity) { this.dailyCapacity = dailyCapacity; }

    public Province getProvince() { return province; }
    public void setProvince(Province province) { this.province = province; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getDataNote() { return dataNote; }
    public void setDataNote(String dataNote) { this.dataNote = dataNote; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public Set<Service> getServices() { return services; }
    public void setServices(Set<Service> services) { this.services = services; }
}
