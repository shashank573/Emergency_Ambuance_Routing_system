package com.emergency.routing.service;

import com.emergency.routing.model.Ambulance;
import com.emergency.routing.model.AmbulanceStatus;
import com.emergency.routing.repository.AmbulanceRepository;
import com.emergency.routing.util.DistanceUtil;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AmbulanceService {
    private final AmbulanceRepository ambulanceRepository;
    private final DistanceUtil distanceUtil;

    public AmbulanceService(AmbulanceRepository ambulanceRepository, DistanceUtil distanceUtil) {
        this.ambulanceRepository = ambulanceRepository;
        this.distanceUtil = distanceUtil;
    }

    public List<Ambulance> getAll() {
        return ambulanceRepository.findAll();
    }

    public Ambulance getNearestAvailableAmbulance(double latitude, double longitude) {
        // Linear searching through available ambulances.
        return ambulanceRepository.findByStatus(AmbulanceStatus.AVAILABLE)
                .stream()
                .min(Comparator.comparingDouble(a ->
                        distanceUtil.haversineKm(a.getLatitude(), a.getLongitude(), latitude, longitude)))
                .orElseThrow(() -> new IllegalStateException("No available ambulance right now"));
    }

    public Ambulance updateStatus(Long ambulanceId, AmbulanceStatus status) {
        Ambulance ambulance = ambulanceRepository.findById(ambulanceId)
                .orElseThrow(() -> new IllegalArgumentException("Ambulance not found"));
        ambulance.setStatus(status);
        return ambulanceRepository.save(ambulance);
    }

    public Ambulance updateStatusByCode(String code, AmbulanceStatus status) {
        Ambulance ambulance = ambulanceRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Ambulance strict Code not found in database"));
        ambulance.setStatus(status);
        return ambulanceRepository.save(ambulance);
    }

    public Ambulance save(Ambulance ambulance) {
        return ambulanceRepository.save(ambulance);
    }

    public boolean existsByCode(String code) {
        return ambulanceRepository.existsByCode(code);
    }

    public void deleteAmbulance(Long id) {
        if (!ambulanceRepository.existsById(id)) {
            throw new IllegalArgumentException("Ambulance ID does not exist.");
        }
        ambulanceRepository.deleteById(id);
    }
}
