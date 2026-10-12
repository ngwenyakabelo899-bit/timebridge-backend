package com.timebridge.controller;

import com.timebridge.entity.Province;
import com.timebridge.repository.BranchRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provinces")
public class ProvinceController {

    private final BranchRepository branchRepository;

    public ProvinceController(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getProvinces() {
        return Arrays.stream(Province.values())
                .map(p -> Map.<String, Object>of(
                        "code", p.name(),
                        "name", p.getDisplayName(),
                        "branchCount", branchRepository.countByProvince(p)))
                .toList();
    }
}
