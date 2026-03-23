package com.emergency.routing.repository;

import com.emergency.routing.model.Hospital;
import com.emergency.routing.model.Specialization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    List<Hospital> findByAvailableBedsGreaterThan(int beds);
    List<Hospital> findBySpecializationAndAvailableBedsGreaterThan(Specialization specialization, int beds);
    boolean existsByName(String name);
}
