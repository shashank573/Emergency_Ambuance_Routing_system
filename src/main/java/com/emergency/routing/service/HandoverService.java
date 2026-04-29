package com.emergency.routing.service;

import com.emergency.routing.dto.HandoverRequestDto;
import com.emergency.routing.model.Ambulance;
import com.emergency.routing.model.EmergencyRequest;
import com.emergency.routing.model.PatientHandover;
import com.emergency.routing.repository.AmbulanceRepository;
import com.emergency.routing.repository.EmergencyRequestRepository;
import com.emergency.routing.repository.HospitalRepository;
import com.emergency.routing.repository.PatientHandoverRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

@Service
public class HandoverService {

    private final PatientHandoverRepository handoverRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final EmergencyRequestRepository emergencyRequestRepository;

    public HandoverService(
        PatientHandoverRepository handoverRepository,
        AmbulanceRepository ambulanceRepository,
        HospitalRepository hospitalRepository,
        EmergencyRequestRepository emergencyRequestRepository
    ) {
        this.handoverRepository = handoverRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.emergencyRequestRepository = emergencyRequestRepository;
    }

    public PatientHandover create(HandoverRequestDto requestDto) {
        PatientHandover handover = new PatientHandover();
        handover.setPatientName(requestDto.getPatientName());
        handover.setAdmittedBy(requestDto.getAdmittedBy());
        handover.setAdmitterPhone(requestDto.getAdmitterPhone());
        handover.setAge(requestDto.getAge());
        handover.setStatus(requestDto.getStatus());
        handover.setHospitalId(requestDto.getHospitalId());

        ambulanceRepository
            .findById(requestDto.getAmbulanceId())
            .ifPresent(ambulance -> {
                ambulance.setStatus(
                    com.emergency.routing.model.AmbulanceStatus.AVAILABLE
                );
                ambulanceRepository.save(ambulance);
            });

        PatientHandover savedHandover = handoverRepository.save(handover);

        try (FileWriter fw = new FileWriter("HandoverRecords.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("Handover ID: %d%n", savedHandover.getId());
            pw.printf("Patient: %s, Age: %d%n", savedHandover.getPatientName(), savedHandover.getAge());
            pw.printf("Admitted By: %s (%s)%n", savedHandover.getAdmittedBy(), savedHandover.getAdmitterPhone());
            pw.printf("Hospital ID: %d | Ambulance ID: %d%n", savedHandover.getHospitalId(), requestDto.getAmbulanceId());
            pw.printf("Status: %s%n", savedHandover.getStatus());
            pw.printf("--------------------------------------------------%n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String hospitalName = hospitalRepository.findById(requestDto.getHospitalId())
            .map(h -> h.getName())
            .orElse("Unknown Hospital");

        int cost = requestDto.getCost() != null ? requestDto.getCost() : 500 + (int)(Math.random() * 1000);
        String totalTime = requestDto.getTotalTime() != null ? requestDto.getTotalTime() : "Unknown";
        String sourceAddress = requestDto.getSource() != null ? requestDto.getSource() : "Unknown Source";

        try (FileWriter fw = new FileWriter("RideDetails.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("Ride for Patient: %s%n", savedHandover.getPatientName());
            pw.printf("Rider Name: Amit Sharma%n");
            pw.printf("Source: %s%n", sourceAddress);
            pw.printf("Destination: %s%n", hospitalName);
            pw.printf("Duration: %s%n", totalTime);
            pw.printf("Fare: ₹%d%n", cost);
            pw.printf("--------------------------------------------------%n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return savedHandover;
    }

    public List<PatientHandover> getByHospital(Long hospitalId) {
        return handoverRepository.findByHospitalId(hospitalId);
    }
}
