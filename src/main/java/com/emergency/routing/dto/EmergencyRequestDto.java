package com.emergency.routing.dto;

import com.emergency.routing.model.HospitalPreference;
import com.emergency.routing.model.Severity;
import com.emergency.routing.model.Specialization;
import jakarta.validation.constraints.NotNull;

public class EmergencyRequestDto {

    private double latitude;
    private double longitude;

    @NotNull
    private Severity severity;

    @NotNull
    private Specialization requiredSpecialization;

    @NotNull
    private HospitalPreference hospitalPreference;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Specialization getRequiredSpecialization() {
        return requiredSpecialization;
    }

    public void setRequiredSpecialization(
        Specialization requiredSpecialization
    ) {
        this.requiredSpecialization = requiredSpecialization;
    }

    public HospitalPreference getHospitalPreference() {
        return hospitalPreference;
    }

    public void setHospitalPreference(HospitalPreference hospitalPreference) {
        this.hospitalPreference = hospitalPreference;
    }
}
