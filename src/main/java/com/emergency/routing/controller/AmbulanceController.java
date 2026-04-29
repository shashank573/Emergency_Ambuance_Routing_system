package com.emergency.routing.controller;

import com.emergency.routing.model.Ambulance;
import com.emergency.routing.model.AmbulanceStatus;
import com.emergency.routing.service.AmbulanceService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import java.util.Collections;

@RestController
@RequestMapping("/ambulances")
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    public AmbulanceController(AmbulanceService ambulanceService) {
        this.ambulanceService = ambulanceService;
    }

    @GetMapping
    public ResponseEntity<List<Ambulance>> getAll() {
        return ResponseEntity.ok(ambulanceService.getAll());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Ambulance> updateStatus(
        @PathVariable Long id,
        @RequestBody StatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
            ambulanceService.updateStatus(id, request.status())
        );
    }

    @PutMapping("/code/{code}/status")
    public ResponseEntity<Ambulance> updateStatusByCode(
        @PathVariable String code,
        @RequestBody StatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
            ambulanceService.updateStatusByCode(code, request.status())
        );
    }

    @PostMapping
    public ResponseEntity<?> createAmbulance(@RequestBody Ambulance ambulance) {
        if (ambulanceService.existsByCode(ambulance.getCode())) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap(
                    "error",
                    "Ambulance ID already exists."
                )
            );
        }
        ambulance.setStatus(AmbulanceStatus.AVAILABLE);
        return ResponseEntity.status(HttpStatus.CREATED).body(ambulanceService.save(ambulance));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAmbulance(@PathVariable Long id) {
        try {
            ambulanceService.deleteAmbulance(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Collections.singletonMap("error", e.getMessage())
            );
        }
    }

    public record StatusUpdateRequest(AmbulanceStatus status) {}
}
