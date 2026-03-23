package com.emergency.routing.controller;

import com.emergency.routing.model.AmbulanceStatus;
import com.emergency.routing.repository.AmbulanceRepository;
import com.emergency.routing.repository.HospitalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;

    public AdminController(HospitalRepository hospitalRepository, AmbulanceRepository ambulanceRepository) {
        this.hospitalRepository = hospitalRepository;
        this.ambulanceRepository = ambulanceRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        long totalHospitals = hospitalRepository.count();
        long hospitalsWithBeds = hospitalRepository.findByAvailableBedsGreaterThan(0).size();
        long availableAmbulances = ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE).size();
        return ResponseEntity.ok(new StatsResponse(totalHospitals, hospitalsWithBeds, availableAmbulances));
    }

    public record StatsResponse(long totalHospitals, long hospitalsWithBeds, long availableAmbulances) { }
}
