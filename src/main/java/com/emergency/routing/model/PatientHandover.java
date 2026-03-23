package com.emergency.routing.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PatientHandover {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String patientName;
    private String admittedBy;
    private String admitterPhone;
    private int age;
    @Enumerated(EnumType.STRING)
    private HandoverStatus status;
    private Long hospitalId;

    public PatientHandover() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
}
