package com.emergency.routing.dto;

import com.emergency.routing.model.HandoverStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public class HandoverRequestDto {
    @NotBlank
    private String patientName;
    @NotBlank
    private String admittedBy;
    @NotBlank
    private String admitterPhone;
    @Positive
    private int age;
    @NotNull
    private Long hospitalId;
    @NotNull
    private Long ambulanceId;
    private HandoverStatus status = HandoverStatus.PENDING;

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getAdmittedBy() { return admittedBy; }
    public void setAdmittedBy(String admittedBy) { this.admittedBy = admittedBy; }
    public String getAdmitterPhone() { return admitterPhone; }
    public void setAdmitterPhone(String admitterPhone) { this.admitterPhone = admitterPhone; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public HandoverStatus getStatus() { return status; }
    public void setStatus(HandoverStatus status) { this.status = status; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public Long getAmbulanceId() { return ambulanceId; }
    public void setAmbulanceId(Long ambulanceId) { this.ambulanceId = ambulanceId; }
}
