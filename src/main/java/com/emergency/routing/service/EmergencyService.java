package com.emergency.routing.service;

import com.emergency.routing.dto.CoordinateDto;
import com.emergency.routing.dto.EmergencyRequestDto;
import com.emergency.routing.dto.EmergencyResponseDto;
import com.emergency.routing.model.Ambulance;
import com.emergency.routing.model.AmbulanceStatus;
import com.emergency.routing.model.EmergencyRequest;
import com.emergency.routing.model.EmergencyStatus;
import com.emergency.routing.model.Hospital;
import com.emergency.routing.model.HospitalPreference;
import com.emergency.routing.model.HospitalType;
import com.emergency.routing.model.Severity;
import com.emergency.routing.repository.EmergencyRequestRepository;
import com.emergency.routing.util.DistanceUtil;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class EmergencyService {

    private final EmergencyRequestRepository emergencyRequestRepository;
    private final AmbulanceService ambulanceService;
    private final HospitalService hospitalService;
    private final RoutingService routingService;
    private final DistanceUtil distanceUtil;
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(4);

    public EmergencyService(
        EmergencyRequestRepository emergencyRequestRepository,
        AmbulanceService ambulanceService,
        HospitalService hospitalService,
        RoutingService routingService,
        DistanceUtil distanceUtil
    ) {
        this.emergencyRequestRepository = emergencyRequestRepository;
        this.ambulanceService = ambulanceService;
        this.hospitalService = hospitalService;
        this.routingService = routingService;
        this.distanceUtil = distanceUtil;
    }

    private final PriorityQueue<EmergencyRequest> emergencyQueue =
        new PriorityQueue<>(
            Comparator.comparingInt((EmergencyRequest e) ->
                severityPriority(e.getSeverity())
            ).thenComparing(EmergencyRequest::getTimestamp)
        );

    @Transactional
    public synchronized EmergencyResponseDto handleEmergencyRequest(
        EmergencyRequestDto requestDto
    ) {
        validateCoordinates(
            requestDto.getLatitude(),
            requestDto.getLongitude()
        );

        EmergencyRequest emergencyRequest = new EmergencyRequest();
        emergencyRequest.setLatitude(requestDto.getLatitude());
        emergencyRequest.setLongitude(requestDto.getLongitude());
        emergencyRequest.setSeverity(requestDto.getSeverity());
        emergencyRequest.setTimestamp(LocalDateTime.now());
        emergencyRequest.setStatus(EmergencyStatus.RECEIVED);

        emergencyRequest = emergencyRequestRepository.save(emergencyRequest);
        emergencyQueue.offer(emergencyRequest);
        EmergencyRequest current = emergencyQueue.poll();
        if (current == null) {
            throw new IllegalStateException(
                "Emergency queue processing failed"
            );
        }

        Ambulance ambulance = ambulanceService.getNearestAvailableAmbulance(
            current.getLatitude(),
            current.getLongitude()
        );
        if (ambulance.getStatus() != AmbulanceStatus.AVAILABLE) {
            throw new IllegalStateException(
                "Selected ambulance is no longer available"
            );
        }

        Hospital selectedHospital = selectHospital(
            current.getLatitude(),
            current.getLongitude(),
            requestDto
        );
        if (selectedHospital.getAvailableBeds() <= 0) {
            throw new IllegalStateException(
                "Selected hospital has no beds available"
            );
        }

        List<CoordinateDto> ambulanceToPatientPath =
            routingService.calculateShortestPath(
                new CoordinateDto(
                    ambulance.getLatitude(),
                    ambulance.getLongitude()
                ),
                new CoordinateDto(current.getLatitude(), current.getLongitude())
            );
        List<CoordinateDto> patientToHospitalPath =
            routingService.calculateShortestPath(
                new CoordinateDto(
                    current.getLatitude(),
                    current.getLongitude()
                ),
                new CoordinateDto(
                    selectedHospital.getLatitude(),
                    selectedHospital.getLongitude()
                )
            );

        double leg1 = distanceUtil.haversineKm(
            ambulance.getLatitude(),
            ambulance.getLongitude(),
            current.getLatitude(),
            current.getLongitude()
        );
        double leg2 = distanceUtil.haversineKm(
            current.getLatitude(),
            current.getLongitude(),
            selectedHospital.getLatitude(),
            selectedHospital.getLongitude()
        );
        int etaMinutes = routingService.estimateETA(leg1 + leg2);
        int trafficPercent = 10 + (int) (Math.random() * 60);
        int trafficDelay = (int) (etaMinutes * (trafficPercent / 100.0));
        etaMinutes += trafficDelay;

        ambulanceService.updateStatus(ambulance.getId(), AmbulanceStatus.BUSY);
        hospitalService.updateBeds(
            selectedHospital.getId(),
            selectedHospital.getAvailableBeds() - 1
        );



        current.setStatus(EmergencyStatus.ASSIGNED);
        current.setAssignedAmbulanceId(ambulance.getId());
        current.setAssignedHospitalId(selectedHospital.getId());
        current.setEtaMinutes(etaMinutes);
        EmergencyRequest updated = emergencyRequestRepository.save(current);

        long phase1Delay = 18L + (long) (Math.random() * 3);
        scheduler.schedule(
            () -> {
                updated.setStatus(EmergencyStatus.REACHED_PATIENT);
                emergencyRequestRepository.save(updated);

                long phase2Delay = 18L + (long) (Math.random() * 3);
                scheduler.schedule(
                    () -> {
                        updated.setStatus(EmergencyStatus.REACHED_HOSPITAL);
                        emergencyRequestRepository.save(updated);
                    },
                    phase2Delay,
                    TimeUnit.SECONDS
                );
            },
            phase1Delay,
            TimeUnit.SECONDS
        );

        EmergencyResponseDto response = new EmergencyResponseDto();
        response.setEmergencyRequest(updated);
        response.setAmbulance(ambulance);
        response.setHospital(selectedHospital);
        response.setRouteAmbulanceToPatient(ambulanceToPatientPath);
        response.setRoutePatientToHospital(patientToHospitalPath);
        response.setEtaMinutes(etaMinutes);
        response.setMessage("Ambulance assigned successfully");
        return response;
    }

    public EmergencyRequest getStatus(Long id) {
        return emergencyRequestRepository
            .findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Emergency request not found")
            );
    }

    private Hospital selectHospital(
        double latitude,
        double longitude,
        EmergencyRequestDto requestDto
    ) {
        List<Hospital> available = hospitalService.getAvailableHospitals(
            requestDto.getRequiredSpecialization()
        );
        available = available
            .stream()
            .filter(h ->
                matchesPreference(
                    h.getType(),
                    requestDto.getHospitalPreference()
                )
            )
            .collect(Collectors.toList());
        if (available.isEmpty()) {
            throw new IllegalStateException(
                "No matching hospital available for selected specialization/type"
            );
        }

        available.sort(
            Comparator.comparingDouble((Hospital h) ->
                distanceUtil.haversineKm(
                    latitude,
                    longitude,
                    h.getLatitude(),
                    h.getLongitude()
                )
            ).thenComparing(
                Comparator.comparingInt(Hospital::getAvailableBeds).reversed()
            )
        );

        return available.get(0);
    }

    private boolean matchesPreference(
        HospitalType type,
        HospitalPreference preference
    ) {
        if (preference == HospitalPreference.ANY) {
            return true;
        }
        return switch (preference) {
            case GOVERNMENT -> type == HospitalType.GOVERNMENT;
            case PRIVATE -> type == HospitalType.PRIVATE;
            case ANY -> true;
        };
    }

    private static int severityPriority(Severity severity) {
        return switch (severity) {
            case CRITICAL -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (
            latitude < -90 ||
            latitude > 90 ||
            longitude < -180 ||
            longitude > 180
        ) {
            throw new IllegalArgumentException("Invalid latitude/longitude");
        }
    }
}
