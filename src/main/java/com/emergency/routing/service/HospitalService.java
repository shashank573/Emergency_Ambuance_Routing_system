package com.emergency.routing.service;

import com.emergency.routing.model.Hospital;
import com.emergency.routing.model.Specialization;
import com.emergency.routing.repository.HospitalRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public List<Hospital> getAvailableHospitals(Specialization specialization) {
        return hospitalRepository.findBySpecializationAndAvailableBedsGreaterThan(
            specialization,
            0
        );
    }

    public Hospital updateBeds(Long hospitalId, int availableBeds) {
        Hospital hospital = hospitalRepository
            .findById(hospitalId)
            .orElseThrow(() ->
                new IllegalArgumentException("Hospital not found")
            );
        hospital.setAvailableBeds(availableBeds);
        return hospitalRepository.save(hospital);
    }

    public Hospital save(Hospital hospital) {
        return hospitalRepository.save(hospital);
    }

    public boolean existsById(Long id) {
        return hospitalRepository.existsById(id);
    }

    public Hospital createHospital(Hospital hospital) {
        if (hospital.getId() == null) {
            throw new IllegalArgumentException("Hospital ID is required.");
        }
        if (existsById(hospital.getId())) {
            throw new IllegalArgumentException("Hospital ID already exists.");
        }
        if (hospitalRepository.existsByName(hospital.getName())) {
            throw new IllegalArgumentException(
                "Hospital with this Name already exists. Please use unique details."
            );
        }
        if (hospital.getAvailableBeds() < 0) {
            throw new IllegalArgumentException("Beds must be non-negative.");
        }
        return hospitalRepository.save(hospital);
    }

    public void deleteHospital(Long id) {
        if (!existsById(id)) {
            throw new IllegalArgumentException("Hospital ID does not exist.");
        }
        hospitalRepository.deleteById(id);
    }
}
