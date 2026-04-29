package com.emergency.routing.repository;

import com.emergency.routing.model.PatientHandover;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientHandoverRepository
    extends JpaRepository<PatientHandover, Long>
{
    List<PatientHandover> findByHospitalId(Long hospitalId);
}
