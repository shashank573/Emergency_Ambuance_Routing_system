package com.emergency.routing.service;

import com.emergency.routing.dto.HandoverRequestDto;
import com.emergency.routing.model.PatientHandover;
import com.emergency.routing.repository.AmbulanceRepository;
import com.emergency.routing.repository.PatientHandoverRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HandoverService {
    private final PatientHandoverRepository handoverRepository;
    private final AmbulanceRepository ambulanceRepository;

    public HandoverService(PatientHandoverRepository handoverRepository, AmbulanceRepository ambulanceRepository) {
        this.handoverRepository = handoverRepository;
        this.ambulanceRepository = ambulanceRepository;
    }

    public PatientHandover create(HandoverRequestDto requestDto) {
        PatientHandover handover = new PatientHandover();
        handover.setPatientName(requestDto.getPatientName());
        handover.setAdmittedBy(requestDto.getAdmittedBy());
        handover.setAdmitterPhone(requestDto.getAdmitterPhone());
        handover.setAge(requestDto.getAge());
        handover.setStatus(requestDto.getStatus());
        handover.setHospitalId(requestDto.getHospitalId());
        
        // Return the ambulance to the functional active duty pool so it can be re-dispatched cleanly forever!
        ambulanceRepository.findById(requestDto.getAmbulanceId()).ifPresent(ambulance -> {
            ambulance.setStatus(com.emergency.routing.model.AmbulanceStatus.AVAILABLE);
            ambulanceRepository.save(ambulance);
        });

        return handoverRepository.save(handover);
    }

    public List<PatientHandover> getByHospital(Long hospitalId) {
        return handoverRepository.findByHospitalId(hospitalId);
    }
}
