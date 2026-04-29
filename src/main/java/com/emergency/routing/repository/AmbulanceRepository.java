package com.emergency.routing.repository;

import com.emergency.routing.model.Ambulance;
import com.emergency.routing.model.AmbulanceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByStatus(AmbulanceStatus status);

    Optional<Ambulance> findByCode(String code);

    boolean existsByCode(String code);
}
