package com.emergency.routing.dto;

import com.emergency.routing.model.Ambulance;
import com.emergency.routing.model.EmergencyRequest;
import com.emergency.routing.model.Hospital;
import java.util.List;

public class EmergencyResponseDto {

    private EmergencyRequest emergencyRequest;
    private Ambulance ambulance;
    private Hospital hospital;
    private List<CoordinateDto> routeAmbulanceToPatient;
    private List<CoordinateDto> routePatientToHospital;
    private int etaMinutes;
    private String message;

    public EmergencyRequest getEmergencyRequest() {
        return emergencyRequest;
    }

    public void setEmergencyRequest(EmergencyRequest emergencyRequest) {
        this.emergencyRequest = emergencyRequest;
    }

    public Ambulance getAmbulance() {
        return ambulance;
    }

    public void setAmbulance(Ambulance ambulance) {
        this.ambulance = ambulance;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public List<CoordinateDto> getRouteAmbulanceToPatient() {
        return routeAmbulanceToPatient;
    }

    public void setRouteAmbulanceToPatient(
        List<CoordinateDto> routeAmbulanceToPatient
    ) {
        this.routeAmbulanceToPatient = routeAmbulanceToPatient;
    }

    public List<CoordinateDto> getRoutePatientToHospital() {
        return routePatientToHospital;
    }

    public void setRoutePatientToHospital(
        List<CoordinateDto> routePatientToHospital
    ) {
        this.routePatientToHospital = routePatientToHospital;
    }

    public int getEtaMinutes() {
        return etaMinutes;
    }

    public void setEtaMinutes(int etaMinutes) {
        this.etaMinutes = etaMinutes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
